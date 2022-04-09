package com.tcn.bicicas.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.tcn.bicicas.R
import com.tcn.bicicas.data.model.Settings
import com.tcn.bicicas.data.model.Station
import com.tcn.bicicas.ui.pin.PinScreen
import com.tcn.bicicas.ui.stations.StationsViewModel
import com.tcn.bicicas.ui.stations.list.StationScreen
import com.tcn.bicicas.ui.stations.map.MapScreen
import com.tcn.bicicas.ui.stations.map.MapState
import com.tcn.bicicas.ui.theme.BarHeight
import com.tcn.bicicas.ui.theme.BarTonalElevation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

data class Screen(
    val icon: ImageVector,
    val route: String,
    val content: @Composable (PaddingValues) -> Unit
)

@Composable
fun MainScreen(
    navigationType: Settings.NavigationType,
    initialScreen: Int,
    mapState: MapState<Station>,
    onSettingsClick: () -> Unit
) {

    val stationsViewModel: StationsViewModel = getViewModel()

    val screens = remember {
        listOf(
            Screen(Icons.Rounded.Pin, "pin") { padding ->
                PinScreen(padding)
            },
            Screen(Icons.Rounded.List, "list") { padding ->
                StationScreen(padding, stationsViewModel)
            },
            Screen(Icons.Rounded.Map, "map") { padding ->
                MapScreen(padding, stationsViewModel, mapState)
            },
        )
    }

    when (navigationType) {
        Settings.NavigationType.Tabs -> TabsScreen(
            screens = screens,
            initialScreen = initialScreen,
            navigateToMapEvent = stationsViewModel.navigateToMapEvent,
            onSettingsClick = onSettingsClick
        )
        Settings.NavigationType.BottomBar -> BottomBarScreen(
            screens = screens,
            initialScreen = initialScreen,
            navigateToMapEvent = stationsViewModel.navigateToMapEvent,
            onSettingsClick = onSettingsClick
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabsScreen(
    screens: List<Screen>,
    initialScreen: Int,
    navigateToMapEvent: Flow<String>,
    onSettingsClick: () -> Unit
) {
    val pagerState = rememberPagerState(screens.lastIndex)
    val coroutineScope = rememberCoroutineScope()

    // Hack to compose all pages at first
    var initialized by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!initialized) {
            pagerState.scrollToPage(initialScreen)
            initialized = true
        }
    }

    LaunchedEffect(navigateToMapEvent) {
        navigateToMapEvent.collect { pagerState.animateScrollToPage(Settings.Screen.Map.ordinal) }
    }

    Column {
        ApplicationTopBar(onSettingsClick)
        Surface(tonalElevation = BarTonalElevation) {
            TabRow(
                selectedTabIndex = if (initialized) pagerState.currentPage else initialScreen,
                indicator = { tabPositions ->
                    if (initialized) {
                        TabRowDefaults.Indicator(
                            Modifier.pagerTabIndicatorOffset(
                                pagerState,
                                tabPositions
                            )
                        )
                    } else {
                        TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[initialScreen]))
                    }
                },
                backgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                screens.forEachIndexed { index, screen ->
                    Tab(
                        selected = index == pagerState.currentPage,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                        icon = { Icon(screen.icon, contentDescription = null) }
                    )
                }
            }
        }

        val contentPadding = WindowInsets.navigationBars.asPaddingValues()
        HorizontalPager(count = screens.size, state = pagerState) { page ->
            screens[page].content(contentPadding)
        }
    }

}


@Composable
fun BottomBarScreen(
    initialScreen: Int,
    screens: List<Screen>,
    navigateToMapEvent: Flow<String>,
    onSettingsClick: () -> Unit,
) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
        .asPaddingValues()

    LaunchedEffect(navigateToMapEvent) {
        navigateToMapEvent.collect { navigate(navController, "map") }
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
                    composable(screen.route) {
                        screen.content(contentPadding)
                    }
                }
            }
        }

        Surface(
            tonalElevation = BarTonalElevation,
            shadowElevation = 10.dp,
        ) {
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