package com.example.scancaptureapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scancaptureapp.domain.model.User
import com.example.scancaptureapp.domain.usecase.GetCurrentUserUseCase
import com.example.scancaptureapp.domain.usecase.LogoutUseCase
import com.example.scancaptureapp.localization.AppLanguage
import com.example.scancaptureapp.localization.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val languageManager: LanguageManager
) : ViewModel() {

    private val _user = MutableStateFlow(getCurrentUserUseCase())
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    val currentLanguage: StateFlow<AppLanguage> = languageManager.currentLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = languageManager.getCurrentLanguage()
    )

    fun setLanguage(language: AppLanguage) {
        languageManager.setLocale(language)
    }

    fun logout() {
        logoutUseCase()
        _loggedOut.value = true
    }
}
