package com.petfindercr.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petfindercr.data.repository.AuthRepository
import com.petfindercr.data.repository.MensajeRepository
import com.petfindercr.data.repository.PerfilRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Un resumen de conversación con otra persona. */
data class Conversacion(
    val otroId: String,
    val otroNombre: String,
    val otroFoto: String?,
    val ultimoMensaje: String,
    val fecha: String?
)

data class InboxUiState(
    val conversaciones: List<Conversacion> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val mensajeRepository: MensajeRepository,
    private val perfilRepository: PerfilRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    private val userId get() = authRepository.currentUser?.id ?: ""

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            mensajeRepository.getTodos().onSuccess { mensajes ->
                // Agrupa por el "otro" participante, tomando el mensaje más reciente.
                // getTodos() viene ordenado por fecha desc, así que el primero de cada grupo es el último.
                val porOtro = LinkedHashMap<String, com.petfindercr.data.model.Mensaje>()
                for (m in mensajes) {
                    val otro = if (m.emisorId == userId) m.receptorId else m.emisorId
                    if (otro != null && otro !in porOtro) porOtro[otro] = m
                }

                // Trae el perfil de cada participante para mostrar nombre y foto.
                val convs = porOtro.map { (otroId, ultimo) ->
                    val perfil = perfilRepository.getPerfil(otroId).getOrNull()
                    Conversacion(
                        otroId = otroId,
                        otroNombre = perfil?.nombre ?: "Usuario",
                        otroFoto = perfil?.fotoPerfil,
                        ultimoMensaje = ultimo.mensaje,
                        fecha = ultimo.fechaEnvio
                    )
                }
                _uiState.value = InboxUiState(conversaciones = convs, isLoading = false)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
