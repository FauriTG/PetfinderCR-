package com.petfindercr.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.data.model.Perfil
import com.petfindercr.data.model.Reporte
import com.petfindercr.data.repository.AuthRepository
import com.petfindercr.data.repository.PerfilRepository
import com.petfindercr.data.repository.ReporteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublicProfileUiState(
    val perfil: Perfil? = null,
    val reportes: List<Reporte> = emptyList(),
    val isLoading: Boolean = true,
    val esMiPerfil: Boolean = false,
    val error: String? = null
) {
    val total get() = reportes.size
    val activos get() = reportes.count { it.estado != EstadoReporte.RECUPERADA.name }
    val recuperados get() = reportes.count { it.estado == EstadoReporte.RECUPERADA.name }
}

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val perfilRepository: PerfilRepository,
    private val reporteRepository: ReporteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublicProfileUiState())
    val uiState: StateFlow<PublicProfileUiState> = _uiState.asStateFlow()

    fun cargar(userId: String) {
        viewModelScope.launch {
            _uiState.value = PublicProfileUiState(isLoading = true)
            val perfil = perfilRepository.getPerfil(userId).getOrNull()
            val reportes = reporteRepository.getReportesByUser(userId).getOrDefault(emptyList())
            if (perfil != null) {
                _uiState.value = PublicProfileUiState(
                    perfil = perfil,
                    reportes = reportes,
                    isLoading = false,
                    esMiPerfil = authRepository.currentUser?.id == userId
                )
            } else {
                _uiState.value = PublicProfileUiState(isLoading = false, error = "No se pudo cargar el perfil")
            }
        }
    }
}
