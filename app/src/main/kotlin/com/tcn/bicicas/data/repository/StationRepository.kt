package com.tcn.bicicas.data.repository

import com.tcn.bicicas.data.Clock
import com.tcn.bicicas.data.datasource.local.LocalStore
import com.tcn.bicicas.data.datasource.remote.StationApi
import com.tcn.bicicas.data.model.Station
import com.tcn.bicicas.data.model.StationsData
import com.tcn.bicicas.data.resultOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext

class StationRepository(
    private val clock: Clock,
    private val stationApi: StationApi,
    private val favoriteStationsStore: LocalStore<Set<String>>,
) {

    private val favorites: MutableSet<String> = favoriteStationsStore.get().orEmpty().toMutableSet()
    private val _stationsData = MutableStateFlow(StationsData(emptyList(), null, clock.millis()))
    val stationsData: StateFlow<StationsData> = _stationsData.asStateFlow()

    suspend fun getStations(refresh: Boolean): Result<StationsData> {
        return resultOf {
            if (refresh) stationApi.getStations() else stationApi.getStationsFromCache()
        }.map { (response, stations) ->
            val date = response.headers().getDate("Date")?.time ?: clock.millis()
            StationsData(updateFavorites(stations.toList()), date, clock.millis())
        }.onSuccess { stations ->
            _stationsData.value = stations
        }.onFailure {
            _stationsData.update { data -> data.copy(updatedDate = clock.millis()) }
        }
    }


    suspend fun toggleFavorite(id: String): StationsData = withContext(Dispatchers.Default) {
        if (!favorites.remove(id)) {
            favorites.add(id)
        }
        favoriteStationsStore.save(favorites.toSet())
        _stationsData.updateAndGet { data -> data.copy(stations = updateFavorites(data.stations)) }
    }

    private fun updateFavorites(stations: Iterable<Station>): List<Station> {
        return stations
            .map { it.copy(favorite = favorites.contains(it.id)) }
            .sortedWith(compareBy({ !it.favorite }, Station::id))
    }

}