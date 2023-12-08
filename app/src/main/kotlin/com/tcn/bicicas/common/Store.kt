package com.tcn.bicicas.common

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import kotlinx.serialization.json.okio.encodeToBufferedSink
import kotlinx.serialization.serializer
import okio.BufferedSource
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import java.lang.ref.WeakReference

/**
 * A manager for [Store] instances that allows to share the same [Store] instances within the same
 * [StoreManager] when the same key and version are used.
 *
 * @property dispatcher The [CoroutineDispatcher] used by the [Store] instances created by this
 * manager to perform IO operations.
 * @property baseDir The base directory where all the stores will be saved.
 */
class StoreManager(
    private val dispatcher: CoroutineDispatcher,
    private val baseDir: String,
    private val epochInMillis: () -> Long,
    private val fileSystem: FileSystem,
) {

    private data class Key(val name: String, val version: Long)

    private val storeInstances = mutableMapOf<Key, WeakReference<Store<*>>>()

    private val lock = reentrantLock()

    @OptIn(ExperimentalSerializationApi::class)
    private val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    fun <T> getStore(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
        version: Long,
        migration: (fromVersion: Long, content: JsonElement?) -> T,
    ): Store<T> = lock.withLock {
        val compoundKey = Key(key, version)

        @Suppress("UNCHECKED_CAST")
        val instance = storeInstances[compoundKey]?.get() as? Store<T>?
        if (instance != null) {
            return instance
        }

        val newInstance = Store(
            dispatcher = dispatcher,
            path = baseDir.toPath() / key.toPath(),
            version = version,
            fileSystem = fileSystem,
            json = json,
            serializer = serializer,
            defaultValue = defaultValue,
            epochInMillis = epochInMillis,
            migration = migration,
        )
        storeInstances[compoundKey] = WeakReference(newInstance)
        return newInstance
    }


    /**
     * Removes all data
     */
    fun clear() {
        fileSystem.deleteRecursively(baseDir.toPath(), false)
    }

    /**
     * Returns an [Store] that allows to store serializable data using the file system
     * Instances retrieved within the same [StoreManager] sharing key and version are reused to
     * allow to share cache and to avoid race conditions writing files with the [Store]
     *
     * @param key Unique identifier used as file name.
     * @param version The version of the data stored to check for migrations.
     * @param migration A function invoked to perform migrations transforming outdated data into
     * new data.
     *
     * @return An instance of [Store] that allows to store serializable data using the file system.
     */
    inline fun <reified T> getListStore(
        key: String,
        version: Long = 0,
        noinline migration: (fromVersion: Long, content: JsonElement?) -> List<T> = { _, _ -> emptyList() }
    ): Store<List<T>> = getStore(key, emptyList(), serializer(), version, migration)

    inline fun <reified T> getStore(
        key: String = T::class.simpleName!!,
        defaultValue: T,
        version: Long = 0,
        noinline migration: (fromVersion: Long, content: JsonElement?) -> T = { _, _ -> defaultValue }
    ): Store<T> = getStore(key, defaultValue, serializer(), version, migration)

    inline fun <reified T> getStore(
        key: String = T::class.simpleName!!,
        version: Long = 0,
        noinline migration: (fromVersion: Long, content: JsonElement?) -> T? = { _, _ -> null }
    ): Store<T?> = getStore(key, null, serializer(), version, migration)

}

/**
 * A generic data store class that uses the file system to persist data.
 */

class Store<T> internal constructor(
    private val dispatcher: CoroutineDispatcher,
    private val path: Path,
    private val version: Long,
    private val fileSystem: FileSystem,
    private val defaultValue: T,
    private val epochInMillis: () -> Long,
    private val json: Json,
    private val serializer: KSerializer<T>,
    private val migration: suspend (fromVersion: Long, content: JsonElement?) -> T = { _, _ -> defaultValue },
) {
    private val mutex = Mutex()

    @Serializable
    data class Metadata(val version: Long, val timestamp: Long)

    private var stateFlow: MutableStateFlow<T>? = null

    private suspend fun stateFlow(): MutableStateFlow<T> {
        if (stateFlow == null) {
            stateFlow = MutableStateFlow(read())
        }
        return stateFlow!!
    }

    /**
     * @return The current value of the data stored, or null if there is no data stored.
     */
    suspend fun get(): T = mutex.withLock {
        return stateFlow().value
    }

    /**
     * @return A Flow instance that emits updated values of the data store.
     */
    fun updates(): Flow<T> = flow {
        val stateFlow = mutex.withLock { stateFlow() }
        stateFlow.collect { emit(it) }
    }

    /**
     * Updates the data store with the given update function.
     *
     * @param update The update function that takes the current value of the data stored and
     * returns the new value.
     */
    suspend fun update(update: (T) -> T): Unit = mutex.withLock {
        val updated = update(stateFlow().value)
        stateFlow().value = updated
        write(updated)
    }

    private suspend fun read(): T = withContext(dispatcher) {
        when (val result = read(version)) {
            is ReadResult.OutDated -> {
                val migratedData = migration(result.metadata.version, result.data)
                val metadata = Metadata(version, epochInMillis())
                write(metadata, migratedData)
                migratedData
            }

            is ReadResult.UpToDate -> result.data
            null -> defaultValue
        }
    }

    private suspend fun write(value: T) = withContext(dispatcher) {
        val metadata = Metadata(version, epochInMillis())
        write(metadata, value)
    }

    sealed interface ReadResult<T> {
        class UpToDate<T>(val data: T) : ReadResult<T>
        class OutDated<T>(val metadata: Metadata, val data: JsonElement) : ReadResult<T>
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun write(metadata: Metadata, value: T) {
        if (value == defaultValue) {
            fileSystem.delete(path)
            return
        }

        path.parent?.let { fileSystem.createDirectories(it) }
        fileSystem.write(path) {
            writeUtf8(json.encodeToString(metadata) + "\n")
            json.encodeToBufferedSink(serializer, value, this)
        }
    }

    private fun read(version: Long): ReadResult<T>? {
        if (!fileSystem.exists(path)) {
            // File will be created when data is stored
            return null
        }

        return fileSystem.read(path) {
            // Get metadata
            val metadata: Metadata? = try {
                readUtf8Line()?.let { json -> Json.decodeFromString(json) }
            } catch (e: Exception) {
                null
            }

            // Check migration
            if (metadata != null && metadata.version != version) {
                val data = readJsonSafely<JsonElement>(this, serializer())
                data?.let { ReadResult.OutDated(metadata, data) }
            } else if (metadata != null) {
                val data = readJsonSafely(this, serializer)
                data?.let { ReadResult.UpToDate(data) }
            } else {
                null
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun <T> readJsonSafely(buffer: BufferedSource, serializer: KSerializer<T>): T? {
        return try {
            json.decodeFromBufferedSource(serializer, buffer)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}