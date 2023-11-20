package com.tcn.bicicas.di

import android.content.Context
import org.koin.dsl.module

val preferencesModule = module {
    single { get<Context>().getSharedPreferences("prefs", Context.MODE_PRIVATE) }
}