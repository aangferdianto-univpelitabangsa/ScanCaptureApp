package com.example.scancaptureapp.localization

import android.content.Context
import android.util.Log
import com.example.scancaptureapp.data.local.LanguagePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageManager @Inject constructor(
    private val languagePreferences: LanguagePreferences,
    @ApplicationContext private val context: Context
) {

    val currentLanguage: Flow<AppLanguage> = languagePreferences.languageCode.map {
        AppLanguage.fromCode(it)
    }

    private val _localeChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val localeChanged: SharedFlow<Unit> = _localeChanged.asSharedFlow()

    suspend fun initialize() {
        languagePreferences.initializeIfNeeded()
        val code = languagePreferences.getLanguageCodeSync()
        LocaleManager.setLocale(context, code)
        Log.d(TAG, "initialize — language=$code")
    }

    fun getCurrentLanguage(): AppLanguage = LocaleManager.getLocale(context)

    /**
     * Saves language, applies locale globally, then signals activity to recreate.
     */
    fun setLocale(language: AppLanguage) {
        val current = languagePreferences.getLanguageCodeSync()
        if (current == language.code) {
            Log.d(TAG, "setLocale — already ${language.code}")
            return
        }

        val saved = languagePreferences.setLanguageCodeSync(language.code)
        if (!saved) {
            Log.e(TAG, "setLocale — save failed, aborting")
            return
        }

        LocaleManager.setLocale(context, language.code)
        Log.d(TAG, "setLocale — applied ${language.code}, requesting recreate")
        _localeChanged.tryEmit(Unit)
    }

    companion object {
        private const val TAG = "LanguageManager"
    }
}
