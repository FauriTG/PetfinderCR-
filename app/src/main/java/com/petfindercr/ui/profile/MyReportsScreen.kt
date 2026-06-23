package com.petfindercr.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petfindercr.ui.components.ReportCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadMisReportes() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Reportes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (state.misReportes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No tienes reportes aún.", style = MaterialTheme.typography.bodyLarge)
                    Text("Crea un reporte desde la pantalla principal.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.misReportes, key = { it.id }) { reporte ->
                    ReportCard(reporte = reporte, onClick = { onNavigateToDetail(reporte.id) })
                }
            }
        }
    }
}
