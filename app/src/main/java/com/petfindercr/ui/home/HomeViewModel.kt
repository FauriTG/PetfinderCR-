package com.petfindercr.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.data.model.Perfil
import com.petfindercr.data.model.Reporte
import com.petfindercr.data.repository.AuthRepository
import com.petfindercr.data.repository.PerfilRepository
import com.petfindercr.data.repository.ReporteRepository
import com.petfindercr.utils.NotificationHelper
import com.petfindercr.utils.getLastKnownLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val perdidos: List<Reporte> = emptyList(),
    val encontrados: List<Reporte> = emptyList(),
    val cercanos: List<Reporte> = emptyList(),
    val misReportes: List<Reporte> = emptyList(),
    val userProfile: Perfil? = null,
    val isLoading: Boolean = false,
    val isLoadingNearby: Boolean = false,
    val error: String? = null
) {
    val statsPerdidas get() = perdidos.size
    val statsEncontradas get() = encontrados.size
    val statsActivos get() = misReportes.count { it.estado != EstadoReporte.RECUPERADA.name }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reporteRepository: ReporteRepository,
    private val perfilRepository: PerfilRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("notified_reports", Context.MODE_PRIVATE)

    init {
        loadReportes()
        loadUserData()
    }

    fun loadReportes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val perdidosResult = reporteRepository.getReportes(EstadoReporte.PERDIDA)
            val encontradosResult = reporteRepository.getReportes(EstadoReporte.ENCONTRADA)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                perdidos = perdidosResult.getOrDefault(emptyList()).take(10),
                encontrados = encontradosResult.getOrDefault(emptyList()).take(10),
                error = perdidosResult.exceptionOrNull()?.message
            )
        }
    }

    private fun loadUserData() {
        val userId = authRepository.currentUser?.id ?: return
        viewModelScope.launch {
            perfilRepository.getPerfil(userId).onSuccess { perfil ->
                _uiState.value = _uiState.value.copy(userProfile = perfil)
            }
            reporteRepository.getReportesByUser(userId).onSuccess { reportes ->
                _uiState.value = _uiState.value.copy(misReportes = reportes)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun loadNearbyReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingNearby = true)
            try {
                val location = getLastKnownLocation(context)
                if (location != null) {
                    reporteRepository.getReportesNearby(location.latitude, location.longitude)
                        .onSuccess { reportes ->
                            _uiState.value = _uiState.value.copy(isLoadingNearby = false, cercanos = reportes)
                            notifyNewNearbyReports(reportes)
                        }.onFailure {
                            _uiState.value = _uiState.value.copy(isLoadingNearby = false)
                        }
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingNearby = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingNearby = false)
            }
        }
    }

    private fun notifyNewNearbyReports(reportes: List<Reporte>) {
        reportes.forEach { reporte ->
            val key = "report_${reporte.id}"
            if (!prefs.getBoolean(key, false)) {
                prefs.edit().putBoolean(key, true).apply()
                val estadoLabel = when (reporte.estado) {
                    EstadoReporte.PERDIDA.name -> "perdida"
                    EstadoReporte.ENCONTRADA.name -> "encontrada"
                    else -> "reporte"
                }
                NotificationHelper.sendNearbyReportNotification(
                    context = context,
                    title = "Mascota $estadoLabel cerca de ti",
                    body = reporte.titulo,
                    notifId = reporte.id.toInt()
                )
            }
        }
    }
}
