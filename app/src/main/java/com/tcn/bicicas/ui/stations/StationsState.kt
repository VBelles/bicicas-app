package com.tcn.bicicas.ui.stations

import androidx.compose.runtime.Immutable
import com.tcn.bicicas.data.model.Station

@Immutable
data class StationsState(
    val stations: List<Station> = emptyList(),
    val secondsSinceLastUpdate: Long? = null,
    val isLoading: Boolean = false,
)