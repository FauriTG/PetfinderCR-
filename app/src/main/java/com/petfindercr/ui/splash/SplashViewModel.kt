package com.petfindercr.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petfindercr.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun checkSession(onLoggedIn: () -> Unit, onNotLoggedIn: () -> Unit) {
        viewModelScope.launch {
            delay(1500L)
            try {
                authRepository.refreshSession()
                if (authRepository.isLoggedIn) onLoggedIn() else onNotLoggedIn()
            } catch (e: Exception) {
                onNotLoggedIn()
            }
        }
    }
}
