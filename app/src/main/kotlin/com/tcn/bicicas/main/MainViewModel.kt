package com.tcn.bicicas.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcn.bicicas.common.Store
import com.tcn.bicicas.settings.domain.Settings
import com.tcn.bicicas.settings.domain.Settings.InitialScreen.Last
import com.tcn.bicicas.settings.domain.Settings.InitialScreen.Pin
import com.tcn.bicicas.stations.domain.GetStations
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class MainState(
    val isInitialDataLoaded: Boolean = false,
    val settings: Settings? = null,
)

class MainViewModel(
    private val settingsStore: Store<Settings>,
    getStations: GetStations,
) : ViewModel() {

    private val stations = getStations().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val state: StateFlow<MainState> = combine(settingsStore.updates(), stations) { settings, stations ->
        val initialScreen = settings.initialScreen
        val isInitialScreenPin = initialScreen == Pin || (initialScreen == Last && settings.lastScreen == Pin.ordinal)
        MainState(
            isInitialDataLoaded = isInitialScreenPin || stations != null,
            settings = settings
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainState())


    fun onLastScreenChanged(ordinal: Int) {
        viewModelScope.launch {
            settingsStore.update { settings -> settings.copy(lastScreen = ordinal) }
        }
    }
}