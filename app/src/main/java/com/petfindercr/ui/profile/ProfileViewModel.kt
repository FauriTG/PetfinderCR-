package com.petfindercr.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.data.model.Perfil
import com.petfindercr.data.model.Reporte
import com.petfindercr.data.repository.AuthRepository
import com.petfindercr.data.repository.PerfilRepository
import com.petfindercr.data.repository.ReporteRepository
import com.petfindercr.data.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val perfil: Perfil? = null,
    val email: String = "",
    val misReportes: List<Reporte> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    // Editable fields
    val nombre: String = "",
    val telefono: String = "",
    val isEditing: Boolean = false
) {
    val totalReportes get() = misReportes.size
    val reportesActivos get() = misReportes.count { it.estado != EstadoReporte.RECUPERADA.name }
    val reportesRecuperados get() = misReportes.count { it.estado == EstadoReporte.RECUPERADA.name }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val perfilRepository: PerfilRepository,
    private val reporteRepository: ReporteRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val userId get() = authRepository.currentUser?.id ?: ""
    private val userEmail get() = authRepository.currentUser?.email ?: ""

    init {
        loadPerfil()
        loadMisReportes()
    }

    fun loadPerfil() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, email = userEmail)
            perfilRepository.getPerfil(userId).onSuccess { perfil ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    perfil = perfil,
                    nombre = perfil.nombre,
                    telefono = perfil.telefono ?: ""
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun loadMisReportes() {
        viewModelScope.launch {
            reporteRepository.getReportesByUser(userId).onSuccess { list ->
                _uiState.value = _uiState.value.copy(misReportes = list)
            }
        }
    }

    fun startEditing() { _uiState.value = _uiState.value.copy(isEditing = true) }
    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(
            isEditing = false,
            nombre = _uiState.value.perfil?.nombre ?: "",
            telefono = _uiState.value.perfil?.telefono ?: ""
        )
    }

    fun onNombreChange(v: String) { _uiState.value = _uiState.value.copy(nombre = v) }
    fun onTelefonoChange(v: String) { _uiState.value = _uiState.value.copy(telefono = v) }

    fun savePerfil() {
        if (_uiState.value.nombre.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "El nombre no puede estar vacío")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            perfilRepository.updatePerfil(userId, _uiState.value.nombre, _uiState.value.telefono.takeIf { it.isNotBlank() })
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSaving = false, isEditing = false, message = "Perfil actualizado")
                    loadPerfil()
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isSaving = false, error = e.message) }
        }
    }

    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            storageRepository.uploadProfileImage(uri, userId).onSuccess { url ->
                perfilRepository.updateFotoPerfil(userId, url)
                _uiState.value = _uiState.value.copy(isSaving = false, message = "Foto actualizada")
                loadPerfil()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun sendPasswordReset() {
        val email = userEmail
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "No hay correo asociado a la cuenta")
            return
        }
        viewModelScope.launch {
            authRepository.sendPasswordReset(email)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(message = "Te enviamos un correo para cambiar tu contraseña")
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(message = null, error = null) }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onDone()
        }
    }
}
