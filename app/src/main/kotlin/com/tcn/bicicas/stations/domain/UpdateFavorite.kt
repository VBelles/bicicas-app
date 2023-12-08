package com.tcn.bicicas.stations.domain

import com.tcn.bicicas.common.Store

class UpdateFavorite(
    private val favoriteStore: Store<List<String>>,
) {
    suspend operator fun invoke(id: String) {
        favoriteStore.update { ids -> if (ids.contains(id)) ids - id else ids + id }
    }
}