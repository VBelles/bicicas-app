package com.tcn.bicicas.common

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.tcn.bicicas.pin.domain.model.TwoFactorAuth
import com.tcn.bicicas.settings.domain.Settings
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jasypt.util.text.BasicTextEncryptor


suspend fun legacyMigration(
    context: Context,
    storeManager: StoreManager,
    password: String,
) = coroutineScope {
    val preferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    awaitAll(
        async { legacyFavoriteStationsMigration(preferences, storeManager.getListStore("favorite")) },
        async { legacySettingsMigration(preferences, storeManager.getStore(defaultValue = Settings())) },
        async { legacyTwoFactorAuthMigration(preferences, storeManager.getStore(), password) },
    )
    migrationCleanup(preferences)
}

private suspend fun legacyFavoriteStationsMigration(
    preferences: SharedPreferences,
    store: Store<List<String>>
) = runCatching {
    val favorites = preferences.getStringSet("favorites", null)
    if (favorites.isNullOrEmpty()) return@runCatching
    store.update { favorites.toList() }
    preferences.edit { remove("favorites") }
}

private suspend fun legacySettingsMigration(
    preferences: SharedPreferences,
    store: Store<Settings>,
) = runCatching {
    if (!preferences.contains(KEY_INITIAL_SCREEN)) return@runCatching
    val settings = Settings()
    val previousSettings = Settings(
        initialScreen = preferences.getEnum(KEY_INITIAL_SCREEN, settings.initialScreen),
        lastScreen = preferences.getInt(KEY_LAST_SCREEN, settings.lastScreen),
        theme = preferences.getEnum(KEY_THEME, settings.theme),
        navigationType = preferences.getEnum(KEY_NAVIGATION_TYPE, settings.navigationType),
        dynamicColorEnabled = preferences.getBoolean(
            KEY_DYNAMIC_COLOR_ENABLED, settings.dynamicColorEnabled
        )
    )
    store.update { previousSettings }
}

private suspend fun legacyTwoFactorAuthMigration(
    preferences: SharedPreferences,
    store: Store<TwoFactorAuth?>,
    password: String,
) = runCatching {
    if (!preferences.contains(KEY_USER)) return@runCatching
    val encryptor = BasicTextEncryptor().apply { setPassword(password) }
    val twoFactorAuth = runCatching {
        TwoFactorAuth(
            user = preferences.getString(KEY_USER, null)?.let(encryptor::decrypt)!!,
            secret = preferences.getString(KEY_SECRET, null)?.let(encryptor::decrypt)!!
        )
    }.getOrNull() ?: return@runCatching
    store.update { twoFactorAuth }
}

private fun migrationCleanup(preferences: SharedPreferences) {
    preferences.edit { clear() }
}


private const val KEY_INITIAL_SCREEN = "settings.initialScreen"
private const val KEY_LAST_SCREEN = "settings.lastScreen"
private const val KEY_THEME = "settings.theme"
private const val KEY_NAVIGATION_TYPE = "settings.navigationType"
private const val KEY_DYNAMIC_COLOR_ENABLED = "settings.dynamicColorEnabled"
private const val KEY_USER = "twoFactorAuth.user"
private const val KEY_SECRET = "twoFactorAuth.secret"

private inline fun <reified T : Enum<T>> SharedPreferences.getEnum(
    key: String,
    defaultValue: T
): T {
    val name = getString(key, null)
    return T::class.java.enumConstants?.find { e -> e.name == name } ?: defaultValue
}