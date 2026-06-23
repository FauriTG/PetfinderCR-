package com.petfindercr.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.petfindercr.data.model.Reporte
import com.petfindercr.data.repository.ReporteRepository
import com.petfindercr.utils.getLastKnownLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val reportes: List<Reporte> = emptyList(),
    val currentLocation: LatLng? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val reporteRepository: ReporteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun loadNearbyReportes(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 1. Cargar TODOS los reportes como marcadores (siempre se ven los puntos)
            reporteRepository.getReportes().onSuccess { list ->
                _uiState.value = _uiState.value.copy(isLoading = false, reportes = list)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }

            // 2. Obtener ubicación actual solo para centrar la cámara (best effort)
            try {
                val location = getLastKnownLocation(context)
                if (location != null) {
                    _uiState.value = _uiState.value.copy(
                        currentLocation = LatLng(location.latitude, location.longitude)
                    )
                }
            } catch (_: Exception) { /* sin ubicación: la cámara se queda en Costa Rica */ }
        }
    }
}
