package com.petfindercr.ui.report

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.utils.createImageUri

private val BgColor = Color(0xFFF8F9FC)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)
private val Border = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QuickReportScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var nota by remember { mutableStateOf("") }

    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Al abrir: marca como ENCONTRADA y captura la ubicación automáticamente
    LaunchedEffect(Unit) { viewModel.onEstadoChange(EstadoReporte.ENCONTRADA) }
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) viewModel.loadCurrentLocation(context)
        else locationPermissions.launchMultiplePermissionRequest()
    }
    LaunchedEffect(state.success) { if (state.success) { viewModel.clearSuccess(); onSuccess() } }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.addImageUri(it) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { viewModel.addImageUri(it) }
    }

    fun publicar() {
        val titulo = nota.trim().takeIf { it.isNotBlank() }?.take(50) ?: "Mascota encontrada"
        viewModel.onTituloChange(titulo)
        viewModel.onDescripcionChange(nota.trim())
        viewModel.createReporte(context)
    }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Reporte rápido", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Column {
                    Text("¿Viste una mascota en la calle?", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                    Text("Repórtalo en segundos para ayudar a reunirla con su familia.", fontSize = 12.sp, color = Muted)
                }
            }

            // 1. Foto
            StepLabel("1", "Toma o elige una foto")
            if (state.selectedImageUris.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.5.dp, Border, RoundedCornerShape(18.dp))
                        .clickable {
                            cameraUri = createImageUri(context)
                            cameraLauncher.launch(cameraUri!!)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Tomar foto", fontWeight = FontWeight.SemiBold, color = Ink)
                        Text("o elige de la galería abajo", fontSize = 12.sp, color = Muted)
                    }
                }
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Elegir de la galería")
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.selectedImageUris) { uri ->
                        Box(Modifier.size(120.dp)) {
                            AsyncImage(
                                model = uri, contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { viewModel.removeImageUri(uri) },
                                modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
                            ) { Icon(Icons.Default.Close, null, tint = Color.White) }
                        }
                    }
                    item {
                        Box(
                            modifier = Modifier.size(120.dp).clip(RoundedCornerShape(14.dp))
                                .background(Color.White).border(1.dp, Border, RoundedCornerShape(14.dp))
                                .clickable {
                                    cameraUri = createImageUri(context)
                                    cameraLauncher.launch(cameraUri!!)
                                },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary) }
                    }
                }
            }

            // 2. Tipo de animal (chips rápidos)
            if (state.tiposMascota.isNotEmpty()) {
                StepLabel("2", "¿Qué animal es? (opcional)")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.tiposMascota) { tipo ->
                        val selected = state.selectedTipoMascota?.id == tipo.id
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.onTipoMascotaChange(tipo) },
                            label = { Text(tipo.nombre) }
                        )
                    }
                }
            }

            // 3. Nota
            StepLabel("3", "Descripción (opcional)")
            OutlinedTextField(
                value = nota,
                onValueChange = { nota = it },
                placeholder = { Text("Ej: Perro café con collar rojo, cerca del parque central") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2, maxLines = 4
            )

            // Ubicación automática
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, Border)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text("Ubicación", fontSize = 12.sp, color = Muted)
                        val locText = when {
                            state.isLoadingLocation -> "Obteniendo ubicación…"
                            state.latitud != null && state.longitud != null ->
                                "📍 Capturada automáticamente"
                            else -> "Sin ubicación (toca para reintentar)"
                        }
                        Text(locText, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Ink)
                    }
                    if (state.isLoadingLocation) CircularProgressIndicator(Modifier.size(20.dp))
                    else IconButton(onClick = {
                        if (locationPermissions.allPermissionsGranted) viewModel.loadCurrentLocation(context)
                        else locationPermissions.launchMultiplePermissionRequest()
                    }) { Icon(Icons.Default.MyLocation, "Actualizar ubicación") }
                }
            }

            state.error?.let { error ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(error, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            // Botón publicar
            Button(
                onClick = { publicar() },
                enabled = !state.isSaving && state.selectedImageUris.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Send, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Publicar avistamiento", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
            if (state.selectedImageUris.isEmpty()) {
                Text(
                    "Agrega al menos una foto para publicar",
                    fontSize = 12.sp, color = Muted, modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StepLabel(numero: String, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) { Text(numero, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        Text(texto, fontWeight = FontWeight.SemiBold, color = Ink, fontSize = 15.sp)
    }
}
