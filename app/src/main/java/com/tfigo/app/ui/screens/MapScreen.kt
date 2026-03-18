package com.tfigo.app.ui.screens

import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tfigo.app.data.model.Coordinate
import com.tfigo.app.data.model.LocationResult
import com.tfigo.app.ui.components.StopTypeIcon
import com.tfigo.app.ui.components.formatStopType
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(
    mapStops: List<LocationResult>,
    isLoadingMapStops: Boolean,
    userLocation: Coordinate?,
    onLoadStops: (south: Double, west: Double, north: Double, east: Double) -> Unit,
    onStopSelected: (LocationResult) -> Unit
) {
    val context = LocalContext.current
    var selectedStop by remember { mutableStateOf<LocationResult?>(null) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    var lastLoadedBounds by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Debounced stop loading
    fun loadStopsIfNeeded(mapView: MapView) {
        val zoom = mapView.zoomLevelDouble
        if (zoom < 14.0) return // Only load at reasonable zoom

        val bounds = mapView.boundingBox
        val key = "%.3f,%.3f,%.3f,%.3f".format(
            bounds.latSouth, bounds.lonWest, bounds.latNorth, bounds.lonEast
        )
        if (key != lastLoadedBounds) {
            lastLoadedBounds = key
            onLoadStops(bounds.latSouth, bounds.lonWest, bounds.latNorth, bounds.lonEast)
        }
    }

    fun markerColor(type: String): Int = when {
        type.contains("TRAIN") -> 0xFFC5221F.toInt()
        type.contains("TRAM") -> 0xFF137333.toInt()
        type.contains("FERRY") -> 0xFF007B83.toInt()
        type.contains("COACH") -> 0xFF9334E6.toInt()
        else -> 0xFF1967D2.toInt()
    }

    fun createCircleDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setStroke(4, 0xFFFFFFFF.toInt())
            setSize(36, 36)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)

                    val startPoint = if (userLocation != null) {
                        GeoPoint(userLocation.latitude, userLocation.longitude)
                    } else {
                        GeoPoint(53.3498, -6.2603)
                    }
                    controller.setCenter(startPoint)

                    addOnFirstLayoutListener { _, _, _, _, _ ->
                        loadStopsIfNeeded(this)
                    }

                    addMapListener(object : org.osmdroid.events.MapListener {
                        override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                            loadStopsIfNeeded(this@apply)
                            return false
                        }
                        override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                            loadStopsIfNeeded(this@apply)
                            return false
                        }
                    })

                    mapViewRef.value = this
                }
            },
            update = { mapView ->
                // Only update markers, don't re-add listeners
                mapView.overlays.removeAll { it is Marker }

                // User location marker
                if (userLocation != null) {
                    val userDot = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(0xFF4285F4.toInt())
                        setStroke(6, 0x664285F4)
                        setSize(40, 40)
                    }
                    val userMarker = Marker(mapView).apply {
                        position = GeoPoint(userLocation.latitude, userLocation.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = "You are here"
                        icon = userDot
                    }
                    mapView.overlays.add(userMarker)
                }

                // Stop markers - limit to prevent flooding
                val stopsToShow = if (mapView.zoomLevelDouble < 14.0) {
                    emptyList()
                } else {
                    mapStops.take(150)
                }

                stopsToShow.forEach { stop ->
                    stop.coordinate?.let { coord ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(coord.latitude, coord.longitude)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            title = stop.name
                            snippet = formatStopType(stop.type)
                            icon = createCircleDrawable(markerColor(stop.type))
                            setOnMarkerClickListener { _, _ ->
                                selectedStop = stop
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                }

                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator
        if (isLoadingMapStops) {
            @Suppress("DEPRECATION")
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }

        // Locate me FAB
        FloatingActionButton(
            onClick = {
                val loc = userLocation
                if (loc != null) {
                    mapViewRef.value?.controller?.animateTo(
                        GeoPoint(loc.latitude, loc.longitude), 16.0, 500L
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "My location")
        }

        // Bottom card for selected stop
        selectedStop?.let { stop ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 80.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                ListItem(
                    headlineContent = {
                        Text(stop.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    supportingContent = {
                        Text(
                            buildString {
                                stop.shortCode?.let { append("Stop $it · ") }
                                append(formatStopType(stop.type))
                            }
                        )
                    },
                    leadingContent = { StopTypeIcon(stop.type) },
                    trailingContent = {
                        FilledTonalButton(onClick = {
                            selectedStop = null
                            onStopSelected(stop)
                        }) {
                            Text("View")
                        }
                    },
                    modifier = Modifier.clickable {
                        selectedStop = null
                        onStopSelected(stop)
                    }
                )
            }
        }
    }
}
