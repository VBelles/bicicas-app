package com.tcn.bicicas.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcn.bicicas.common.Store
import com.tcn.bicicas.settings.domain.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val store: Store<Settings>,
) : ViewModel() {

    val state: StateFlow<Settings> =
        store.updates().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Settings())

    fun onInitialScreenChanged(option: Int) =
        update { copy(initialScreen = Settings.InitialScreen.entries[option]) }

    fun onLastScreenChanged(screen: Int) =
        update { copy(lastScreen = screen) }

    fun onNavigationTypeChanged(option: Int) =
        update { copy(navigationType = Settings.NavigationType.entries[option]) }

    fun onThemeChanged(option: Int) =
        update { copy(theme = Settings.Theme.entries[option]) }

    fun onDynamicColorEnabled(enabled: Boolean) =
        update { copy(dynamicColorEnabled = enabled) }

    private fun update(transform: Settings.() -> Settings) {
        viewModelScope.launch { store.update(transform) }
    }
}