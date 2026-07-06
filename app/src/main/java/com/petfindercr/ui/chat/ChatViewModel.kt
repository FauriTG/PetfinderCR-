package com.petfindercr.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petfindercr.data.model.Mensaje
import com.petfindercr.data.repository.AuthRepository
import com.petfindercr.data.repository.MensajeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val mensajes: List<Mensaje> = emptyList(),
    val input: String = "",
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val mensajeRepository: MensajeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val currentUserId get() = authRepository.currentUser?.id ?: ""
    private var otroId: String = ""
    private var polling = false

    fun start(receptorId: String) {
        otroId = receptorId
        cargar(showLoading = true)
        iniciarPolling()
    }

    private fun cargar(showLoading: Boolean) {
        viewModelScope.launch {
            if (showLoading) _uiState.value = _uiState.value.copy(isLoading = true)
            mensajeRepository.getConversacion(otroId).onSuccess { lista ->
                _uiState.value = _uiState.value.copy(isLoading = false, mensajes = lista, error = null)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    /** Refresco ligero cada 5s para ver mensajes nuevos (chat sencillo sin realtime). */
    private fun iniciarPolling() {
        if (polling) return
        polling = true
        viewModelScope.launch {
            while (true) {
                delay(5000)
                mensajeRepository.getConversacion(otroId).onSuccess { lista ->
                    if (lista.size != _uiState.value.mensajes.size) {
                        _uiState.value = _uiState.value.copy(mensajes = lista)
                    }
                }
            }
        }
    }

    fun onInputChange(v: String) { _uiState.value = _uiState.value.copy(input = v) }

    fun enviar() {
        val texto = _uiState.value.input.trim()
        if (texto.isBlank() || otroId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true, input = "")
            mensajeRepository.enviar(currentUserId, otroId, texto).onSuccess {
                cargar(showLoading = false)
                _uiState.value = _uiState.value.copy(isSending = false)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isSending = false, input = texto, error = e.message)
            }
        }
    }
}
