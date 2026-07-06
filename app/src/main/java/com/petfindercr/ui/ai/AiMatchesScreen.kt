package com.petfindercr.ui.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

private val BgColor = Color(0xFFF8F9FC)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)
private val Border = Color(0xFFE2E8F0)
private val ScoreHigh = Color(0xFF22C55E)
private val ScoreMid = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMatchesScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: AiMatchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Coincidencias IA", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = viewModel::analizar) {
                        Icon(Icons.Default.Refresh, "Volver a analizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingState()
                state.error != null -> EmptyState(
                    icon = Icons.Default.SearchOff,
                    title = "Ups, algo salió mal",
                    subtitle = state.error ?: ""
                )
                state.misPerdidasCount == 0 -> EmptyState(
                    icon = Icons.Default.Pets,
                    title = "No tienes mascotas perdidas",
                    subtitle = "Cuando reportes una mascota perdida, aquí buscaremos automáticamente coincidencias con las mascotas encontradas por otros usuarios."
                )
                state.matches.isEmpty() -> EmptyState(
                    icon = Icons.Default.SearchOff,
                    title = "Sin coincidencias por ahora",
                    subtitle = "Analizamos las mascotas encontradas y ninguna coincide todavía con tus reportes. Vuelve a intentarlo más tarde: cada día se publican nuevos reportes."
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item { HeaderBanner(state.matches.size) }
                    items(state.matches) { match ->
                        MatchCard(match = match, onClick = { onNavigateToDetail(match.encontrada.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderBanner(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                Icons.Default.AutoAwesome, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    "$count posible${if (count == 1) "" else "s"} coincidencia${if (count == 1) "" else "s"}",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink
                )
                Text(
                    "Ordenadas por porcentaje de similitud",
                    fontSize = 12.sp, color = Muted
                )
            }
        }
    }
}

@Composable
private fun MatchCard(match: PetMatch, onClick: () -> Unit) {
    val foto = match.encontrada.imagenesReporte?.firstOrNull()?.urlImagen
    val scoreColor = when {
        match.score >= 70 -> ScoreHigh
        match.score >= 50 -> ScoreMid
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Foto de la mascota encontrada
                if (foto != null) {
                    AsyncImage(
                        model = foto,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Pets, null, tint = MaterialTheme.colorScheme.primary) }
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        match.encontrada.titulo,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink,
                        maxLines = 1
                    )
                    Text("Mascota encontrada", fontSize = 12.sp, color = Muted)
                    match.distanciaKm?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, null, tint = Muted, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(
                                "vs. \"${match.perdida.titulo}\"",
                                fontSize = 11.sp, color = Muted, maxLines = 1
                            )
                        }
                    }
                }

                // Badge de porcentaje
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(scoreColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${match.score}%", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = scoreColor)
                }
            }

            if (match.razones.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                FlowRowChips(match.razones)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowChips(razones: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        razones.forEach { razon ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BgColor,
                border = BorderStroke(1.dp, Border)
            ) {
                Text(
                    razon,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 11.sp, color = Muted, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Analizando reportes…", color = Muted)
    }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted, textAlign = TextAlign.Center, lineHeight = 22.sp)
    }
}
