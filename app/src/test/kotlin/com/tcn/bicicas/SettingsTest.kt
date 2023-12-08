package com.tcn.bicicas

import app.cash.turbine.test
import com.tcn.bicicas.common.StoreManager
import com.tcn.bicicas.settings.SettingsModuleImpl
import com.tcn.bicicas.settings.domain.Settings
import com.tcn.bicicas.settings.presentation.SettingsViewModel
import com.tcn.bicicas.utils.MainCoroutineRule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.fakefilesystem.FakeFileSystem
import org.junit.Rule
import org.junit.Test

class SettingsTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private fun settingsModule(): SettingsModuleImpl = SettingsModuleImpl {
        StoreManager(mainCoroutineRule.dispatcher, "", { 0 }, FakeFileSystem())
    }

    @Test
    fun when_settings_are_updated_then_state_is_updated() = runTest {
        val settingsViewModel: SettingsViewModel = settingsModule().settingsViewModel
        settingsViewModel.state.test {
            assertEquals(Settings(), awaitItem())// Default settings state

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