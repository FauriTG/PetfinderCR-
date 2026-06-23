package com.petfindercr.ui.report

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
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.ui.components.ReportCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Perdidas", "Encontradas", "Recuperadas")
    val estados = listOf(EstadoReporte.PERDIDA, EstadoReporte.ENCONTRADA, EstadoReporte.RECUPERADA)

    LaunchedEffect(selectedTab) { viewModel.loadReportes(estados[selectedTab]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.reportes.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No hay reportes en esta categoría.")
                            }
                        }
                    } else {
                        items(state.reportes, key = { it.id }) { reporte ->
                            ReportCard(reporte = reporte, onClick = { onNavigateToDetail(reporte.id) })
                        }
                    }
                }
            }
        }
    }
}
