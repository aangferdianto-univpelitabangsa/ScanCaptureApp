package com.example.scancaptureapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scancaptureapp.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                delay(SPLASH_DELAY_MS)
                val loggedIn = runCatching { getCurrentUserUseCase() != null }
                    .onFailure { e ->
                        Log.e(TAG, "Auth check failed — routing to login", e)
                    }
                    .getOrDefault(false)
                Log.d(TAG, "Splash complete — loggedIn=$loggedIn")
                _uiState.value = SplashUiState(isLoading = false, isLoggedIn = loggedIn)
            } catch (e: Exception) {
                Log.e(TAG, "Splash init failed — routing to login", e)
                _uiState.value = SplashUiState(isLoading = false, isLoggedIn = false)
            }
        }
    }

    companion object {
        private const val TAG = "SplashViewModel"
        private const val SPLASH_DELAY_MS = 1500L
    }
}
