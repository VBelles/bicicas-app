package com.tcn.bicicas.stations

import com.tcn.bicicas.data.datasource.Store
import com.tcn.bicicas.data.datasource.remote.getStations
import com.tcn.bicicas.data.model.StationsData
import io.ktor.client.HttpClient

class FetchStations(
    private val stationsStore: Store<StationsData>,
    private val favoriteStore: Store<List<String>>,
    private val httpClient: HttpClient,
    private val dateParser: (String) -> Long,
    private val epochInMillis: () -> Long,
    private val baseUrl: String,
) {
    suspend operator fun invoke() {
        val favorites = favoriteStore.get()
        runCatching {
            httpClient.getStations(baseUrl, dateParser, favorites::contains)
        }.onSuccess { stations ->
            stationsStore.update { StationsData(stations, epochInMillis(), epochInMillis()) }
        }.onFailure {
            stationsStore.update { data -> data.copy(updatedDate = epochInMillis()) }
        }
    }
}