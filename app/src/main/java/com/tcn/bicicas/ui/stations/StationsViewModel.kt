package com.tcn.bicicas.ui.stations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcn.bicicas.data.Clock
import com.tcn.bicicas.data.repository.StationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
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

    private val _navigateToMapEvent = Channel<String>(Channel.BUFFERED)
    val navigateToMapEvent: Flow<String> = _navigateToMapEvent.receiveAsFlow()

    init {
        // Refresh data every 30s while app is active
        viewModelScope.launch {
            stationRepository.getStations(false)
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

    fun onMapClicked(stationId: String) = viewModelScope.launch {
        _navigateToMapEvent.send(stationId)
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