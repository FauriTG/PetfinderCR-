package com.petfindercr.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petfindercr.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) { _uiState.value = _uiState.value.copy(email = value) }
    fun onPasswordChange(value: String) { _uiState.value = _uiState.value.copy(password = value) }

    fun signIn() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.signIn(_uiState.value.email.trim(), _uiState.value.password)
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(isLoading = false, success = true)
            } else {
                _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
