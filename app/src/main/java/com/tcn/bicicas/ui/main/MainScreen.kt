package com.tcn.bicicas.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.tcn.bicicas.R
import com.tcn.bicicas.data.model.Settings
import com.tcn.bicicas.ui.pin.PinScreen
import com.tcn.bicicas.ui.stations.list.StationScreen
import com.tcn.bicicas.ui.stations.map.MapScreen
import com.tcn.bicicas.ui.theme.BarHeight
import com.tcn.bicicas.ui.theme.BarTonalElevation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class Screen(
    val icon: ImageVector,
    val content: @Composable (PaddingValues) -> Unit
)

@Composable
fun MainScreen(
    navigationType: Settings.NavigationType,
    initialScreen: Int,
    navigateToMapEvent: Flow<String>,
    onSettingsClick: () -> Unit
) {
    val screens = listOf(
        Screen(Icons.Rounded.Pin) { padding ->
            PinScreen(padding)
        },
        Screen(Icons.Rounded.List) { padding ->
            StationScreen(padding)
        },
        Screen(Icons.Rounded.Map) { padding ->
            MapScreen(padding)
        },
    )

    when (navigationType) {
        Settings.NavigationType.Tabs -> TabsScreen(
            screens = screens,
            initialScreen = initialScreen,
            navigateToMapEvent = navigateToMapEvent,
            onSettingsClick = onSettingsClick
        )
        Settings.NavigationType.BottomBar -> BottomBarScreen(
            screens = screens,
            initialScreen = initialScreen,
            navigateToMapEvent = navigateToMapEvent,
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

        val contentPadding =
            rememberInsetsPaddingValues(LocalWindowInsets.current.navigationBars, applyTop = false)
        HorizontalPager(count = screens.size, state = pagerState) { page ->
            screens[page].content(contentPadding)
        }
    }

}

@Composable
fun BottomBarScreen(
    screens: List<Screen>,
    initialScreen: Int,
    navigateToMapEvent: Flow<String>,
    onSettingsClick: () -> Unit
) {
    var selectedScreen by rememberSaveable { mutableStateOf(initialScreen) }

    val contentPadding = rememberInsetsPaddingValues(
        LocalWindowInsets.current.navigationBars,
        applyTop = false,
        applyBottom = false
    )

    LaunchedEffect(navigateToMapEvent) {
        navigateToMapEvent.collect {
            selectedScreen = Settings.Screen.Map.ordinal
        }
    }
    Column {
        ApplicationTopBar(onSettingsClick)
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            screens.forEachIndexed { index, screen ->
                AnimatedAlpha(selectedScreen == index) {
                    screen.content(contentPadding)
                }
            }
        }

        Surface(tonalElevation = BarTonalElevation, shadowElevation = 10.dp) {
            Column {
                Row(
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                        .selectableGroup()
                        .systemBarsPadding(bottom = false, top = false),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    screens.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            //label = { Text(screen.title) },
                            selected = selectedScreen == index,
                            onClick = { selectedScreen = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.navigationBarsHeight())
            }

        }
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
                .systemBarsPadding(bottom = false)
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

@Composable
fun AnimatedAlpha(visible: Boolean, content: @Composable BoxScope.() -> Unit) {
    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f)
    Box(
        modifier = Modifier
            .alpha(alpha)
            .zIndex(alpha - 1f),
        content = content,
    )
}