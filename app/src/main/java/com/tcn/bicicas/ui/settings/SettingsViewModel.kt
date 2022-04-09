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
            settingsState.value.copy(initialScreen = Settings.Screen.values()[option])
        )
    }

    fun onThemeChanged(option: Int) {
        settingsRepository.update(settingsState.value.copy(theme = Settings.Theme.values()[option]))
    }

    fun onDynamicColorEnabled(enabled: Boolean) {
        settingsRepository.update(settingsState.value.copy(dynamicColorEnabled = enabled))
    }


}