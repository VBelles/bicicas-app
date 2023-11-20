package com.tcn.bicicas.di

import com.tcn.bicicas.ui.AppLifecycleObserver
import org.koin.dsl.module

val appActiveModule = module {
    single { AppLifecycleObserver.activeFlow }
}