package com.tcn.bicicas

import android.content.SharedPreferences
import app.cash.turbine.test
import com.tcn.bicicas.data.model.Settings
import com.tcn.bicicas.di.settingsModule
import com.tcn.bicicas.ui.settings.SettingsViewModel
import com.tcn.bicicas.utils.MainCoroutineRule
import com.tcn.bicicas.utils.MemorySharedPreferences
import com.tcn.bicicas.utils.get
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule
import kotlin.test.assertEquals

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

            settingsViewModel.onInitialScreenChanged(Settings.InitialScreen.Map.ordinal)
            assertEquals(Settings.InitialScreen.Map, awaitItem().initialScreen)

            settingsViewModel.onLastScreenChanged(2)
            assertEquals(2, awaitItem().lastScreen)

            settingsViewModel.onThemeChanged(Settings.Theme.Dark.ordinal)
            assertEquals(Settings.Theme.Dark, awaitItem().theme)

            settingsViewModel.onNavigationTypeChanged(Settings.NavigationType.Tabs.ordinal)
            assertEquals(Settings.NavigationType.Tabs, awaitItem().navigationType)

            settingsViewModel.onDynamicColorEnabled(true)
            assertEquals(true, awaitItem().dynamicColorEnabled)
        }
    }

}