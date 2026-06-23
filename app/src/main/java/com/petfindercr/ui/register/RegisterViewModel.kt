package com.petfindercr.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petfindercr.data.model.Perfil
import com.petfindercr.data.repository.AuthRepository
import com.petfindercr.data.repository.PerfilRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val perfilRepository: PerfilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNombreChange(v: String) { _uiState.value = _uiState.value.copy(nombre = v) }
    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v) }
    fun onTelefonoChange(v: String) { _uiState.value = _uiState.value.copy(telefono = v) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v) }
    fun onConfirmPasswordChange(v: String) { _uiState.value = _uiState.value.copy(confirmPassword = v) }

    fun register() {
        val s = _uiState.value
        if (s.password != s.confirmPassword) {
            _uiState.value = s.copy(error = "Las contraseñas no coinciden")
            return
        }
        if (s.nombre.isBlank()) {
            _uiState.value = s.copy(error = "El nombre es requerido")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.signUp(s.email.trim(), s.password, s.nombre)
            if (result.isSuccess) {
                val userId = authRepository.currentUser?.id ?: ""
                perfilRepository.createPerfil(
                    Perfil(id = userId, nombre = s.nombre, telefono = s.telefono.takeIf { it.isNotBlank() })
                )
                _uiState.value = _uiState.value.copy(isLoading = false, success = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al registrarse"
                )
            }
        }
    }
}
