package com.petfindercr.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

private val LostRed = Color(0xFFDC2626)
private val FoundBlue = Color(0xFF2563EB)
private val SuccessGreen = Color(0xFF22C55E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToMyReports: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
    }

    // Mostrar mensajes / errores como snackbar
    LaunchedEffect(state.message, state.error) {
        val text = state.message ?: state.error
        if (text != null) {
            snackbarHostState.showSnackbar(text)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FC),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = viewModel::savePerfil, enabled = !state.isSaving) {
                            if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp))
                            else Icon(Icons.Default.Check, "Guardar")
                        }
                        IconButton(onClick = viewModel::cancelEditing) { Icon(Icons.Default.Close, "Cancelar") }
                    } else {
                        IconButton(onClick = viewModel::startEditing) { Icon(Icons.Default.Edit, "Editar") }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FC))
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Tarjeta de perfil ──
                ProfileHeaderCard(
                    fotoPerfil = state.perfil?.fotoPerfil,
                    nombre = state.perfil?.nombre ?: "Sin nombre",
                    email = state.email,
                    telefono = state.perfil?.telefono,
                    isEditing = state.isEditing,
                    nombreField = state.nombre,
                    telefonoField = state.telefono,
                    onNombreChange = viewModel::onNombreChange,
                    onTelefonoChange = viewModel::onTelefonoChange,
                    onChangePhoto = { photoLauncher.launch("image/*") }
                )

                // ── Estadísticas ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox(Modifier.weight(1f), "${state.totalReportes}", "Reportes", Icons.Default.Pets, MaterialTheme.colorScheme.primary)
                    StatBox(Modifier.weight(1f), "${state.reportesActivos}", "Activos", Icons.Default.CheckCircle, FoundBlue)
                    StatBox(Modifier.weight(1f), "${state.reportesRecuperados}", "Recuperados", Icons.Default.Favorite, SuccessGreen)
                }

                // ── Sección: Cuenta ──
                SectionLabel("Cuenta")
                SettingsGroup {
                    SettingsRow(Icons.Default.List, "Mis reportes", "Ver y gestionar tus publicaciones", onClick = onNavigateToMyReports)
                    RowDivider()
                    SettingsRow(Icons.Default.Lock, "Cambiar contraseña", "Recibe un correo para restablecerla", onClick = { showPasswordDialog = true })
                }

                // ── Sección: General ──
                SectionLabel("General")
                SettingsGroup {
                    SettingsRow(Icons.Default.Notifications, "Notificaciones", "Reportes cercanos activados", onClick = {})
                    RowDivider()
                    SettingsRow(Icons.Default.Info, "Acerca de PetFinder CR", "Versión 1.0", onClick = { showAboutDialog = true })
                }

                Spacer(Modifier.height(4.dp))

                // ── Cerrar sesión ──
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, LostRed.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LostRed)
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // ── Diálogos ──
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null, tint = LostRed) },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; viewModel.signOut(onLogout) },
                    colors = ButtonDefaults.buttonColors(containerColor = LostRed)
                ) { Text("Cerrar sesión") }
            },
            dismissButton = { OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") } }
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            icon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Cambiar contraseña") },
            text = { Text("Te enviaremos un correo a ${state.email} con un enlace para restablecer tu contraseña.") },
            confirmButton = {
                Button(onClick = { showPasswordDialog = false; viewModel.sendPasswordReset() }) { Text("Enviar correo") }
            },
            dismissButton = { OutlinedButton(onClick = { showPasswordDialog = false }) { Text("Cancelar") } }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Default.Pets, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("PetFinder CR") },
            text = {
                Text(
                    "Versión 1.0\n\n" +
                    "App para reportar y encontrar mascotas perdidas y encontradas en Costa Rica. " +
                    "Ayuda a reunir familias con sus mascotas. 🐾"
                )
            },
            confirmButton = { Button(onClick = { showAboutDialog = false }) { Text("Entendido") } }
        )
    }
}

// ═══════════════════════════════════════════════
//  TARJETA DE PERFIL
// ═══════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileHeaderCard(
    fotoPerfil: String?,
    nombre: String,
    email: String,
    telefono: String?,
    isEditing: Boolean,
    nombreField: String,
    telefonoField: String,
    onNombreChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onChangePhoto: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(contentAlignment = Alignment.BottomEnd) {
                if (fotoPerfil != null) {
                    AsyncImage(
                        model = fotoPerfil,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.size(96.dp).clip(CircleShape)
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(96.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = nombre.firstOrNull()?.uppercase() ?: "U",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                SmallFloatingActionButton(
                    onClick = onChangePhoto,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.CameraAlt, "Cambiar foto", modifier = Modifier.size(16.dp))
                }
            }

            if (isEditing) {
                OutlinedTextField(
                    value = nombreField, onValueChange = onNombreChange,
                    label = { Text("Nombre") }, leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = telefonoField, onValueChange = onTelefonoChange,
                    label = { Text("Teléfono") }, leadingIcon = { Icon(Icons.Default.Phone, null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(nombre, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Email, null, modifier = Modifier.size(14.dp), tint = Color(0xFF94A3B8))
                    Text(email, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                }
                telefono?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp), tint = Color(0xFF94A3B8))
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  ESTADÍSTICAS
// ═══════════════════════════════════════════════

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text(label, fontSize = 11.sp, color = Color(0xFF64748B))
        }
    }
}

// ═══════════════════════════════════════════════
//  SECCIONES / FILAS DE AJUSTES
// ═══════════════════════════════════════════════

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFCBD5E1))
    }
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .padding(start = 70.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFF1F5F9))
    )
}
