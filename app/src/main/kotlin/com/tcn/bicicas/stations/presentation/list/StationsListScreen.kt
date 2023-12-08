package com.tcn.bicicas.stations.presentation.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tcn.bicicas.R
import com.tcn.bicicas.common.Clock
import com.tcn.bicicas.main.theme.Theme
import com.tcn.bicicas.stations.presentation.StationsState
import com.tcn.bicicas.stations.presentation.StationsViewModel
import com.tcn.bicicas.stations.presentation.components.StationItem
import com.tcn.bicicas.stations.presentation.components.plus
import com.tcn.bicicas.stations.presentation.stationsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes


@Composable
fun StationScreen(
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: StationsViewModel,
    clock: Clock,
) {
    val stationsState: StationsState by viewModel.state.collectAsState()
    StationsScreen(
        contentPadding = contentPadding,
        stationsState = stationsState,
        clock = clock,
        onRefresh = viewModel::onRefresh,
        onFavoriteClicked = viewModel::onFavoriteClicked,
        onMapClicked = viewModel::onMapClicked,
        onErrorHandled = viewModel::onErrorHandled,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    stationsState: StationsState,
    clock: Clock,
    onRefresh: () -> Unit,
    onFavoriteClicked: (String) -> Unit,
    onMapClicked: (String) -> Unit,
    onErrorHandled: () -> Unit,
) {
    val snackBarHostState = remember { SnackbarHostState() }

    val loadingErrorText = stringResource(R.string.stations_list_loading_error)
    val loadingErrorAction = stringResource(R.string.stations_list_loading_error_retry)

    LaunchedEffect(stationsState.hasError) {
        if (stationsState.hasError) {
            val result = snackBarHostState.showSnackbar(loadingErrorText, loadingErrorAction)
            if (result == SnackbarResult.ActionPerformed) {
                onRefresh()
            }
            onErrorHandled()
        }
    }

    val pullRefreshState = rememberPullToRefreshState()
    LaunchedEffect(stationsState.isLoading) {
        if (stationsState.isLoading) pullRefreshState.startRefresh() else pullRefreshState.endRefresh()
    }
    Box(
        Modifier
            .fillMaxSize()
            .nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        if (stationsState.stations.isEmpty()) {
            StationsListEmpty(contentPadding, stationsState.isLoading, onRefresh)
        } else {
            StationsListContent(
                contentPadding = contentPadding,
                stationsState = stationsState,
                clock = clock,
                onFavoriteClicked = onFavoriteClicked,
                onMapClicked = onMapClicked,
            )
        }
        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier
                .padding(contentPadding)
                .align(Alignment.BottomCenter)
        )
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullRefreshState,
        )
    }
}

@Composable
fun StationsListEmpty(
    padding: PaddingValues,
    isLoading: Boolean,
    onRefresh: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.weight(0.5f))
        Text(
            text = stringResource(R.string.stations_list_empty_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.sizeIn(maxWidth = 600.dp),
        )
        Spacer(modifier = Modifier.weight(0.5f))

        Box(Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isLoading) 0f else 1f)
            ) {
                Text(
                    text = stringResource(R.string.stations_list_empty_text),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.sizeIn(maxWidth = 600.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onRefresh,
                    content = { Text(stringResource(R.string.stations_list_empty_retry)) },
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StationsListContent(
    contentPadding: PaddingValues = PaddingValues(),
    stationsState: StationsState,
    clock: Clock,
    onFavoriteClicked: (String) -> Unit,
    onMapClicked: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    val topDivider = if (listState.canScrollBackward) DividerDefaults.color else Color.Transparent
    val botDivider = if (listState.canScrollForward) DividerDefaults.color else Color.Transparent
    val topDividerColor by animateColorAsState(topDivider, label = "Top divider")
    val botDividerColor by animateColorAsState(botDivider, label = "Bottom divider")

    LazyColumn(
        state = listState,
        modifier = Modifier.drawWithContent {
            drawContent()
            drawLine(topDividerColor, Offset.Zero, Offset(size.width, 0f))
            drawLine(botDividerColor, Offset(0f, size.height), Offset(size.width, size.height))
        },
        contentPadding = contentPadding + PaddingValues(vertical = 16.dp),
    ) {
        var favoriteTitleDisplayed = false
        var titleDisplayed = false

        item {
            StationsStatus(stationsState.lastUpdate, clock)
        }

        stationsState.stations.forEach { station ->
            if (station.favorite && !favoriteTitleDisplayed) {
                item(R.string.stations_title_benches_fav) {
                    StationsTitle(
                        text = stringResource(R.string.stations_title_benches_fav),
                        modifier = Modifier
                            .padding(8.dp)
                            .animateItemPlacement()
                    )
                }
                favoriteTitleDisplayed = true
            }
            if (!station.favorite && !titleDisplayed) {
                item(R.string.stations_title_benches) {
                    StationsTitle(
                        text = stringResource(R.string.stations_title_benches),
                        modifier = Modifier
                            .padding(8.dp)
                            .animateItemPlacement()
                    )
                }
                titleDisplayed = true
            }

            item(station.id) {
                var expanded by rememberSaveable { mutableStateOf(false) }
                StationItem(
                    station = station,
                    expanded = expanded,
                    onClick = { expanded = !expanded },
                    onMapClicked = { onMapClicked(station.id) },
                    onFavoriteClicked = { onFavoriteClicked(station.id) },
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .animateItemPlacement(),
                )
            }
        }
    }
}

@Composable
private fun StationsStatus(lastUpdated: Long?, clock: Clock) {
    val timeSinceUpdate = produceState<Duration?>(null, lastUpdated) {
        if (lastUpdated == null) return@produceState
        while (isActive) {
            value = (clock.millis() - lastUpdated).coerceAtLeast(0).milliseconds
            delay(10_000)
        }
    }.value
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        val updatedText = when (timeSinceUpdate) {
            null -> ""
            in 0.minutes..1.minutes -> stringResource(R.string.stations_list_status_updated)
            in 1.minutes..1.hours -> stringResource(
                R.string.stations_list_status_updated_minutes_ago, timeSinceUpdate.inWholeMinutes
            )

            in 1.hours..1.days -> stringResource(
                R.string.stations_list_status_updated_hours_ago, timeSinceUpdate.inWholeHours
            )

            else -> stringResource(
                R.string.stations_list_status_updated_days_ago, timeSinceUpdate.inWholeDays
            )
        }

        val icon = when {
            timeSinceUpdate != null && timeSinceUpdate < 1.minutes -> Icons.Rounded.Check
            else -> Icons.Rounded.Warning
        }
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(updatedText)
    }
}


@Composable
private fun StationsTitle(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier,
    )
}

@Preview
@Composable
fun StationsScreenPreview() {
    Theme {
        StationsScreen(
            stationsState = stationsState(),
            clock = { 0 },
            onRefresh = {},
            onFavoriteClicked = {},
            onMapClicked = {},
            onErrorHandled = {},
        )
    }
}

