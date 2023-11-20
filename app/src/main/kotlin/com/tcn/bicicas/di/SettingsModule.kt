package com.tcn.bicicas.di

import com.tcn.bicicas.data.datasource.local.SettingsStore
import com.tcn.bicicas.data.repository.SettingsRepository
import com.tcn.bicicas.ui.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    single { SettingsRepository(SettingsStore(get())) }
    viewModel { SettingsViewModel(get()) }
}