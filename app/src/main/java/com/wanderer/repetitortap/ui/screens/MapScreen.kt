@file:Suppress("DEPRECATION")

package com.wanderer.repetitortap.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.ColorUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.wanderer.repetitortap.data.MarkerEntity
import com.wanderer.repetitortap.ui.viewmodel.MapViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap

private const val MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val MARKER_ICON_SIZE_PX = 96

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    focusedMarker: MarkerEntity?,
    markerFocusRequestId: Int,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val markers by viewModel.markers.collectAsState()
    
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val fusedLocationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val iconFactory = remember(context) { IconFactory.getInstance(context.applicationContext) }
    val mapView = remember(context) {
        MapLibre.getInstance(context.applicationContext)
        MapView(context).apply {
            onCreate(Bundle())
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val lifecycle = lifecycleOwner.lifecycle
        var isStarted = false
        var isResumed = false
        var isDestroyed = false

        fun startMap() {
            if (!isStarted && !isDestroyed) {
                mapView.onStart()
                isStarted = true
            }
        }

        fun resumeMap() {
            if (!isResumed && !isDestroyed) {
                mapView.onResume()
                isResumed = true
            }
        }

        fun pauseMap() {
            if (isResumed && !isDestroyed) {
                mapView.onPause()
                isResumed = false
            }
        }

        fun stopMap() {
            if (isStarted && !isDestroyed) {
                mapView.onStop()
                isStarted = false
            }
        }

        fun destroyMap() {
            if (!isDestroyed) {
                pauseMap()
                stopMap()
                mapLibreMap?.clear()
                mapView.onDestroy()
                mapLibreMap = null
                isDestroyed = true
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> startMap()
                Lifecycle.Event.ON_RESUME -> resumeMap()
                Lifecycle.Event.ON_PAUSE -> pauseMap()
                Lifecycle.Event.ON_STOP -> stopMap()
                Lifecycle.Event.ON_DESTROY -> destroyMap()
                Lifecycle.Event.ON_CREATE,
                Lifecycle.Event.ON_ANY -> Unit
            }
        }

        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            startMap()
        }
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            resumeMap()
        }

        onDispose {
            lifecycle.removeObserver(observer)
            destroyMap()
        }
    }

    fun moveCameraTo(latitude: Double, longitude: Double, zoom: Double = 14.0) {
        mapLibreMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(latitude, longitude),
                zoom
            )
        )
    }

    fun centerOnGpsLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                moveCameraTo(location.latitude, location.longitude)
            } else {
                fusedLocationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { currentLocation ->
                        if (currentLocation != null) {
                            moveCameraTo(currentLocation.latitude, currentLocation.longitude)
                        }
                    }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            centerOnGpsLocation()
        }
    }

    fun requestOrCenterOnGpsLocation() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            centerOnGpsLocation()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            locationPermissionGranted = true
        }
    }

    LaunchedEffect(markerFocusRequestId, focusedMarker?.id, mapLibreMap) {
        val marker = focusedMarker
        if (markerFocusRequestId > 0 && marker != null && mapLibreMap != null) {
            moveCameraTo(marker.latitude, marker.longitude)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    getMapAsync { map ->
                        mapLibreMap = map
                        configureMapGestures(map)
                        map.setStyle(MAP_STYLE_URL) {
                            updateMarkersOnMap(map, iconFactory, markers)
                        }

                        map.addOnMapClickListener { latLng ->
                            viewModel.addMarker(latLng.latitude, latLng.longitude)
                            true
                        }
                    }
                }
            },
            update = {
                val map = mapLibreMap
                if (map != null) {
                    updateMarkersOnMap(map, iconFactory, markers)
                }
            }
        )

        FloatingActionButton(
            onClick = onMenuClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp, 48.dp, 16.dp, 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
        }

        SmallFloatingActionButton(
            onClick = { requestOrCenterOnGpsLocation() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 40.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = if (locationPermissionGranted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Center on GPS location")
        }
    }
}

private fun configureMapGestures(map: MapLibreMap) {
    map.uiSettings.apply {
        setAllGesturesEnabled(true)
        setScrollGesturesEnabled(true)
        setHorizontalScrollGesturesEnabled(true)
        setZoomGesturesEnabled(true)
        setDoubleTapGesturesEnabled(true)
        setQuickZoomGesturesEnabled(true)
        setFlingVelocityAnimationEnabled(true)
    }
}

@Suppress("DEPRECATION")
private fun updateMarkersOnMap(
    map: MapLibreMap,
    iconFactory: IconFactory,
    markers: List<com.wanderer.repetitortap.data.MarkerEntity>
) {
    map.clear()
    markers.forEach { marker ->
        map.addMarker(
            MarkerOptions()
                .position(LatLng(marker.latitude, marker.longitude))
                .title(marker.title)
                .icon(iconFactory.fromBitmap(createMarkerBitmap(marker.color.toInt())))
        )
    }
}

private fun createMarkerBitmap(color: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(MARKER_ICON_SIZE_PX, MARKER_ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = color
    }
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = android.graphics.Color.argb(64, 0, 0, 0)
    }
    val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = android.graphics.Color.WHITE
    }
    val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        this.color = ColorUtils.setAlphaComponent(android.graphics.Color.WHITE, 140)
    }

    canvas.drawOval(31f, 82f, 65f, 91f, shadowPaint)

    val pinPath = Path().apply {
        moveTo(48f, 88f)
        cubicTo(42f, 76f, 24f, 57f, 24f, 39f)
        cubicTo(24f, 25f, 35f, 14f, 48f, 14f)
        cubicTo(61f, 14f, 72f, 25f, 72f, 39f)
        cubicTo(72f, 57f, 54f, 76f, 48f, 88f)
        close()
    }

    canvas.drawPath(pinPath, fillPaint)
    canvas.drawPath(pinPath, highlightPaint)
    canvas.drawCircle(48f, 39f, 11f, centerPaint)

    return bitmap
}
