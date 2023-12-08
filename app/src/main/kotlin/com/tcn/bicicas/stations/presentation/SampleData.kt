package com.tcn.bicicas.stations.presentation

import com.tcn.bicicas.stations.domain.model.Station
import kotlin.random.Random

fun stationsState() = StationsState(List(4) { station(it) })

fun station(id: Int) = Station(
    id = id.toString(),
    name = "Station $id",
    bikesAvailable = 10,
    electricBikesAvailable = 3,
    incidents = 2,
    anchors = List(15) { anchorId ->
        Station.Anchor(
            number = anchorId,
            bicycleId = if (Random.nextBoolean()) Random.nextInt(100, 2000) else 0,
            isElectric = listOf(false, false, true).random(),
            hasIncident = listOf(false, false, true).random()
        )
    },
    online = true,
    lastSeen = 0,
    latitude = 0.0,
    longitude = 0.0,
    favorite = id < 4
)
