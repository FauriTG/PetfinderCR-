package com.petfindercr.ui.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petfindercr.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotUiState())
    val uiState: StateFlow<ForgotUiState> = _uiState.asStateFlow()

    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v) }

    fun sendReset() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
            val result = authRepository.sendPasswordReset(_uiState.value.email.trim())
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(isLoading = false, message = "Correo enviado. Revisa tu bandeja de entrada.")
            } else {
                _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Error al enviar correo")
            }
        }
    }
}
