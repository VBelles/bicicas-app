package com.tcn.bicicas.ui.stations.map

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.tcn.bicicas.R


data class MapState<T>(
    val mapView: MapView,
    val markerAdapter: MapMarkerAdapter<T>,
    val icons: MutableMap<Any, BitmapDescriptor>
) {
    companion object {
        fun <T> create(
            context: Context,
            lifecycle: Lifecycle,
            savedInstanceState: Bundle?,
            markerAdapter: MapMarkerAdapter<T>,
            mapId: Int,
        ): MapState<T> {
            val mapView = MapViewHorizontalScrollFixed(context).apply { id = mapId }
            lifecycle.addObserver(getMapLifecycleObserver(mapView, savedInstanceState))
            return MapState(mapView, markerAdapter, hashMapOf())
        }
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapViewHorizontalScrollFixed(context).apply { id = R.id.map }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        // Make MapView follow the current lifecycle
        val lifecycleObserver = getMapLifecycleObserver(mapView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

fun getMapLifecycleObserver(
    mapView: MapView,
    savedInstanceState: Bundle? = null
): LifecycleEventObserver = LifecycleEventObserver { _, event ->
    when (event) {
        Lifecycle.Event.ON_CREATE -> mapView.onCreate(savedInstanceState)
        Lifecycle.Event.ON_START -> mapView.onStart()
        Lifecycle.Event.ON_RESUME -> mapView.onResume()
        Lifecycle.Event.ON_PAUSE -> mapView.onPause()
        Lifecycle.Event.ON_STOP -> mapView.onStop()
        Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
        else -> throw IllegalStateException()
    }
}


class MapMarkerAdapter<T> {

    @Immutable
    data class MarkerData<T>(val marker: Marker, val icon: BitmapDescriptor?, val item: T)

    private var markers = hashMapOf<String, MarkerData<T>>()

    fun submit(
        googleMap: GoogleMap,
        items: List<T>,
        key: (T) -> String,
        onBind: (T) -> MarkerOptions
    ) {
        val currentMarkers = hashMapOf<String, MarkerData<T>>()

        // Update existent and create new markers
        items.forEach { item ->
            val itemKey = key(item)
            val markerData = markers[itemKey]
            if (markerData != null) {
                if (markerData.item != item) {
                    val options = onBind(item)

                    if (markerData.marker.title != options.title) {
                        markerData.marker.title = options.title
                    }

                    if (markerData.marker.position != options.position) {
                        markerData.marker.position = options.position
                    }

                    if (markerData.marker.snippet != options.snippet) {
                        markerData.marker.snippet = options.snippet
                    }

                    if (markerData.icon != options.icon) {
                        markerData.marker.setIcon(options.icon)
                    }
                    currentMarkers[itemKey] = MarkerData(markerData.marker, options.icon, item)
                } else {
                    currentMarkers[itemKey] = markerData
                }
            } else {
                val options = onBind(item)
                val newMarker = googleMap.addMarker(options)!!
                currentMarkers[itemKey] = MarkerData(newMarker, options.icon, item)
            }
        }

        // Remove unused markers
        markers.forEach { (key, markerData) ->
            if (!currentMarkers.containsKey(key)) {
                markerData.marker.remove()
            }
        }
        markers.clear()
        markers = currentMarkers
    }

    fun getMarker(itemId: String?): Marker? = markers[itemId]?.marker

    fun getMarkerItem(marker: Marker): T? = markers.values.find { it.marker.id == marker.id }?.item
}