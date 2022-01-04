package com.tcn.bicicas.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class StationsData(
    val stations: List<Station>,
    val stationsDate: Long?,
    val updatedDate: Long?,
)


