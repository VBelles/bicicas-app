package com.tcn.bicicas.data.repository

import com.tcn.bicicas.data.datasource.local.LocalStore
import com.tcn.bicicas.data.model.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsRepository(private val localStore: LocalStore<Settings>) {

    private val _settingsState = MutableStateFlow(localStore.get() ?: Settings())
    val settingsState: StateFlow<Settings> = _settingsState.asStateFlow()

    fun update(settings: Settings) {
        localStore.save(settings)
        _settingsState.update { settings }
    }

}
