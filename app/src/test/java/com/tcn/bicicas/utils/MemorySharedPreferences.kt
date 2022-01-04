package com.tcn.bicicas.utils

import android.content.SharedPreferences


class MemorySharedPreferences : SharedPreferences {

    private var values: Map<String, Any?> = mutableMapOf()

    override fun getAll(): Map<String, *> = values

    override fun getString(key: String, defValue: String?): String? =
        values[key] as? String? ?: defValue

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: Set<String>?): Set<String>? =
        values[key] as? Set<String>? ?: defValues

    override fun getInt(key: String?, defValue: Int): Int =
        values[key] as? Int? ?: defValue

    override fun getLong(key: String?, defValue: Long): Long =
        values[key] as? Long? ?: defValue

    override fun getFloat(key: String?, defValue: Float): Float =
        values[key] as? Float? ?: defValue

    override fun getBoolean(key: String?, defValue: Boolean): Boolean =
        values[key] as? Boolean? ?: defValue

    override fun contains(key: String): Boolean =
        values.containsKey(key)

    override fun edit(): SharedPreferences.Editor =
        EditorImpl()

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        throw NotImplementedError()
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        throw NotImplementedError()
    }

    inner class EditorImpl : SharedPreferences.Editor {

        private val newValues: MutableMap<String, Any?> = values.toMutableMap()

        override fun putString(key: String, value: String?) = put(key, value)

        override fun putStringSet(key: String, values: Set<String>?) = put(key, values)

        override fun putInt(key: String, value: Int) = put(key, newValues)

        override fun putLong(key: String, value: Long) = put(key, newValues)

        override fun putFloat(key: String, value: Float) = put(key, newValues)

        override fun putBoolean(key: String, value: Boolean) = put(key, newValues)

        override fun remove(key: String) = this.apply { newValues.remove(key) }

        private fun put(key: String, value: Any?) = this.apply { newValues[key] = value }

        override fun clear() = this.apply { newValues.clear() }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            values = newValues
        }
    }

}