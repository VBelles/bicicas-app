package com.tcn.bicicas

import android.content.SharedPreferences
import app.cash.turbine.test
import com.tcn.bicicas.data.model.Settings
import com.tcn.bicicas.di.settingsModule
import com.tcn.bicicas.ui.settings.SettingsViewModel
import com.tcn.bicicas.utils.MainCoroutineRule
import com.tcn.bicicas.utils.MemorySharedPreferences
import com.tcn.bicicas.utils.get
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module { single<SharedPreferences> { MemorySharedPreferences() } },
            settingsModule,
        )
    }


    @Test
    fun when_settings_are_updated_then_state_is_updated() = runBlocking {
        val settingsViewModel: SettingsViewModel = koinTestRule.get()
        settingsViewModel.settingsState.test {
            awaitItem() // Default settings state

            settingsViewModel.onInitialScreenChanged(Settings.Screen.Map.ordinal)
            assertEquals(Settings.Screen.Map, awaitItem().initialScreen)

            settingsViewModel.onThemeChanged(Settings.Theme.Dark.ordinal)
            assertEquals(Settings.Theme.Dark, awaitItem().theme)

            settingsViewModel.onDynamicColorEnabled(true)
            assertEquals(true, awaitItem().dynamicColorEnabled)
        }
    }

}