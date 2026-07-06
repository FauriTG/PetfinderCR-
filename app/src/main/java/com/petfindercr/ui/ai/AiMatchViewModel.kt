package com.petfindercr.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petfindercr.data.model.EstadoReporte
import com.petfindercr.data.repository.AuthRepository
import com.petfindercr.data.repository.ReporteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiMatchUiState(
    val matches: List<PetMatch> = emptyList(),
    val misPerdidasCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AiMatchViewModel @Inject constructor(
    private val reporteRepository: ReporteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiMatchUiState())
    val uiState: StateFlow<AiMatchUiState> = _uiState.asStateFlow()

    private val userId get() = authRepository.currentUser?.id ?: ""

    init { analizar() }

    fun analizar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            reporteRepository.getReportes().onSuccess { todos ->
                val misPerdidas = todos.count {
                    it.usuarioId == userId && it.estado == EstadoReporte.PERDIDA.name
                }
                val matches = MatchEngine.findMatches(userId, todos)
                _uiState.value = AiMatchUiState(
                    matches = matches,
                    misPerdidasCount = misPerdidas,
                    isLoading = false
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
