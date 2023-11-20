package com.tcn.bicicas.di

import com.tcn.bicicas.BuildConfig
import com.tcn.bicicas.data.datasource.local.TwoFactorAuthStore
import com.tcn.bicicas.data.repository.PinRepository
import com.tcn.bicicas.ui.pin.PinViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create

fun pinModule(baseUrl: String = BuildConfig.OAUTH_ENDPOINT) = module {
    single {
        PinRepository(
            secretApi = get<Retrofit.Builder>().baseUrl(baseUrl).build()
                .create(),
            twoFactorStore = TwoFactorAuthStore(get(), BuildConfig.ENCRYPT_PASSWORD.toCharArray()),
            clientId = BuildConfig.OAUTH_CLIENT_ID,
            clientSecret = BuildConfig.OAUTH_CLIENT_SECRET,
        )
    }

    viewModel { PinViewModel(get(), get()) }

}