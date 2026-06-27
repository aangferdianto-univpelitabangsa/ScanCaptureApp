package com.example.scancaptureapp.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scancaptureapp.R
import com.example.scancaptureapp.domain.usecase.LoginUseCase
import com.example.scancaptureapp.domain.usecase.RegisterUseCase
import com.example.scancaptureapp.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null, errorMessage = null) }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null) }

    fun login() {
        val state = _uiState.value
        val emailError = ValidationUtils.validateEmail(context, state.email)
        val passwordError = ValidationUtils.validatePassword(context, state.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loginUseCase(state.email.trim(), state.password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: context.getString(R.string.error_login_failed)
                        )
                    }
                }
        }
    }

    fun register() {
        val state = _uiState.value
        val nameError = ValidationUtils.validateName(context, state.name)
        val emailError = ValidationUtils.validateEmail(context, state.email)
        val passwordError = ValidationUtils.validatePassword(context, state.password)
        val confirmError = ValidationUtils.validateConfirmPassword(
            context,
            state.password,
            state.confirmPassword
        )
        if (nameError != null || emailError != null || passwordError != null || confirmError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            registerUseCase(state.name.trim(), state.email.trim(), state.password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: context.getString(R.string.error_registration_failed)
                        )
                    }
                }
        }
    }

    fun clearSuccess() = _uiState.update { it.copy(isSuccess = false) }
}
