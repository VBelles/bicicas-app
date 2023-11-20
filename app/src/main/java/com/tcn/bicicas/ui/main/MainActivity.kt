package com.tcn.bicicas.ui.main

import android.graphics.Color
import android.os.Build
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

        val insetsController =
            WindowCompat.getInsetsController(window, findViewById(android.R.id.content))
        val updateSystemUiTheme = { darkMode: Boolean, navigationBarContrastEnforced: Boolean ->
            if (Build.VERSION.SDK_INT > 23) {
                insetsController.isAppearanceLightStatusBars = !darkMode
            } else {
                val veil = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
                window.statusBarColor = if (darkMode) Color.TRANSPARENT else veil
            }
            if (Build.VERSION.SDK_INT < 26 || navigationBarContrastEnforced) {
                val veil = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
                window.navigationBarColor = if (darkMode) Color.TRANSPARENT else veil
            } else {
                insetsController.isAppearanceLightNavigationBars = !darkMode
            }
        }

        setContent { MainScreen(mapState, hideSplashScreen, updateSystemUiTheme) }
    }
}

