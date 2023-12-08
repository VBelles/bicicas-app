package com.tcn.bicicas.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.tcn.bicicas.BuildConfig
import com.tcn.bicicas.R
import com.tcn.bicicas.pin.PinModuleImpl
import com.tcn.bicicas.settings.SettingsModuleImpl
import com.tcn.bicicas.stations.StationsModuleImpl
import com.tcn.bicicas.stations.domain.model.Station
import com.tcn.bicicas.stations.presentation.AppLifecycleObserver
import com.tcn.bicicas.stations.presentation.map.MapMarkerAdapter
import com.tcn.bicicas.stations.presentation.map.MapState
import java.text.SimpleDateFormat
import java.util.Locale

class Activity : ComponentActivity() {

    private val mainModule by lazy { MainModuleImpl(applicationContext.filesDir.absolutePath) }
    private val pinModule by lazy {
        PinModuleImpl(
            httpClient = mainModule::httpClient,
            storeManager = mainModule::storeManager,
            clock = mainModule.clock,
            oauthBaseUrl = BuildConfig.OAUTH_ENDPOINT,
            oauthClientId = BuildConfig.OAUTH_CLIENT_ID,
            oauthClientSecret = BuildConfig.OAUTH_CLIENT_SECRET
        )
    }
    private val settingsModule by lazy { SettingsModuleImpl(mainModule::storeManager) }

    private val dateParser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "ES"))
    private val stationsModule by lazy {
        StationsModuleImpl(
            clock = mainModule.clock,
            storeManager = mainModule::storeManager,
            httpClient = mainModule::httpClient,
            stationsBaseUrl = BuildConfig.STATUS_ENDPOINT,
            dateParser = { date -> dateParser.parse(date)?.time!! },
            activeFlow = AppLifecycleObserver.activeFlow,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var keepOnScreen = true
        val hideSplashScreen = { keepOnScreen = false }
        installSplashScreen().setKeepOnScreenCondition { keepOnScreen }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        val mapState = MapState.create<Station>(
            this, lifecycle, savedInstanceState, MapMarkerAdapter(), R.id.map
        )

        setContent {
            MainScreen(
                settingsViewModelProvider = settingsModule::settingsViewModel,
                pinViewModelProvider = pinModule::providePinViewModel,
                stationsViewModelProvider = stationsModule::stationsViewModel,
                clock = mainModule.clock,
                mapState = mapState,
                hideSplashScreen = hideSplashScreen,
            )
        }
    }
}

