package com.tcn.bicicas.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tcn.bicicas.R
import com.tcn.bicicas.data.model.Station
import com.tcn.bicicas.ui.pin.PinScreen
import com.tcn.bicicas.ui.stations.list.StationScreen
import com.tcn.bicicas.ui.stations.map.MapScreen
import com.tcn.bicicas.ui.stations.map.MapState
import com.tcn.bicicas.ui.theme.BarHeight
import com.tcn.bicicas.ui.theme.BarTonalElevation
import kotlinx.coroutines.flow.Flow

data class Screen(
    val icon: ImageVector,
    val route: String,
    val content: @Composable (PaddingValues) -> Unit
)

@Composable
fun MainScreen(
    initialScreen: Int,
    navigateToMapEvent: Flow<String>,
    mapState: MapState<Station>,
    onSettingsClick: () -> Unit
) {
    val screens = listOf(
        Screen(Icons.Rounded.Pin, "pin") { padding ->
            PinScreen(padding)
        },
        Screen(Icons.Rounded.List, "list") { padding ->
            StationScreen(padding)
        },
        Screen(Icons.Rounded.Map, "map") { padding ->
            MapScreen(padding, mapState)
        },
    )

    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
        .asPaddingValues()

    LaunchedEffect(navigateToMapEvent) {
        navigateToMapEvent.collect {
            println("navigate to ${screens[2].route}")
            navigate(navController, screens[2].route)
        }
    }

    Column {
        ApplicationTopBar(onSettingsClick)
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavHost(navController, screens[initialScreen].route) {
                for (screen in screens) {
                    composable(screen.route) { screen.content(contentPadding) }
                }
            }
        }

        Surface(
            tonalElevation = BarTonalElevation,
            shadowElevation = 10.dp,
        ) {
            println(currentEntry?.destination?.route)
            BottomAppBar(tonalElevation = 0.dp, modifier = Modifier.navigationBarsPadding()) {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        selected = currentEntry?.destination?.route == screen.route,
                        onClick = { navigate(navController, screen.route) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

private fun navigate(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(0) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun ApplicationTopBar(onSettingsClick: () -> Unit) {
    Surface(
        tonalElevation = BarTonalElevation,
        shadowElevation = BarTonalElevation
    ) {
        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .height(BarHeight)
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(24.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }
    }
}