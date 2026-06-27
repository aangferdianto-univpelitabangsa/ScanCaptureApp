package com.example.scancaptureapp.data.local

import android.content.Context
import android.util.Log
import com.example.scancaptureapp.localization.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists language code ("id" / "en") via SharedPreferences.
 * Uses [commit] (not [apply]) so the value is saved before [android.app.Activity.recreate].
 */
@Singleton
class LanguagePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _languageCode = MutableStateFlow(readLanguageCodeFromPrefs())

    val languageCode: Flow<String> = _languageCode.asStateFlow()

    fun getLanguageCodeSync(): String = readLanguageCodeFromPrefs().also {
        _languageCode.value = it
    }

    suspend fun getLanguageCode(): String = getLanguageCodeSync()

    fun hasSavedLanguage(): Boolean = prefs.contains(KEY_LANGUAGE_CODE)

    /**
     * Synchronous save — must complete before activity recreate.
     */
    fun setLanguageCodeSync(code: String): Boolean {
        val saved = prefs.edit().putString(KEY_LANGUAGE_CODE, code).commit()
        if (saved) {
            _languageCode.value = code
            Log.d(TAG, "Language saved: $code")
        } else {
            Log.e(TAG, "Failed to save language: $code")
        }
        return saved
    }

    suspend fun setLanguageCode(code: String) {
        setLanguageCodeSync(code)
    }

    suspend fun initializeIfNeeded() {
        if (!hasSavedLanguage()) {
            val detected = AppLanguage.detectSystemLanguage().code
            setLanguageCodeSync(detected)
            Log.d(TAG, "First launch — detected system language: $detected")
        } else {
            _languageCode.value = readLanguageCodeFromPrefs()
        }
    }

    private fun readLanguageCodeFromPrefs(): String =
        prefs.getString(KEY_LANGUAGE_CODE, null) ?: AppLanguage.INDONESIAN.code

    companion object {
        private const val TAG = "LanguagePreferences"
        private const val PREFS_NAME = "language_preferences"
        private const val KEY_LANGUAGE_CODE = "language_code"

        fun resolveLanguageCodeSync(context: Context): String {
            return try {
                val appContext = context.applicationContext
                val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                preferences.getString(KEY_LANGUAGE_CODE, null)
                    ?: AppLanguage.INDONESIAN.code
            } catch (e: Exception) {
                Log.e(TAG, "resolveLanguageCodeSync failed, using Indonesian", e)
                AppLanguage.INDONESIAN.code
            }
        }

        fun ensureFirstLaunchLanguage(context: Context): String {
            return try {
                val appContext = context.applicationContext
                val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val saved = preferences.getString(KEY_LANGUAGE_CODE, null)
                if (saved != null) {
                    saved
                } else {
                    val detected = AppLanguage.detectSystemLanguage().code
                    preferences.edit().putString(KEY_LANGUAGE_CODE, detected).commit()
                    Log.d(TAG, "First launch — saved language: $detected")
                    detected
                }
            } catch (e: Exception) {
                Log.e(TAG, "ensureFirstLaunchLanguage failed", e)
                AppLanguage.INDONESIAN.code
            }
        }
    }
}
