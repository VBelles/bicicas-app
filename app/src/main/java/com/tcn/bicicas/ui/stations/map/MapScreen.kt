package com.tcn.bicicas.ui.stations.map

import android.Manifest
import android.content.Context
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GpsOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.maps.android.ktx.awaitAnimateCamera
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.model.markerOptions
import com.tcn.bicicas.R
import com.tcn.bicicas.data.model.Station
import com.tcn.bicicas.ui.components.plus
import com.tcn.bicicas.ui.stations.StationItem
import com.tcn.bicicas.ui.stations.StationItemVertical
import com.tcn.bicicas.ui.stations.StationsState
import com.tcn.bicicas.ui.stations.StationsViewModel
import com.tcn.bicicas.ui.theme.HighAvailabilityColor
import com.tcn.bicicas.ui.theme.LocalDarkTheme
import com.tcn.bicicas.ui.theme.LowAvailabilityColor
import com.tcn.bicicas.ui.theme.NoAvailabilityColor
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.getViewModel


@Composable
fun MapScreen(contentPadding: PaddingValues) {
    val viewModel: StationsViewModel = getViewModel()
    val stationsState by viewModel.state.collectAsState()

    MapScreen(
        stationsState = stationsState,
        activeFlow = viewModel.activeFlow,
        navigateToStationEvent = viewModel.navigateToMapEvent,
        contentPadding = contentPadding,
        onFavoriteClicked = viewModel::onFavoriteClicked
    )
}


@Composable
fun MapScreen(
    stationsState: StationsState,
    activeFlow: Flow<Boolean>,
    navigateToStationEvent: Flow<String>,
    contentPadding: PaddingValues,
    onFavoriteClicked: (String) -> Unit
) {
    StationsMap(
        stations = stationsState.stations,
        activeFlow = activeFlow,
        navigateToStationEvent = navigateToStationEvent,
        contentPadding = contentPadding,
        onFavoriteClicked = onFavoriteClicked,
    )
    //MapComponent()
}

@Composable
fun StationsMap(
    stations: List<Station>,
    activeFlow: Flow<Boolean>,
    navigateToStationEvent: Flow<String>,
    contentPadding: PaddingValues,
    onFavoriteClicked: (String) -> Unit
) {
    //val mapView = rememberMapViewWithLifecycle()

    var selectedStationId: String? by rememberSaveable { mutableStateOf(null) }

    val selectedStation: Station? = remember(stations, selectedStationId) {
        stations.find { station -> station.id == selectedStationId }
    }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val isPortrait = LocalConfiguration.current.orientation == ORIENTATION_PORTRAIT

    var mapPadding: PaddingValues by remember(contentPadding) { mutableStateOf(contentPadding) }

    Box(modifier = Modifier.fillMaxSize()) {
        /*MapViewContainer(mapView, mapPadding, stations, navigateToStationEvent) { id ->
            selectedStationId = id
        }*/


        var locationActive by remember { mutableStateOf(false) }
        MapContent(mapPadding, locationActive, stations, navigateToStationEvent) { id ->
            selectedStationId = id
        }
        LocationPermissionButton(
            activeFlow = activeFlow,
            modifier = Modifier
                .padding(mapPadding)
                .align(Alignment.TopEnd),
            onLocationActive = { active -> locationActive = active }
        )

        if (isPortrait) {
            PortraitStationDetails(
                selectedStation = selectedStation,
                onFavoriteClicked = onFavoriteClicked,
                modifier = Modifier
                    .padding(contentPadding)
                    .onSizeChanged { size ->
                        mapPadding = contentPadding.plus(
                            PaddingValues(bottom = with(density) { size.height.toDp() }),
                            layoutDirection
                        )
                    }
                    .align(Alignment.BottomCenter),

                )
        } else {
            LandscapeStationDetails(
                selectedStation = selectedStation,
                onFavoriteClicked = onFavoriteClicked,
                modifier = Modifier
                    .padding(contentPadding)
                    .onSizeChanged { size ->
                        mapPadding = contentPadding.plus(
                            PaddingValues(end = with(density) { size.width.toDp() }),
                            layoutDirection
                        )
                    }
                    .align(Alignment.TopEnd),
            )
        }
    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionButton(
    activeFlow: Flow<Boolean>,
    modifier: Modifier = Modifier,
    onLocationActive: (Boolean) -> Unit
) {
    // Request location permission
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    if (!permissionState.status.isGranted) {
        Box(modifier = modifier
            .padding(12.dp)
            .size(40.dp)
            .background(Color.White.copy(alpha = 0.85f))
            .clip(RoundedCornerShape(3.dp))
            .clickable { permissionState.launchPermissionRequest() }
        ) {
            Icon(
                imageVector = Icons.Rounded.GpsOff,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center)
            )
        }
    }

    // When permission is granted and app is active to avoid problems with collecting location on
    // background
    LaunchedEffect(permissionState.status.isGranted, activeFlow) {
        activeFlow.collect { active ->
            onLocationActive(permissionState.status.isGranted && active)
        }
    }
}

@Composable
private fun PortraitStationDetails(
    selectedStation: Station?,
    modifier: Modifier,
    onFavoriteClicked: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    AnimatedVisibility(
        visible = selectedStation != null,
        enter = slideInVertically(tween()) { it },
        exit = slideOutVertically(tween()) { it },
        modifier = modifier
    ) {
        val station = selectedStation ?: return@AnimatedVisibility
        StationItem(
            station = station,
            modifier = Modifier.padding(12.dp),
            opacity = 0.7f,
            expanded = expanded,
            onClick = { expanded = !expanded },
            onMapClicked = null,
            onFavoriteClicked = { onFavoriteClicked(station.id) },
        )
    }
}

@Composable
private fun LandscapeStationDetails(
    selectedStation: Station?,
    modifier: Modifier,
    onFavoriteClicked: (String) -> Unit
) {
    AnimatedVisibility(
        visible = selectedStation != null,
        enter = slideInHorizontally(tween()) { it },
        exit = slideOutHorizontally(tween()) { it },
        modifier = modifier
    ) {
        val station = selectedStation ?: return@AnimatedVisibility
        StationItemVertical(
            station = station,
            modifier = Modifier.padding(12.dp),
            onFavoriteClicked = { onFavoriteClicked(station.id) },
        )
    }
}

data class MarkerKey(val availabilityColor: Int, val favorite: Boolean, val hasElectric: Boolean)

@Composable
fun MapContent(
    contentPadding: PaddingValues,
    locationActive: Boolean,
    stations: List<Station>,
    navigateToStationEvent: Flow<String>,
    onStationSelected: (String?) -> Unit,
) {
    val cachedIcons = remember { hashMapOf<MarkerKey, BitmapDescriptor>() }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(39.9887138, -0.04635), 13.1f)
    }

    val markerStates = remember(stations) {
        stations.map { station -> MarkerState(LatLng(station.latitude, station.longitude)) }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = locationActive),
        uiSettings = MapUiSettings(),
        onMapClick = { onStationSelected(null) }
    ) {
        val context = LocalContext.current
        stations.forEachIndexed { index, station ->
            val hasElectric = station.electricBikesAvailable > 0
            val availabilityColor = when (station.bikesAvailable) {
                0 -> NoAvailabilityColor
                in 0..4 -> LowAvailabilityColor
                else -> HighAvailabilityColor
            }.toArgb()
            val markerKey = MarkerKey(availabilityColor, station.favorite, hasElectric)
            val icon = cachedIcons.getOrPut(markerKey) {
                BitmapDescriptorFactory.fromBitmap(buildMarkerBitmap(context, markerKey))
            }
            val snippetIcon = if (hasElectric) " ⚡" else ""
            val snippet = "${station.bikesAvailable} / ${station.anchors.size}$snippetIcon"
            val title = "${station.id} - ${station.name}"
            Marker(
                state = markerStates[index],
                icon = icon,
                title = title,
                snippet = snippet,
                onClick = {
                    onStationSelected(station.id)
                    false
                })
        }
    }

    LaunchedEffect(navigateToStationEvent, stations) {
        navigateToStationEvent.collect { stationId ->
            val stationIndex = stations.indexOfFirst { station -> station.id == stationId }
            val markerState = markerStates[stationIndex]
            markerState.showInfoWindow()
            onStationSelected(stationId)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(markerState.position, 14F)
            cameraPositionState.animate(cameraUpdate)
        }
    }
}


@Composable
fun MapViewContainer(
    map: MapView,
    padding: PaddingValues,
    stations: List<Station>,
    navigateToStationEvent: Flow<String>,
    onStationSelected: (String?) -> Unit
) {

    val context = LocalContext.current
    val markersAdapter = remember { MapMarkerAdapter<Station>() }
    val cachedIcons = remember { hashMapOf<MarkerKey, BitmapDescriptor>() }

    // Update markers given stations
    LaunchedEffect(map, stations) {
        val googleMap = map.awaitMap()

        markersAdapter.submit(googleMap, stations, Station::id) { station ->
            val hasElectric = station.electricBikesAvailable > 0
            markerOptions {
                title(station.name)
                position(LatLng(station.latitude, station.longitude))
                snippet(
                    "${station.bikesAvailable} / ${station.anchors.size}" +
                            if (hasElectric) " ⚡" else ""
                )

                val availabilityColor = when (station.bikesAvailable) {
                    0 -> NoAvailabilityColor
                    in 0..4 -> LowAvailabilityColor
                    else -> HighAvailabilityColor
                }.toArgb()

                val markerKey = MarkerKey(availabilityColor, station.favorite, hasElectric)
                icon(cachedIcons.getOrPut(markerKey) {
                    BitmapDescriptorFactory.fromBitmap(buildMarkerBitmap(context, markerKey))
                })
            }
        }

        googleMap.setOnMapClickListener { onStationSelected(null) }
        googleMap.setOnMarkerClickListener { marker ->
            onStationSelected(markersAdapter.getMarkerItem(marker)?.id)
            false
        }
    }

    // Adjust content map padding
    val topPadding = with(LocalDensity.current) { padding.calculateTopPadding().toPx() }
    val bottomPadding = with(LocalDensity.current) { padding.calculateBottomPadding().toPx() }
    val startPadding = with(LocalDensity.current) {
        padding.calculateStartPadding(LocalLayoutDirection.current).toPx()
    }
    val endPadding = with(LocalDensity.current) {
        padding.calculateEndPadding(LocalLayoutDirection.current).toPx()
    }
    LaunchedEffect(map, topPadding, bottomPadding, startPadding, endPadding) {
        val googleMap = map.awaitMap()
        googleMap.setPadding(
            startPadding.toInt(),
            topPadding.toInt(),
            endPadding.toInt(),
            bottomPadding.toInt()
        )
    }

    // Initial map configuration
    LaunchedEffect(map) {
        val googleMap = map.awaitMap()
        googleMap.uiSettings.setAllGesturesEnabled(true)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(39.9887138, -0.04635), 13.1f))
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
    }


    // Handle navigation to station
    LaunchedEffect(navigateToStationEvent) {
        val googleMap = map.awaitMap()
        navigateToStationEvent.collect { stationId ->
            val marker = markersAdapter.getMarker(stationId)
            if (marker != null) {
                marker.showInfoWindow()
                onStationSelected(stationId)
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.position, 14F)
                googleMap.awaitAnimateCamera(cameraUpdate, 500)
            }
        }
    }

    // Update map theme
    val darkTheme = LocalDarkTheme.current
    LaunchedEffect(map, darkTheme) {
        val googleMap = map.awaitMap()
        val mapStyle = when {
            darkTheme -> MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
            else -> null
        }
        googleMap.setMapStyle(mapStyle)
    }

    AndroidView({ map }) {}
}

private fun buildMarkerBitmap(context: Context, markerKey: MarkerKey): Bitmap {
    val drawable = ContextCompat.getDrawable(context, R.drawable.marker) as LayerDrawable
    drawable.findDrawableByLayerId(R.id.marker).setTint(markerKey.availabilityColor)
    drawable.findDrawableByLayerId(R.id.fav).alpha = if (markerKey.favorite) 255 else 0
    drawable.findDrawableByLayerId(R.id.electric).alpha = if (markerKey.hasElectric) 255 else 0
    return getBitmapFromVectorDrawable(drawable)
}

private fun getBitmapFromVectorDrawable(drawable: Drawable): Bitmap {
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}