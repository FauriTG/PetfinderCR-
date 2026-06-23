package com.petfindercr.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.ui.components.EstadoBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: Long,
    onEdit: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val reporte = state.selectedReporte
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEstadoMenu by remember { mutableStateOf(false) }

    LaunchedEffect(reportId) { viewModel.loadReporte(reportId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Reporte") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (reporte != null) {
                        IconButton(onClick = { onEdit(reporte.id) }) { Icon(Icons.Default.Edit, "Editar") }
                        Box {
                            IconButton(onClick = { showEstadoMenu = true }) { Icon(Icons.Default.MoreVert, "Más opciones") }
                            DropdownMenu(expanded = showEstadoMenu, onDismissRequest = { showEstadoMenu = false }) {
                                Text("Cambiar estado", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                                EstadoReporte.entries.forEach { estado ->
                                    DropdownMenuItem(text = { Text(estado.name) }, onClick = {
                                        viewModel.updateEstado(reporte.id, estado)
                                        showEstadoMenu = false
                                    })
                                }
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = { showDeleteDialog = true; showEstadoMenu = false }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading || reporte == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            ) {
                // Images carousel
                reporte.imagenesReporte?.takeIf { it.isNotEmpty() }?.let { images ->
                    LazyRow {
                        items(images) { img ->
                            AsyncImage(
                                model = img.urlImagen, contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(240.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } ?: Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxSize()) {}
                    Icon(Icons.Default.Pets, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstadoBadge(reporte.estado)
                        reporte.tiposMascota?.nombre?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                    }

                    Text(reporte.titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                    reporte.descripcion?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

                    Divider()

                    DetailRow(icon = Icons.Default.Palette, label = "Color", value = reporte.color)
                    DetailRow(icon = Icons.Default.Pets, label = "Raza", value = reporte.raza)
                    DetailRow(icon = Icons.Default.LocationOn, label = "Dirección", value = reporte.direccion)
                    DetailRow(icon = Icons.Default.Person, label = "Reportado por", value = reporte.perfiles?.nombre)
                    DetailRow(icon = Icons.Default.Phone, label = "Teléfono", value = reporte.perfiles?.telefono)

                    if (reporte.recompensa && reporte.montoRecompensa != null) {
                        Divider()
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(Modifier.width(8.dp))
                                Text("Recompensa: ₡${reporte.montoRecompensa}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar reporte") },
            text = { Text("¿Estás seguro de que deseas eliminar este reporte? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; viewModel.deleteReporte(reportId, onBack) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
