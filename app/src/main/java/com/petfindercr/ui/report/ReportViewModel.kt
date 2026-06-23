package com.petfindercr.ui.report

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.data.model.ImagenReporte
import com.petfindercr.data.model.Reporte
import com.petfindercr.data.model.TipoMascota
import com.petfindercr.data.repository.AuthRepository
import com.petfindercr.data.repository.ReporteRepository
import com.petfindercr.data.repository.StorageRepository
import com.petfindercr.utils.getLastKnownLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportUiState(
    val reportes: List<Reporte> = emptyList(),
    val selectedReporte: Reporte? = null,
    val tiposMascota: List<TipoMascota> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,

    // Form fields
    val titulo: String = "",
    val descripcion: String = "",
    val color: String = "",
    val raza: String = "",
    val estado: EstadoReporte = EstadoReporte.PERDIDA,
    val selectedTipoMascota: TipoMascota? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val direccion: String = "",
    val recompensa: Boolean = false,
    val montoRecompensa: String = "",
    val selectedImageUris: List<Uri> = emptyList(),
    val uploadedImageUrls: List<String> = emptyList(),
    val isLoadingLocation: Boolean = false
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reporteRepository: ReporteRepository,
    private val storageRepository: StorageRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init { loadTiposMascota() }

    fun loadReportes(estado: EstadoReporte? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            reporteRepository.getReportes(estado).onSuccess { list ->
                _uiState.value = _uiState.value.copy(isLoading = false, reportes = list)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun loadReporte(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            reporteRepository.getReporte(id).onSuccess { reporte ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedReporte = reporte,
                    titulo = reporte.titulo,
                    descripcion = reporte.descripcion ?: "",
                    color = reporte.color ?: "",
                    raza = reporte.raza ?: "",
                    estado = EstadoReporte.valueOf(reporte.estado),
                    selectedTipoMascota = reporte.tiposMascota,
                    latitud = reporte.latitud,
                    longitud = reporte.longitud,
                    direccion = reporte.direccion ?: "",
                    recompensa = reporte.recompensa,
                    montoRecompensa = reporte.montoRecompensa?.toString() ?: ""
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun loadTiposMascota() {
        viewModelScope.launch {
            reporteRepository.getTiposMascota().onSuccess { tipos ->
                _uiState.value = _uiState.value.copy(tiposMascota = tipos)
            }
        }
    }

    fun onTituloChange(v: String) { _uiState.value = _uiState.value.copy(titulo = v) }
    fun onDescripcionChange(v: String) { _uiState.value = _uiState.value.copy(descripcion = v) }
    fun onColorChange(v: String) { _uiState.value = _uiState.value.copy(color = v) }
    fun onRazaChange(v: String) { _uiState.value = _uiState.value.copy(raza = v) }
    fun onEstadoChange(v: EstadoReporte) { _uiState.value = _uiState.value.copy(estado = v) }
    fun onTipoMascotaChange(v: TipoMascota) { _uiState.value = _uiState.value.copy(selectedTipoMascota = v) }
    fun onDireccionChange(v: String) { _uiState.value = _uiState.value.copy(direccion = v) }
    fun onRecompensaChange(v: Boolean) { _uiState.value = _uiState.value.copy(recompensa = v) }
    fun onMontoRecompensaChange(v: String) { _uiState.value = _uiState.value.copy(montoRecompensa = v) }
    fun onLocationSelected(lat: Double, lon: Double) { _uiState.value = _uiState.value.copy(latitud = lat, longitud = lon) }

    /** Obtiene la ubicación actual del dispositivo y la guarda en el formulario. */
    fun loadCurrentLocation(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocation = true)
            try {
                val location = getLastKnownLocation(context)
                _uiState.value = if (location != null) {
                    _uiState.value.copy(
                        latitud = location.latitude,
                        longitud = location.longitude,
                        isLoadingLocation = false
                    )
                } else {
                    _uiState.value.copy(isLoadingLocation = false, error = "No se pudo obtener la ubicación")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingLocation = false, error = "No se pudo obtener la ubicación")
            }
        }
    }
    fun addImageUri(uri: Uri) { _uiState.value = _uiState.value.copy(selectedImageUris = _uiState.value.selectedImageUris + uri) }
    fun removeImageUri(uri: Uri) { _uiState.value = _uiState.value.copy(selectedImageUris = _uiState.value.selectedImageUris - uri) }
    fun clearSuccess() { _uiState.value = _uiState.value.copy(success = false) }

    fun createReporte(context: Context) {
        val s = _uiState.value
        if (s.titulo.isBlank()) {
            _uiState.value = s.copy(error = "El título es requerido")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            val userId = authRepository.currentUser?.id ?: run {
                _uiState.value = _uiState.value.copy(isSaving = false, error = "Usuario no autenticado")
                return@launch
            }
            val reporte = Reporte(
                usuarioId = userId,
                titulo = s.titulo,
                descripcion = s.descripcion.takeIf { it.isNotBlank() },
                color = s.color.takeIf { it.isNotBlank() },
                raza = s.raza.takeIf { it.isNotBlank() },
                estado = s.estado.name,
                tipoMascotaId = s.selectedTipoMascota?.id,
                latitud = s.latitud,
                longitud = s.longitud,
                direccion = s.direccion.takeIf { it.isNotBlank() },
                recompensa = s.recompensa,
                montoRecompensa = s.montoRecompensa.toDoubleOrNull()
            )
            reporteRepository.createReporte(reporte).onSuccess { created ->
                s.selectedImageUris.forEach { uri ->
                    storageRepository.uploadReporteImage(uri).onSuccess { url ->
                        reporteRepository.addImagen(ImagenReporte(reporteId = created.id, urlImagen = url))
                    }
                }
                _uiState.value = _uiState.value.copy(isSaving = false, success = true)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun updateReporte(context: Context) {
        val s = _uiState.value
        val existing = s.selectedReporte ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            val updated = existing.copy(
                titulo = s.titulo,
                descripcion = s.descripcion.takeIf { it.isNotBlank() },
                color = s.color.takeIf { it.isNotBlank() },
                raza = s.raza.takeIf { it.isNotBlank() },
                estado = s.estado.name,
                tipoMascotaId = s.selectedTipoMascota?.id,
                latitud = s.latitud,
                longitud = s.longitud,
                direccion = s.direccion.takeIf { it.isNotBlank() },
                recompensa = s.recompensa,
                montoRecompensa = s.montoRecompensa.toDoubleOrNull()
            )
            reporteRepository.updateReporte(updated).onSuccess {
                s.selectedImageUris.forEach { uri ->
                    storageRepository.uploadReporteImage(uri).onSuccess { url ->
                        reporteRepository.addImagen(ImagenReporte(reporteId = existing.id, urlImagen = url))
                    }
                }
                _uiState.value = _uiState.value.copy(isSaving = false, success = true)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun deleteReporte(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            reporteRepository.deleteReporte(id).onSuccess { onSuccess() }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun updateEstado(id: Long, estado: EstadoReporte) {
        viewModelScope.launch {
            reporteRepository.updateEstado(id, estado)
            loadReporte(id)
        }
    }
}
