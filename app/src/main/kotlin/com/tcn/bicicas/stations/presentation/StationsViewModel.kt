package com.tcn.bicicas.stations.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcn.bicicas.stations.domain.FetchStations
import com.tcn.bicicas.stations.domain.GetStations
import com.tcn.bicicas.stations.domain.UpdateFavorite
import com.tcn.bicicas.stations.domain.model.Station
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


@Immutable
data class StationsState(
    val stations: List<Station> = emptyList(),
    val lastUpdate: Long? = null,
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val navigateTo: String? = null,
    val hasError: Boolean = false,
)

class StationsViewModel(
    getStations: GetStations,
    private val fetchStations: FetchStations,
    private val updateFavorite: UpdateFavorite,
    val activeFlow: Flow<Boolean>,
) : ViewModel() {

    private val loadingState = MutableStateFlow(false)
    private val errorState = MutableStateFlow(false)
    private val navigateTo = MutableStateFlow<String?>(null)
    val state = combine(
        getStations(),
        loadingState,
        navigateTo,
        errorState
    ) { stations, loading, navigateTo, error ->
        StationsState(
            stations = stations.stations,
            lastUpdate = stations.stationsDate,
            isLoading = loading,
            hasLoaded = true,
            navigateTo = navigateTo,
            hasError = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), StationsState())

    init {
        // Refresh data every 30s while app is active
        viewModelScope.launch {
            activeFlow.collectLatest { active ->
                while (active && coroutineContext.isActive) {
                    fetchStations().onSuccess { errorState.value = false }
                    delay(30_000)
                }
            }
        }
    }

    fun onRefresh() {
        if (loadingState.value) return
        loadingState.value = true
        viewModelScope.launch {
            errorState.value = fetchStations().isFailure
            loadingState.value = false
        }
    }

    fun onFavoriteClicked(stationId: String) = viewModelScope.launch {
        updateFavorite(stationId)
    }

    fun onMapClicked(stationId: String) {
        navigateTo.value = stationId
    }

    fun onNavigatedToStation() {
        navigateTo.value = null
    }

    fun onErrorHandled() {
        errorState.value = false
    }
}