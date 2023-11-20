package com.tcn.bicicas.data.datasource.local

import android.content.SharedPreferences
import androidx.core.content.edit

class FavoriteStationsStore(private val preferences: SharedPreferences) : LocalStore<Set<String>> {

    override fun get(): Set<String>? =
        preferences.getStringSet("favorites", null)

    override fun save(value: Set<String>) =
        preferences.edit { putStringSet("favorites", value) }

    override fun clear() =
        preferences.edit { remove("favorites") }

}
