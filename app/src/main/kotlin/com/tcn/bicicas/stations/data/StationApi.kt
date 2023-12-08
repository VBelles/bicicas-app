package com.tcn.bicicas.stations.data

import com.tcn.bicicas.pin.data.result
import com.tcn.bicicas.stations.domain.model.Station
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private class StationsResponse(
    @SerialName("features") val features: List<Feature> = emptyList()
) {
    @Serializable
    data class Feature(
        @SerialName("properties") val properties: Properties,
        @SerialName("geometry") val geometry: Geometry,
    )

    @Serializable
    class Properties(
        @SerialName("name") val name: String,
        @SerialName("bikes_available") val bikesAvailable: Int,
        @SerialName("anchors") val anchors: List<Anchors> = emptyList(),
        @SerialName("last_seen") val lastSeen: String?,
        @SerialName("online") val online: Boolean?,
    )

    @Serializable
    class Anchors(
        @SerialName("number") val number: Int,
        @SerialName("bicycle") val bicycle: Int?,
        @SerialName("incidents") val incidents: List<String>?,
        @SerialName("is_electric") val isElectric: Boolean?,
    )

    @Serializable
    class Geometry(@SerialName("coordinates") val coordinates: List<Double> = emptyList())
}

suspend fun HttpClient.getStations(baseUrl: String, dateParser: (String) -> Long) =
    prepareGet("$baseUrl/bench_status").result { response: StationsResponse ->
        response.features.map { (properties, geometry) ->
            val (id, name) = properties.name.split(".", limit = 2)
            Station(
                id = id.trim(),
                name = name.trim().uppercase(),
                bikesAvailable = properties.bikesAvailable,
                electricBikesAvailable = properties.anchors.count {
                    it.incidents.isNullOrEmpty() && it.isElectric == true && it.bicycle != null
                },
                anchors = properties.anchors.map { anchor ->
                    Station.Anchor(
                        number = anchor.number,
                        bicycleId = anchor.bicycle,
                        isElectric = anchor.isElectric == true,
                        hasIncident = !anchor.incidents.isNullOrEmpty()
                    )
                },
                favorite = false,
                incidents = properties.anchors.count { !it.incidents.isNullOrEmpty() },
                lastSeen = properties.lastSeen?.let { date -> dateParser(date) },
                online = properties.online == true,
                latitude = geometry.coordinates[0],
                longitude = geometry.coordinates[1],
            )
        }
    }