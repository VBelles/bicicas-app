package com.tcn.bicicas.main

import com.tcn.bicicas.common.Clock
import com.tcn.bicicas.common.StoreManager
import com.tcn.bicicas.settings.domain.Settings
import com.tcn.bicicas.stations.domain.GetStations
import com.tcn.bicicas.stations.domain.model.StationsData
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okio.FileSystem

interface MainModule {
    val clock: Clock
    val httpClient: HttpClient
    val storeManager: StoreManager
    val mainViewModel: MainViewModel
}

class MainModuleImpl(
    filesDir: String,
    httpEngineFactory: HttpClientEngineFactory<*> = OkHttp,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    fileSystem: FileSystem = FileSystem.SYSTEM,
    override val clock: Clock = Clock(System::currentTimeMillis)
) : MainModule {

    override val httpClient: HttpClient by lazy { buildHttpClient(httpEngineFactory) }

    override val storeManager: StoreManager by lazy {
        StoreManager(
            dispatcher = dispatcher,
            baseDir = "$filesDir/store",
            epochInMillis = clock::millis,
            fileSystem = fileSystem,
        )
    }

    override val mainViewModel: MainViewModel
        get() = MainViewModel(
            settingsStore = storeManager.getStore(defaultValue = Settings()),
            getStations = GetStations(
                stationsStore = storeManager.getStore<StationsData>("stations", StationsData()),
                favoriteStore = storeManager.getListStore<String>("favorite")
            )
        )
}

fun buildHttpClient(httpEngineFactory: HttpClientEngineFactory<*>) = HttpClient(httpEngineFactory) {
    expectSuccess = true
    install(ContentNegotiation) {
        @OptIn(ExperimentalSerializationApi::class)
        json(Json {
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        })
    }
    install(Logging) {
        level = LogLevel.ALL
    }
}