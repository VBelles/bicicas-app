package com.tcn.bicicas.stations

import com.tcn.bicicas.common.Clock
import com.tcn.bicicas.common.StoreManager
import com.tcn.bicicas.stations.data.getStations
import com.tcn.bicicas.stations.domain.FetchStations
import com.tcn.bicicas.stations.domain.GetStations
import com.tcn.bicicas.stations.domain.UpdateFavorite
import com.tcn.bicicas.stations.domain.model.StationsData
import com.tcn.bicicas.stations.presentation.StationsViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

interface StationsModule {
    val stationsViewModel: StationsViewModel
}

class StationsModuleImpl(
    private val clock: Clock,
    private val storeManager: () -> StoreManager,
    private val httpClient: () -> HttpClient,
    private val stationsBaseUrl: String,
    private val dateParser: (String) -> Long,
    private val activeFlow: Flow<Boolean>,
) : StationsModule {
    override val stationsViewModel: StationsViewModel
        get() {
            val stationsStore = storeManager().getStore<StationsData>("stations", StationsData())
            val favoriteStore = storeManager().getListStore<String>("favorite")
            return StationsViewModel(
                getStations = GetStations(stationsStore, favoriteStore),
                fetchStations = FetchStations(
                    stationsStore = stationsStore,
                    getStations = { httpClient().getStations(stationsBaseUrl, dateParser) },
                    clock = clock,
                ),
                updateFavorite = UpdateFavorite(favoriteStore),
                activeFlow = activeFlow,
            )
        }

}

