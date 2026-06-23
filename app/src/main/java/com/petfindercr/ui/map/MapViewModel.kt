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
            try {
                val location = getLastKnownLocation(context)
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    _uiState.value = _uiState.value.copy(currentLocation = latLng)
                    reporteRepository.getReportesNearby(location.latitude, location.longitude).onSuccess { list ->
                        _uiState.value = _uiState.value.copy(isLoading = false, reportes = list)
                    }.onFailure { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                    }
                } else {
                    // Load all if location unavailable
                    reporteRepository.getReportes().onSuccess { list ->
                        _uiState.value = _uiState.value.copy(isLoading = false, reportes = list)
                    }
                }
            } catch (e: Exception) {
                reporteRepository.getReportes().onSuccess { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, reportes = list)
                }
            }
        }
    }
}
