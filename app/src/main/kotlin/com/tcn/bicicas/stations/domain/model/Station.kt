package com.tcn.bicicas.stations.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable


@Immutable
@Serializable
data class Station(
    val id: String,
    val name: String,
    val bikesAvailable: Int,
    val electricBikesAvailable: Int,
    val incidents: Int,
    val anchors: List<Anchor>,
    val online: Boolean,
    val lastSeen: Long?,
    val latitude: Double,
    val longitude: Double,
    val favorite: Boolean,
) {
    @Immutable
    @Serializable
    data class Anchor(
        val number: Int,
        val bicycleId: Int?,
        val isElectric: Boolean,
        val hasIncident: Boolean
    )
}