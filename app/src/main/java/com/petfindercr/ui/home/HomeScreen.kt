package com.petfindercr.ui.home

import android.Manifest
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.data.model.Perfil
import com.petfindercr.data.model.Reporte
import com.petfindercr.ui.components.PetFinderBottomBar
import java.text.SimpleDateFormat
import java.util.*

// ═══════════════════════════════════════════════
//  HOME SCREEN
// ═══════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToAiMatches: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val permissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val permissionsState = rememberMultiplePermissionsState(permissions)
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) viewModel.loadNearbyReports()
        else permissionsState.launchMultiplePermissionRequest()
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FC),
        bottomBar = {
            PetFinderBottomBar(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "map"           -> onNavigateToMap()
                        "create_report" -> onNavigateToCreate()
                        "ai_matches"    -> onNavigateToAiMatches()
                        "profile"       -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ── Header ──
            item {
                HomeHeader(
                    profile = state.userProfile,
                    notifCount = 3,
                    onProfileClick = onNavigateToProfile
                )
            }

            // ── Hero Banner ──
            item { HeroBanner(onReportar = onNavigateToCreate) }

            // ── Cerca de ti ──
            item { Spacer(Modifier.height(24.dp)) }
            item {
                SectionHeader(
                    title = "Casos recientes cerca de ti",
                    actionLabel = "Ver todos",
                    onAction = onNavigateToList,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
            item {
                RecentCasesRow(
                    reportes = state.cercanos,
                    isLoading = state.isLoadingNearby,
                    onDetail = onNavigateToDetail
                )
            }

            // ── Mapa ──
            item { Spacer(Modifier.height(24.dp)) }
            item {
                PetMapCard(
                    onSeeMap = onNavigateToMap,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── Estadísticas ──
            item { Spacer(Modifier.height(24.dp)) }
            item {
                StatsSection(
                    perdidas = state.statsPerdidas,
                    encontradas = state.statsEncontradas,
                    activos = state.statsActivos,
                    coincidencias = 0,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── IA Match Banner ──
            item { Spacer(Modifier.height(24.dp)) }
            item {
                AiMatchBannerCard(
                    onVerCoincidencias = onNavigateToAiMatches,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── Mis Reportes ──
            if (state.misReportes.isNotEmpty()) {
                item { Spacer(Modifier.height(24.dp)) }
                item {
                    SectionHeader(
                        title = "Mis reportes",
                        actionLabel = "Ver todos",
                        onAction = onNavigateToList,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
                items(state.misReportes.take(3)) { reporte ->
                    MyReportCard(
                        reporte = reporte,
                        onClick = { onNavigateToDetail(reporte.id) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Notificaciones ──
            item { Spacer(Modifier.height(24.dp)) }
            item {
                NotificationsSection(modifier = Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  HEADER
// ═══════════════════════════════════════════════

@Composable
private fun HomeHeader(
    profile: Perfil?,
    notifCount: Int,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Pets,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "PetFinder CR",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
        )
        Spacer(Modifier.weight(1f))

        // Bell with badge
        BadgedBox(
            badge = {
                if (notifCount > 0) {
                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                        Text("$notifCount", color = Color.White, fontSize = 9.sp)
                    }
                }
            }
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notificaciones",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.width(16.dp))

        // Avatar
        Surface(
            modifier = Modifier
                .size(38.dp)
                .clickable(onClick = onProfileClick),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 3.dp
        ) {
            if (profile?.fotoPerfil != null) {
                AsyncImage(
                    model = profile.fotoPerfil,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = profile?.nombre?.firstOrNull()?.uppercase() ?: "U",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  HERO BANNER
// ═══════════════════════════════════════════════

@Composable
private fun HeroBanner(onReportar: () -> Unit) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.62f)
            ) {
                Text(
                    text = "¿Perdiste o encontraste a una mascota?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B0764),
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Crea un reporte y ayuda a reunir familias con sus mascotas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B21A8),
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onReportar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reportar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }

            // Pet emoji illustration
            Column(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🐕", fontSize = 56.sp, modifier = Modifier.offset(x = 6.dp))
                Text("🐈", fontSize = 46.sp, modifier = Modifier.offset(x = (-6).dp, y = (-4).dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  SECTION HEADER
// ═══════════════════════════════════════════════

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        TextButton(onClick = onAction, contentPadding = PaddingValues(0.dp)) {
            Text(
                text = actionLabel,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ═══════════════════════════════════════════════
//  RECENT CASES ROW
// ═══════════════════════════════════════════════

@Composable
private fun RecentCasesRow(
    reportes: List<Reporte>,
    isLoading: Boolean,
    onDetail: (Long) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            isLoading -> items(3) { ShimmerCaseCard() }
            reportes.isEmpty() -> item { EmptyCasesCard() }
            else -> items(reportes) { reporte ->
                RecentCaseCard(reporte = reporte, onClick = { onDetail(reporte.id) })
            }
        }
    }
}

@Composable
private fun EmptyCasesCard() {
    Card(
        modifier = Modifier.width(280.dp).height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Search,
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFFCBD5E1)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "No hay casos cerca de ti",
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  RECENT CASE CARD  (300 × 220 dp)
// ═══════════════════════════════════════════════

@Composable
private fun RecentCaseCard(reporte: Reporte, onClick: () -> Unit) {
    val firstImage = reporte.imagenesReporte?.firstOrNull()
    val (badgeColor, badgeText) = when (reporte.estado) {
        EstadoReporte.PERDIDA.name    -> Color(0xFFDC2626) to "PERDIDO"
        EstadoReporte.ENCONTRADA.name -> Color(0xFF2563EB) to "ENCONTRADO"
        else                          -> Color(0xFF22C55E) to "RECUPERADO"
    }

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // ── Photo ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                if (firstImage != null) {
                    AsyncImage(
                        model = firstImage.urlImagen,
                        contentDescription = reporte.titulo,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFF3E8FF), Color(0xFFEDE9FE))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Pets,
                            null,
                            modifier = Modifier.size(52.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                }
                // Badge
                Surface(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // ── Info ──
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = reporte.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                reporte.raza?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    reporte.direccion?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Text(
                        text = timeAgo(reporte.fechaReporte),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Ver detalles →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  SHIMMER CARD
// ═══════════════════════════════════════════════

@Composable
private fun ShimmerCaseCard() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    val shimmer = Color(0xFFE2E8F0).copy(alpha = alpha)

    Card(
        modifier = Modifier.width(280.dp).height(220.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(115.dp).background(shimmer))
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(Modifier.fillMaxWidth(0.7f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(shimmer))
                Box(Modifier.fillMaxWidth(0.5f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(shimmer))
                Box(Modifier.fillMaxWidth(0.9f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(shimmer))
                Box(Modifier.fillMaxWidth(0.4f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(shimmer))
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  MAP CARD
// ═══════════════════════════════════════════════

@Composable
private fun PetMapCard(onSeeMap: () -> Unit, modifier: Modifier = Modifier) {
    val sanJose = remember { LatLng(9.9281, -84.0907) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(sanJose, 7.5f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false
                )
            )

            // Location pill
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Escazú, San José",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F172A)
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF64748B)
                    )
                }
            }

            // GPS button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onSeeMap),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    "Ver mapa completo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  STATS SECTION  (grid 2×2)
// ═══════════════════════════════════════════════

@Composable
private fun StatsSection(
    perdidas: Int,
    encontradas: Int,
    activos: Int,
    coincidencias: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Resumen",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = "$perdidas",
                label = "Mascotas\nPerdidas",
                icon = Icons.Default.Pets,
                iconColor = LostRed,
                bgColor = Color(0xFFFEF2F2)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = "$encontradas",
                label = "Mascotas\nEncontradas",
                icon = Icons.Default.Favorite,
                iconColor = FoundBlue,
                bgColor = Color(0xFFEFF6FF)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = "$coincidencias",
                label = "Coincidencias\nIA",
                icon = Icons.Default.AutoAwesome,
                iconColor = Purple700,
                bgColor = Color(0xFFF5F3FF)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = "$activos",
                label = "Reportes\nActivos",
                icon = Icons.Default.CheckCircle,
                iconColor = SuccessGreen,
                bgColor = Color(0xFFF0FDF4)
            )
        }
    }
}

private val LostRed     = Color(0xFFDC2626)
private val FoundBlue   = Color(0xFF2563EB)
private val Purple700   = Color(0xFF7C4DFF)
private val SuccessGreen = Color(0xFF22C55E)

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = bgColor,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                lineHeight = 14.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════
//  AI MATCH BANNER
// ═══════════════════════════════════════════════

@Composable
private fun AiMatchBannerCard(
    onVerCoincidencias: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF7C4DFF), Color(0xFF4F46E5)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, 0f)
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Coincidencias IA",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Encontramos posibles coincidencias para tu mascota.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 15.sp
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.clickable(onClick = onVerCoincidencias),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Ver coincidencias →",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  MY REPORT CARD
// ═══════════════════════════════════════════════

@Composable
private fun MyReportCard(
    reporte: Reporte,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstImage = reporte.imagenesReporte?.firstOrNull()
    val (statusColor, statusText) = when (reporte.estado) {
        EstadoReporte.PERDIDA.name    -> LostRed to "Perdida"
        EstadoReporte.ENCONTRADA.name -> FoundBlue to "Encontrada"
        EstadoReporte.RECUPERADA.name -> SuccessGreen to "Recuperada"
        else                          -> Color(0xFF64748B) to reporte.estado
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (firstImage != null) {
                    AsyncImage(
                        model = firstImage.urlImagen,
                        contentDescription = reporte.titulo,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Pets,
                        null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reporte.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Tipo • Raza
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    reporte.tiposMascota?.nombre?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                        if (reporte.raza != null) {
                            Text("•", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                        }
                    }
                    reporte.raza?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                reporte.direccion?.let {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(10.dp), tint = Color(0xFF94A3B8))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Status chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  NOTIFICATIONS SECTION
// ═══════════════════════════════════════════════

private data class NotifData(val icon: ImageVector, val title: String, val time: String)

@Composable
private fun NotificationsSection(modifier: Modifier = Modifier) {
    val notifs = listOf(
        NotifData(Icons.Default.Search,        "Nueva mascota encontrada cerca de tu zona.", "Hace 2 horas"),
        NotifData(Icons.Default.AutoAwesome,   "Coincidencia IA detectada para tu reporte.", "Hace 5 horas"),
        NotifData(Icons.Default.Notifications, "Reporte actualizado por otro usuario.",      "Hace 1 día")
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Últimas notificaciones",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(Modifier.height(12.dp))
            notifs.forEachIndexed { idx, notif ->
                NotificationItem(notif.icon, notif.title, notif.time)
                if (idx < notifs.lastIndex) {
                    Box(
                        modifier = Modifier
                            .padding(start = 48.dp)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFE2E8F0))
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(icon: ImageVector, title: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0F172A),
                maxLines = 2
            )
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

// ═══════════════════════════════════════════════
//  HELPERS
// ═══════════════════════════════════════════════

private fun timeAgo(dateStr: String?): String {
    if (dateStr == null) return "Recientemente"
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(dateStr.substringBefore(".").substringBefore("+")) ?: return "Recientemente"
        val diffMs = System.currentTimeMillis() - date.time
        val hours = diffMs / (1000L * 60 * 60)
        when {
            hours < 1   -> "Hace unos min"
            hours < 24  -> "Hace ${hours}h"
            hours < 168 -> "Hace ${hours / 24}d"
            else        -> "Hace ${hours / 168} sem"
        }
    } catch (e: Exception) {
        "Recientemente"
    }
}
