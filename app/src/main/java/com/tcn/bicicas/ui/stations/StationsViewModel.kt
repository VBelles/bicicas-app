package com.tcn.bicicas.ui.stations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcn.bicicas.data.Clock
import com.tcn.bicicas.data.repository.StationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class StationsViewModel(
    private val clock: Clock,
    private val stationRepository: StationRepository,
    val activeFlow: Flow<Boolean>,
) : ViewModel() {

    private val _state = MutableStateFlow(StationsState())
    val state: StateFlow<StationsState> = _state.asStateFlow()
    private val _errorEvent = MutableSharedFlow<Unit>()
    val errorEvent: Flow<Unit> = _errorEvent.asSharedFlow()

    init {
        // Refresh data every 30s while app is active
        viewModelScope.launch {
            stationRepository.getStations(false)
            _state.update { state -> state.copy(hasLoaded = true) }
            activeFlow.collectLatest { active ->
                while (active) {
                    refresh(false)
                    delay(30_000)
                }
            }
        }

        // Update view state given stations data
        stationRepository.stationsData.onEach { stationsData ->
            _state.update { state ->
                state.copy(
                    stations = stationsData.stations,
                    secondsSinceLastUpdate = stationsData.stationsDate?.let { (clock.millis() - it) / 1000 },
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onRefresh() {
        refresh(true)
    }

    fun onFavoriteClicked(stationId: String) = viewModelScope.launch {
        stationRepository.toggleFavorite(stationId)
    }

    fun onMapClicked(stationId: String) {
        _state.update { state -> state.copy(navigateTo = stationId) }
    }

    fun onNavigatedToStation() {
        _state.update { state -> state.copy(navigateTo = null) }
    }

    private fun refresh(fromUser: Boolean) = viewModelScope.launch {
        _state.update { state -> state.copy(isLoading = fromUser) }
        val result = stationRepository.getStations(true)
        _state.update { state -> state.copy(isLoading = false) }
        result.onFailure {
            if (fromUser) {
                _errorEvent.emit(Unit)
            }
        }
    }


}