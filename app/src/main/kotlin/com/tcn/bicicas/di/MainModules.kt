package com.tcn.bicicas.di

val mainModules = listOf(
    clockModule,
    networkModule,
    appActiveModule,
    preferencesModule,
    settingsModule,
    pinModule(),
    stationModule()
)