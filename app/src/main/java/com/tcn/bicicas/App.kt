package com.tcn.bicicas

import android.app.Application
import com.tcn.bicicas.di.mainModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(mainModules)
        }
    }

}