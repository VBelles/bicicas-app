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
    favoritesStore: Store<List<String>>,
    settingsStore: Store<Settings>,
    authStore: Store<TwoFactorAuth?>,
    password: CharArray,
) = coroutineScope {
    val preferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    awaitAll(
        async { legacyFavoriteStationsMigration(preferences, favoritesStore) },
        async { legacySettingsMigration(preferences, settingsStore) },
        async { legacyTwoFactorAuthMigration(preferences, authStore, password) },
    )
    migrationCleanup(preferences)
}

suspend fun legacyFavoriteStationsMigration(
    preferences: SharedPreferences,
    store: Store<List<String>>
) {
    val favorites = preferences.getStringSet("favorites", null) ?: return
    store.update { favorites.toList() }
    preferences.edit { remove("favorites") }
}

suspend fun legacySettingsMigration(
    preferences: SharedPreferences,
    store: Store<Settings>,
) {
    if (!preferences.contains(KEY_INITIAL_SCREEN)) return
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

suspend fun legacyTwoFactorAuthMigration(
    preferences: SharedPreferences,
    store: Store<TwoFactorAuth?>,
    password: CharArray,
) {
    if (!preferences.contains(KEY_USER)) return
    val encryptor = BasicTextEncryptor().apply { setPasswordCharArray(password) }
    val twoFactorAuth = runCatching {
        TwoFactorAuth(
            user = preferences.getString(KEY_USER, null)?.let(encryptor::decrypt)!!,
            secret = preferences.getString(KEY_SECRET, null)?.let(encryptor::decrypt)!!
        )
    }.getOrNull() ?: return
    store.update { twoFactorAuth }
}

fun migrationCleanup(preferences: SharedPreferences) {
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