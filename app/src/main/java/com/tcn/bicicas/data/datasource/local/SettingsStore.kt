package com.tcn.bicicas.data.datasource.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.tcn.bicicas.data.model.Settings


class SettingsStore(private val preferences: SharedPreferences) : LocalStore<Settings> {

    companion object {
        private const val KEY_INITIAL_SCREEN = "settings.initialScreen"
        private const val KEY_THEME = "settings.theme"
        private const val KEY_NAVIGATION_TYPE = "settings.navigationType"
        private const val KEY_DYNAMIC_COLOR_ENABLED = "settings.dynamicColorEnabled"
    }

    override fun get(): Settings {
        val settings = Settings()
        return Settings(
            initialScreen = preferences.getEnum(KEY_INITIAL_SCREEN, settings.initialScreen),
            theme = preferences.getEnum(KEY_THEME, settings.theme),
            navigationType = preferences.getEnum(KEY_NAVIGATION_TYPE, settings.navigationType),
            dynamicColorEnabled = preferences.getBoolean(
                KEY_DYNAMIC_COLOR_ENABLED, settings.dynamicColorEnabled
            )
        )
    }

    override fun save(value: Settings) {
        preferences.edit {
            putEnum(KEY_INITIAL_SCREEN, value.initialScreen)
            putEnum(KEY_THEME, value.theme)
            putEnum(KEY_NAVIGATION_TYPE, value.navigationType)
            putBoolean(KEY_DYNAMIC_COLOR_ENABLED, value.dynamicColorEnabled)
        }
    }

    override fun clear() {
        preferences.edit {
            remove(KEY_INITIAL_SCREEN)
            remove(KEY_THEME)
            remove(KEY_NAVIGATION_TYPE)
            remove(KEY_DYNAMIC_COLOR_ENABLED)
        }
    }

    private inline fun <reified T : Enum<T>> SharedPreferences.getEnum(
        key: String,
        defaultValue: T
    ): T {
        val name = getString(key, null)
        return T::class.java.enumConstants?.find { e -> e.name == name } ?: defaultValue
    }

    private inline fun <reified T : Enum<T>> SharedPreferences.Editor.putEnum(
        key: String,
        value: Enum<T>?
    ) {
        putString(key, value?.name)
    }


}
