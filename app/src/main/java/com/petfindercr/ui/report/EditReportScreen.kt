package com.petfindercr.ui.report

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.utils.createImageUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReportScreen(
    reportId: Long,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var showEstadoDropdown by remember { mutableStateOf(false) }
    var showTipoDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(reportId) { viewModel.loadReporte(reportId) }
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
                title = { Text("Editar Reporte") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { viewModel.updateReporte(context) }, enabled = !state.isSaving) {
                        if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp))
                        else Icon(Icons.Default.Check, "Guardar")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(expanded = showEstadoDropdown, onExpandedChange = { showEstadoDropdown = it }) {
                    OutlinedTextField(
                        value = state.estado.name, onValueChange = {}, readOnly = true,
                        label = { Text("Estado") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showEstadoDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = showEstadoDropdown, onDismissRequest = { showEstadoDropdown = false }) {
                        EstadoReporte.entries.forEach { estado ->
                            DropdownMenuItem(text = { Text(estado.name) }, onClick = {
                                viewModel.onEstadoChange(estado); showEstadoDropdown = false
                            })
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = showTipoDropdown, onExpandedChange = { showTipoDropdown = it }) {
                    OutlinedTextField(
                        value = state.selectedTipoMascota?.nombre ?: "", onValueChange = {}, readOnly = true,
                        label = { Text("Tipo de mascota") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showTipoDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = showTipoDropdown, onDismissRequest = { showTipoDropdown = false }) {
                        state.tiposMascota.forEach { tipo ->
                            DropdownMenuItem(text = { Text(tipo.nombre) }, onClick = {
                                viewModel.onTipoMascotaChange(tipo); showTipoDropdown = false
                            })
                        }
                    }
                }

                OutlinedTextField(value = state.titulo, onValueChange = viewModel::onTituloChange, label = { Text("Título *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.descripcion, onValueChange = viewModel::onDescripcionChange, label = { Text("Descripción") }, maxLines = 4, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.color, onValueChange = viewModel::onColorChange, label = { Text("Color") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.raza, onValueChange = viewModel::onRazaChange, label = { Text("Raza") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.direccion, onValueChange = viewModel::onDireccionChange, label = { Text("Dirección") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.recompensa, onCheckedChange = viewModel::onRecompensaChange)
                    Text("Ofrece recompensa")
                }
                if (state.recompensa) {
                    OutlinedTextField(value = state.montoRecompensa, onValueChange = viewModel::onMontoRecompensaChange, label = { Text("Monto (₡)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }

                // Existing images
                state.selectedReporte?.imagenesReporte?.takeIf { it.isNotEmpty() }?.let { images ->
                    Text("Fotos actuales", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        images.forEach { img ->
                            AsyncImage(model = img.urlImagen, contentDescription = null, modifier = Modifier.size(80.dp), contentScale = ContentScale.Crop)
                        }
                    }
                }

                Text("Agregar fotos", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(4.dp)); Text("Galería")
                    }
                    OutlinedButton(onClick = {
                        cameraUri = createImageUri(context); cameraLauncher.launch(cameraUri!!)
                    }) {
                        Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(4.dp)); Text("Cámara")
                    }
                }

                state.error?.let { error ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(error, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
