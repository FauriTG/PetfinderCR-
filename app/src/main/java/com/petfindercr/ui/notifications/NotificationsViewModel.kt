package com.petfindercr.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.data.model.Notificacion
import com.petfindercr.data.model.SolicitudEstado
import com.petfindercr.data.repository.AuthRepository
import com.petfindercr.data.repository.NotificacionRepository
import com.petfindercr.data.repository.PerfilRepository
import com.petfindercr.data.repository.ReporteRepository
import com.petfindercr.data.repository.SolicitudRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Solicitud enriquecida con nombres para mostrar. */
data class SolicitudUi(
    val solicitud: SolicitudEstado,
    val solicitanteNombre: String,
    val reporteTitulo: String
)

data class NotificationsUiState(
    val solicitudes: List<SolicitudUi> = emptyList(),
    val notificaciones: List<Notificacion> = emptyList(),
    val isLoading: Boolean = true,
    val procesando: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val solicitudRepository: SolicitudRepository,
    private val notificacionRepository: NotificacionRepository,
    private val reporteRepository: ReporteRepository,
    private val perfilRepository: PerfilRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val userId get() = authRepository.currentUser?.id ?: ""

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val pendientes = solicitudRepository.getPendientesParaDueno(userId).getOrDefault(emptyList())
            val solicitudesUi = pendientes.map { s ->
                val nombre = s.solicitanteId?.let { perfilRepository.getPerfil(it).getOrNull()?.nombre } ?: "Usuario"
                SolicitudUi(
                    solicitud = s,
                    solicitanteNombre = nombre,
                    reporteTitulo = s.reportes?.titulo ?: "Reporte"
                )
            }

            val notifs = notificacionRepository.getMias(userId).getOrDefault(emptyList())

            _uiState.value = NotificationsUiState(
                solicitudes = solicitudesUi,
                notificaciones = notifs,
                isLoading = false
            )
        }
    }

    fun responder(item: SolicitudUi, aprobar: Boolean) {
        val s = item.solicitud
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(procesando = true)

            // 1. Marca la solicitud
            solicitudRepository.responder(s.id, aprobar)

            // 2. Si se aprueba, aplica el cambio de estado al reporte
            if (aprobar && s.reporteId != null) {
                runCatching { EstadoReporte.valueOf(s.estadoSolicitado) }.getOrNull()?.let { estado ->
                    reporteRepository.updateEstado(s.reporteId, estado)
                }
            }

            // 3. Avisa al solicitante
            s.solicitanteId?.let { solicitanteId ->
                val titulo = if (aprobar) "Solicitud aprobada ✅" else "Solicitud rechazada"
                val msg = if (aprobar)
                    "Tu cambio de estado a \"${s.estadoSolicitado}\" en \"${item.reporteTitulo}\" fue aprobado."
                else
                    "Tu solicitud de cambio en \"${item.reporteTitulo}\" fue rechazada."
                notificacionRepository.crear(solicitanteId, titulo, msg)
            }

            _uiState.value = _uiState.value.copy(procesando = false)
            cargar()
        }
    }

    fun marcarLeida(notif: Notificacion) {
        viewModelScope.launch {
            notificacionRepository.marcarLeida(notif.id)
            _uiState.value = _uiState.value.copy(
                notificaciones = _uiState.value.notificaciones.map {
                    if (it.id == notif.id) it.copy(leida = true) else it
                }
            )
        }
    }
}
