package com.petfindercr.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.ui.components.EstadoBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: Long,
    onEdit: (Long) -> Unit,
    onOpenChat: (String, String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val reporte = state.selectedReporte
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEstadoMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val esMio = reporte?.usuarioId != null && reporte.usuarioId == viewModel.currentUserId

    LaunchedEffect(reportId) { viewModel.loadReporte(reportId) }
    LaunchedEffect(state.mensaje, state.error) {
        val text = state.mensaje ?: state.error
        if (text != null) { snackbarHostState.showSnackbar(text); viewModel.clearMensaje() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Reporte") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (reporte != null) {
                        if (esMio) IconButton(onClick = { onEdit(reporte.id) }) { Icon(Icons.Default.Edit, "Editar") }
                        Box {
                            IconButton(onClick = { showEstadoMenu = true }) { Icon(Icons.Default.MoreVert, "Más opciones") }
                            DropdownMenu(expanded = showEstadoMenu, onDismissRequest = { showEstadoMenu = false }) {
                                Text(
                                    if (esMio) "Cambiar estado" else "Solicitar cambio de estado",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                EstadoReporte.entries.forEach { estado ->
                                    DropdownMenuItem(text = { Text(estado.name) }, onClick = {
                                        if (esMio) viewModel.updateEstado(reporte.id, estado)
                                        else viewModel.solicitarCambioEstado(estado)
                                        showEstadoMenu = false
                                    })
                                }
                                if (esMio) {
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
                // Images carousel con fondo difuminado (para fotos que no llenan el marco)
                reporte.imagenesReporte?.takeIf { it.isNotEmpty() }?.let { images ->
                    LazyRow {
                        items(images) { img ->
                            Box(
                                modifier = Modifier.fillParentMaxWidth().height(300.dp)
                            ) {
                                // Fondo: la misma imagen difuminada rellenando los lados
                                AsyncImage(
                                    model = img.urlImagen, contentDescription = null,
                                    modifier = Modifier.matchParentSize().blur(28.dp),
                                    contentScale = ContentScale.Crop
                                )
                                // Velo suave para dar contraste y aire minimalista
                                Box(Modifier.matchParentSize().background(Color.White.copy(alpha = 0.25f)))
                                // Imagen nítida, completa y centrada
                                AsyncImage(
                                    model = img.urlImagen, contentDescription = null,
                                    modifier = Modifier.fillMaxSize().padding(20.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                } ?: Box(
                    modifier = Modifier.fillMaxWidth().height(220.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Pets, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
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

                    // ── Reportado por (tappable → perfil, con botón de mensaje) ──
                    reporte.usuarioId?.let { dueñoId ->
                        Divider()
                        ReporterRow(
                            nombre = reporte.perfiles?.nombre ?: "Usuario",
                            fotoPerfil = reporte.perfiles?.fotoPerfil,
                            esMio = dueñoId == viewModel.currentUserId,
                            onOpenProfile = { onOpenProfile(dueñoId) },
                            onMessage = { onOpenChat(dueñoId, reporte.perfiles?.nombre ?: "Usuario") }
                        )
                    }
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

                    // ── Mini-mapa con la ubicación del reporte ──
                    if (reporte.latitud != null && reporte.longitud != null) {
                        Divider()
                        Text("Ubicación", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        val punto = LatLng(reporte.latitud!!, reporte.longitud!!)
                        val cameraState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(punto, 15f)
                        }
                        Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(0.dp)) {
                            GoogleMap(
                                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)),
                                cameraPositionState = cameraState,
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = false,
                                    scrollGesturesEnabled = false,
                                    zoomGesturesEnabled = false,
                                    rotationGesturesEnabled = false,
                                    tiltGesturesEnabled = false
                                )
                            ) {
                                Marker(state = MarkerState(position = punto), title = reporte.titulo)
                            }
                        }
                    }

                    // ── Contactar a quien reportó (si no es tu propio reporte) ──
                    val dueñoId = reporte.usuarioId
                    if (dueñoId != null && dueñoId != viewModel.currentUserId) {
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = { onOpenChat(dueñoId, reporte.perfiles?.nombre ?: "Usuario") },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Chat, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Enviar mensaje", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
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
private fun ReporterRow(
    nombre: String,
    fotoPerfil: String?,
    esMio: Boolean,
    onOpenProfile: () -> Unit,
    onMessage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onOpenProfile)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar (o anónimo)
        Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            if (fotoPerfil != null) {
                AsyncImage(model = fotoPerfil, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text("Reportado por", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        // Ver perfil
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp).clickable(onClick = onOpenProfile)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AccountCircle, "Ver perfil", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
        }
        // Enviar mensaje (solo si no es mi reporte)
        if (!esMio) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp).clickable(onClick = onMessage)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Chat, "Enviar mensaje", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
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
