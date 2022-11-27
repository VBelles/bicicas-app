package com.tcn.bicicas.data.model

import androidx.compose.runtime.Immutable
import java.util.Date


@Immutable
data class Station(
    val id: String,
    val name: String,
    val bikesAvailable: Int,
    val electricBikesAvailable: Int,
    val incidents: Int,
    val anchors: List<Anchor>,
    val online: Boolean,
    val lastSeen: Date,
    val latitude: Double,
    val longitude: Double,
    val favorite: Boolean,
) {
    @Immutable
    data class Anchor(
        val number: Int,
        val bicycleId: Int?,
        val isElectric: Boolean,
        val hasIncident: Boolean
    )

}