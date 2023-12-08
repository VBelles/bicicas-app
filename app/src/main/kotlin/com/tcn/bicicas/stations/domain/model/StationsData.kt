package com.tcn.bicicas.stations.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class StationsData(
    val stations: List<Station> = emptyList(),
    val stationsDate: Long? = null,
)


