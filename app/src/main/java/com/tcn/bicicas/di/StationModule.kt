package com.tcn.bicicas.di

import com.tcn.bicicas.BuildConfig
import com.tcn.bicicas.data.datasource.local.FavoriteStationsStore
import com.tcn.bicicas.data.repository.StationRepository
import com.tcn.bicicas.ui.stations.StationsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create

fun stationModule(baseUrl: String = BuildConfig.STATUS_ENDPOINT) = module {
    single {
        StationRepository(
            clock = get(),
            stationApi = get<Retrofit.Builder>().baseUrl(baseUrl).build().create(),
            favoriteStationsStore = FavoriteStationsStore(get())
        )
    }

    viewModel { StationsViewModel(get(), get(), get()) }
}