package com.tcn.bicicas.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.tcn.bicicas.R
import com.tcn.bicicas.data.model.Station
import com.tcn.bicicas.ui.stations.map.MapMarkerAdapter
import com.tcn.bicicas.ui.stations.map.MapState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        var keepOnScreen = true
        val hideSplashScreen = { keepOnScreen = false }
        installSplashScreen().setKeepOnScreenCondition { keepOnScreen }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        val mapState = MapState.create(
            this, lifecycle, savedInstanceState, MapMarkerAdapter<Station>(), R.id.map
        )

        setContent { MainScreen(mapState, hideSplashScreen) }
    }
}

