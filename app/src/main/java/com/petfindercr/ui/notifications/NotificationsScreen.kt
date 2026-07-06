package com.petfindercr.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petfindercr.data.model.Notificacion

private val BgColor = Color(0xFFF8F9FC)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)
private val Border = Color(0xFFE2E8F0)
private val Green = Color(0xFF22C55E)
private val Red = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = { IconButton(onClick = viewModel::cargar) { Icon(Icons.Default.Refresh, "Actualizar") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.solicitudes.isEmpty() && state.notificaciones.isEmpty() -> EmptyState()
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.solicitudes.isNotEmpty()) {
                        item { SectionLabel("Solicitudes de cambio de estado") }
                        items(state.solicitudes) { sol ->
                            SolicitudCard(
                                sol = sol,
                                procesando = state.procesando,
                                onAprobar = { viewModel.responder(sol, true) },
                                onRechazar = { viewModel.responder(sol, false) }
                            )
                        }
                    }
                    if (state.notificaciones.isNotEmpty()) {
                        item { SectionLabel("Actividad") }
                        items(state.notificaciones) { n ->
                            NotifCard(n = n, onClick = { if (!n.leida) viewModel.marcarLeida(n) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Muted)
}

@Composable
private fun SolicitudCard(
    sol: SolicitudUi,
    procesando: Boolean,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.SwapHoriz, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)) }
                Column(Modifier.weight(1f)) {
                    Text(sol.solicitanteNombre, fontWeight = FontWeight.Bold, color = Ink, fontSize = 15.sp)
                    Text("Quiere cambiar un reporte tuyo", fontSize = 12.sp, color = Muted)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("\"${sol.reporteTitulo}\"", fontWeight = FontWeight.SemiBold, color = Ink, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Nuevo estado:", fontSize = 13.sp, color = Muted)
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(sol.solicitud.estadoSolicitado, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onRechazar, enabled = !procesando,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Red.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Rechazar")
                }
                Button(
                    onClick = onAprobar, enabled = !procesando,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Aprobar")
                }
            }
        }
    }
}

@Composable
private fun NotifCard(n: Notificacion, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (n.leida) Color.White else Color(0xFFF5F3FF)),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Border)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
            Column(Modifier.weight(1f)) {
                Text(n.titulo, fontWeight = FontWeight.SemiBold, color = Ink, fontSize = 14.sp)
                Text(n.mensaje, fontSize = 13.sp, color = Muted)
            }
            if (!n.leida) Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(46.dp), tint = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(18.dp))
        Text("Sin notificaciones", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink)
        Spacer(Modifier.height(6.dp))
        Text("Aquí verás solicitudes de cambio de estado y avisos de tus reportes.",
            fontSize = 13.sp, color = Muted, textAlign = TextAlign.Center, lineHeight = 20.sp)
    }
}
