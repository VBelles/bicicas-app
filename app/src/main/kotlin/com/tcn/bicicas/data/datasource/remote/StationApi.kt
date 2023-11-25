package com.tcn.bicicas.data.datasource.remote

import com.tcn.bicicas.data.model.Station
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
private class StationsResponse(
    @SerialName("features") val features: List<Feature> = emptyList()
) {
    @Serializable
    data class Feature(
        @SerialName("geometry") val geometry: Geometry,
        @SerialName("properties") val properties: Properties,
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

private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "ES"))

suspend fun HttpClient.getStations(
    baseUrl: String,
    dateParser: (String) -> Long,
    isFavorite: (String) -> Boolean
): List<Station> {
    val response = get("$baseUrl/bench_status").body<StationsResponse>()
    return response.features.map { (geometry, properties) ->
        val (id, name) = properties.name.split(".", limit = 2)
            .map { value -> value.uppercase().trim() }
        Station(
            id = id,
            name = name,
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
            lastSeen = properties.lastSeen?.let { date -> dateFormatter.parse(date) } ?: Date(),
            online = properties.online == true,
            latitude = geometry.coordinates[0],
            longitude = geometry.coordinates[1],
        )
    }

}