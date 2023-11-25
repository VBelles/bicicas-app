package com.tcn.bicicas.stations

import com.tcn.bicicas.data.datasource.Store
import com.tcn.bicicas.data.model.Station
import com.tcn.bicicas.data.model.StationsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetStations(
    private val stationsStore: Store<StationsData>,
    private val favoriteStore: Store<List<String>>,
) {
    operator fun invoke(): Flow<StationsData> =
        combine(stationsStore.updates(), favoriteStore.updates()) { stationsData, favorite ->
            val stations = stationsData.stations
                .map { station -> station.copy(favorite = favorite.contains(station.id)) }
                .sortedWith(compareBy({ !it.favorite }, Station::id))
            stationsData.copy(stations = stations)
        }
}