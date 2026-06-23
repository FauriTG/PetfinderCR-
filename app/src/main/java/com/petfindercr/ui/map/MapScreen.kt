package com.petfindercr.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.petfindercr.data.model.EstadoReporte

// Default: San José, Costa Rica
private val DEFAULT_CR_LOCATION = LatLng(9.9281, -84.0907)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_CR_LOCATION, 10f)
    }

    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.loadNearbyReportes(context)
        } else {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(state.currentLocation) {
        state.currentLocation?.let { loc ->
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(loc, 13f))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Reportes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { viewModel.loadNearbyReportes(context) }) {
                        Icon(Icons.Default.MyLocation, "Mi ubicación")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermissions.allPermissionsGranted),
                uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = true)
            ) {
                state.reportes.forEach { reporte ->
                    val lat = reporte.latitud ?: return@forEach
                    val lon = reporte.longitud ?: return@forEach
                    val color = when (reporte.estado) {
                        EstadoReporte.PERDIDA.name -> BitmapDescriptorFactory.HUE_RED
                        EstadoReporte.ENCONTRADA.name -> BitmapDescriptorFactory.HUE_GREEN
                        EstadoReporte.RECUPERADA.name -> BitmapDescriptorFactory.HUE_BLUE
                        else -> BitmapDescriptorFactory.HUE_ORANGE
                    }
                    Marker(
                        state = MarkerState(position = LatLng(lat, lon)),
                        title = reporte.titulo,
                        snippet = reporte.estado,
                        icon = BitmapDescriptorFactory.defaultMarker(color),
                        onClick = { onNavigateToDetail(reporte.id); true }
                    )
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Legend
            Card(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Leyenda", style = MaterialTheme.typography.labelMedium)
                    LegendItem(color = MaterialTheme.colorScheme.error, label = "Perdida")
                    LegendItem(color = MaterialTheme.colorScheme.tertiary, label = "Encontrada")
                    LegendItem(color = MaterialTheme.colorScheme.primary, label = "Recuperada")
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(color = color, modifier = Modifier.size(12.dp), shape = MaterialTheme.shapes.small) {}
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
