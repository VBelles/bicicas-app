package com.tcn.bicicas.stations.domain

import com.tcn.bicicas.common.Clock
import com.tcn.bicicas.common.Store
import com.tcn.bicicas.stations.domain.model.Station
import com.tcn.bicicas.stations.domain.model.StationsData

class FetchStations(
    private val stationsStore: Store<StationsData>,
    private val getStations: suspend () -> Result<List<Station>>,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Result<StationsData> =
        getStations()
            .map { stations -> StationsData(stations, clock.millis()) }
            .onSuccess { stationsData -> stationsStore.update { stationsData } }
}