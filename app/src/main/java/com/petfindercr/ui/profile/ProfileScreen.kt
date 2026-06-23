package com.petfindercr.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

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

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
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
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box(contentAlignment = Alignment.BottomEnd) {
                    if (state.perfil?.fotoPerfil != null) {
                        AsyncImage(
                            model = state.perfil!!.fotoPerfil,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.size(100.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(100.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    SmallFloatingActionButton(onClick = { photoLauncher.launch("image/*") }) {
                        Icon(Icons.Default.CameraAlt, "Cambiar foto", modifier = Modifier.size(16.dp))
                    }
                }

                if (state.isEditing) {
                    OutlinedTextField(value = state.nombre, onValueChange = viewModel::onNombreChange,
                        label = { Text("Nombre") }, leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = state.telefono, onValueChange = viewModel::onTelefonoChange,
                        label = { Text("Teléfono") }, leadingIcon = { Icon(Icons.Default.Phone, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                } else {
                    Text(state.perfil?.nombre ?: "Sin nombre", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    state.perfil?.telefono?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }

                Divider()

                // Options
                ListItem(
                    headlineContent = { Text("Mis Reportes") },
                    leadingContent = { Icon(Icons.Default.List, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                    modifier = Modifier.clip(MaterialTheme.shapes.medium).clickable { onNavigateToMyReports() }
                )

                Divider()

                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar sesión")
                }

                state.error?.let { error ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(error, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                Button(onClick = { showLogoutDialog = false; viewModel.signOut(onLogout) }) { Text("Cerrar sesión") }
            },
            dismissButton = { OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") } }
        )
    }
}
