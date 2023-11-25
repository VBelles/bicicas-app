package com.tcn.bicicas.stations

import com.tcn.bicicas.data.datasource.Store

class UpdateFavorite(
    private val favoriteStore: Store<List<String>>,
) {
    suspend operator fun invoke(id: String) {
        favoriteStore.update { ids -> if (ids.contains(id)) ids - id else ids + id }
    }
}