package com.petfindercr.ui.report

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.utils.createImageUri

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreateReportScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var showTipoDropdown by remember { mutableStateOf(false) }
    var showEstadoDropdown by remember { mutableStateOf(false) }
    var sinRaza by remember { mutableStateOf(false) }

    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Al abrir la pantalla: pide permiso y captura la ubicación actual automáticamente
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.loadCurrentLocation(context)
        } else {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(state.success) { if (state.success) { viewModel.clearSuccess(); onSuccess() } }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { viewModel.addImageUri(it) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { viewModel.addImageUri(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Reporte") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { viewModel.createReporte(context) }, enabled = !state.isSaving) {
                        if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp))
                        else Icon(Icons.Default.Check, "Guardar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Estado
            ExposedDropdownMenuBox(expanded = showEstadoDropdown, onExpandedChange = { showEstadoDropdown = it }) {
                OutlinedTextField(
                    value = state.estado.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showEstadoDropdown) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = showEstadoDropdown, onDismissRequest = { showEstadoDropdown = false }) {
                    EstadoReporte.entries.forEach { estado ->
                        DropdownMenuItem(text = { Text(estado.name) }, onClick = {
                            viewModel.onEstadoChange(estado)
                            showEstadoDropdown = false
                        })
                    }
                }
            }

            // Tipo de mascota
            ExposedDropdownMenuBox(expanded = showTipoDropdown, onExpandedChange = { showTipoDropdown = it }) {
                OutlinedTextField(
                    value = state.selectedTipoMascota?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de mascota") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showTipoDropdown) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = showTipoDropdown, onDismissRequest = { showTipoDropdown = false }) {
                    state.tiposMascota.forEach { tipo ->
                        DropdownMenuItem(text = { Text(tipo.nombre) }, onClick = {
                            viewModel.onTipoMascotaChange(tipo)
                            showTipoDropdown = false
                        })
                    }
                }
            }

            OutlinedTextField(value = state.titulo, onValueChange = viewModel::onTituloChange,
                label = { Text("Título *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = state.descripcion, onValueChange = viewModel::onDescripcionChange,
                label = { Text("Descripción") }, maxLines = 4, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = state.color, onValueChange = viewModel::onColorChange,
                label = { Text("Color") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = state.raza, onValueChange = viewModel::onRazaChange,
                label = { Text("Raza") }, singleLine = true, enabled = !sinRaza,
                modifier = Modifier.fillMaxWidth())

            // Opción "Sin raza"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        sinRaza = !sinRaza
                        viewModel.onRazaChange(if (sinRaza) "Sin raza" else "")
                    }
            ) {
                Checkbox(
                    checked = sinRaza,
                    onCheckedChange = {
                        sinRaza = it
                        viewModel.onRazaChange(if (it) "Sin raza" else "")
                    }
                )
                Text("Sin raza / mestizo")
            }

            OutlinedTextField(value = state.direccion, onValueChange = viewModel::onDireccionChange,
                label = { Text("Dirección / Zona") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            // Ubicación actual (se captura automáticamente al abrir)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ubicación del reporte", style = MaterialTheme.typography.labelMedium)
                        val locText = when {
                            state.isLoadingLocation -> "Obteniendo ubicación…"
                            state.latitud != null && state.longitud != null ->
                                "📍 %.5f, %.5f".format(state.latitud, state.longitud)
                            else -> "Sin ubicación"
                        }
                        Text(locText, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (state.isLoadingLocation) {
                        CircularProgressIndicator(Modifier.size(20.dp))
                    } else {
                        IconButton(onClick = {
                            if (locationPermissions.allPermissionsGranted) viewModel.loadCurrentLocation(context)
                            else locationPermissions.launchMultiplePermissionRequest()
                        }) {
                            Icon(Icons.Default.MyLocation, "Actualizar ubicación")
                        }
                    }
                }
            }

            // Recompensa
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.recompensa, onCheckedChange = viewModel::onRecompensaChange)
                Text("Ofrece recompensa")
            }
            if (state.recompensa) {
                OutlinedTextField(
                    value = state.montoRecompensa, onValueChange = viewModel::onMontoRecompensaChange,
                    label = { Text("Monto (₡)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }

            // Imágenes
            Text("Fotos", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.PhotoLibrary, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Galería")
                }
                OutlinedButton(onClick = {
                    cameraUri = createImageUri(context)
                    cameraLauncher.launch(cameraUri!!)
                }) {
                    Icon(Icons.Default.CameraAlt, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Cámara")
                }
            }
            if (state.selectedImageUris.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.selectedImageUris) { uri ->
                        Box(Modifier.size(80.dp)) {
                            AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            IconButton(onClick = { viewModel.removeImageUri(uri) }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp)) {
                                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            state.error?.let { error ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(error, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Botón principal: Enviar reporte
            Button(
                onClick = { viewModel.createReporte(context) },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Send, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Enviar reporte", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
