package com.tcn.bicicas.ui.settings

import androidx.lifecycle.ViewModel
import com.tcn.bicicas.data.model.Settings
import com.tcn.bicicas.data.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settingsState: StateFlow<Settings> = settingsRepository.settingsState

    fun onInitialScreenChanged(option: Int) {
        settingsRepository.update(
            settingsState.value.copy(initialScreen = Settings.InitialScreen.entries[option])
        )
    }

    fun onLastScreenChanged(screen: Int) {
        settingsRepository.update(
            settingsState.value.copy(lastScreen = screen)
        )
    }

    fun onNavigationTypeChanged(option: Int) {
        settingsRepository.update(
            settingsState.value.copy(navigationType = Settings.NavigationType.entries[option])
        )
    }

    fun onThemeChanged(option: Int) {
        settingsRepository.update(settingsState.value.copy(theme = Settings.Theme.entries[option]))
    }

    fun onDynamicColorEnabled(enabled: Boolean) {
        settingsRepository.update(settingsState.value.copy(dynamicColorEnabled = enabled))
    }


}