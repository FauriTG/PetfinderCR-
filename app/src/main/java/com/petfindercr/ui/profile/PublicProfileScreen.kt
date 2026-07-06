package com.petfindercr.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

private val BgColor = Color(0xFFF8F9FC)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)
private val Border = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    userId: String,
    onOpenChat: (String, String) -> Unit,
    onOpenReport: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: PublicProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(userId) { viewModel.cargar(userId) }
    val perfil = state.perfil

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            perfil == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error ?: "No se pudo cargar el perfil", color = Muted)
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar (o anónimo)
                Box(
                    modifier = Modifier.size(110.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (perfil.fotoPerfil != null) {
                        AsyncImage(model = perfil.fotoPerfil, contentDescription = "Foto", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Text(perfil.nombre.ifBlank { "Usuario anónimo" }, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)

                // Chips de sexo / procedencia
                if (!perfil.sexo.isNullOrBlank() || !perfil.procedencia.isNullOrBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        perfil.sexo?.takeIf { it.isNotBlank() }?.let {
                            InfoChip(if (it.startsWith("M", true) && it.contains("uj", true)) Icons.Default.Female else Icons.Default.Male, it)
                        }
                        perfil.procedencia?.takeIf { it.isNotBlank() }?.let { InfoChip(Icons.Default.Place, it) }
                    }
                }

                // Descripción
                if (!perfil.descripcion.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = BorderStroke(1.dp, Border)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Acerca de", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Muted)
                            Spacer(Modifier.height(6.dp))
                            Text(perfil.descripcion!!, fontSize = 14.sp, color = Ink, lineHeight = 20.sp)
                        }
                    }
                }

                // Contacto: teléfono
                if (!perfil.telefono.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = BorderStroke(1.dp, Border)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Teléfono", fontSize = 12.sp, color = Muted)
                                Text(perfil.telefono!!, fontSize = 15.sp, color = Ink, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // ── Estadísticas de reportes de esta persona ──
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PublicStatBox(Modifier.weight(1f), "${state.total}", "Reportes", Icons.Default.Pets)
                    PublicStatBox(Modifier.weight(1f), "${state.activos}", "Activos", Icons.Default.CheckCircle)
                    PublicStatBox(Modifier.weight(1f), "${state.recuperados}", "Recuperados", Icons.Default.Favorite)
                }

                // ── Lista de reportes de esta persona (tapeables) ──
                if (state.reportes.isNotEmpty()) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            Text("Reportes de ${perfil.nombre.ifBlank { "esta persona" }}",
                                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Ink)
                        }
                        state.reportes.forEach { rep ->
                            PublicReportCard(
                                titulo = rep.titulo,
                                estado = rep.estado,
                                foto = rep.imagenesReporte?.firstOrNull()?.urlImagen,
                                onClick = { onOpenReport(rep.id) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Botón enviar mensaje (si no es mi propio perfil)
                if (!state.esMiPerfil) {
                    Button(
                        onClick = { onOpenChat(perfil.id, perfil.nombre) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Chat, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar mensaje", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicStatBox(modifier: Modifier, value: String, label: String, icon: ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(label, fontSize = 10.sp, color = Muted)
        }
    }
}

@Composable
private fun PublicReportCard(titulo: String, estado: String, foto: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Border)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (foto != null) {
                AsyncImage(model = foto, contentDescription = null, modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Pets, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold, color = Ink, maxLines = 1)
                val (etiqueta, colorE) = when (estado) {
                    "PERDIDA" -> "Perdida" to Color(0xFFDC2626)
                    "ENCONTRADA" -> "Encontrada" to Color(0xFF2563EB)
                    else -> "Recuperada" to Color(0xFF22C55E)
                }
                Text(etiqueta, fontSize = 12.sp, color = colorE, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFCBD5E1))
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, texto: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
            Text(texto, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        }
    }
}
