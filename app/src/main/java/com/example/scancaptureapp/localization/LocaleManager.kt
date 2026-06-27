package com.example.scancaptureapp.localization

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Central locale handling: applies [Configuration] + [AppCompatDelegate] so Compose
 * [androidx.compose.ui.res.stringResource] picks up the correct strings.
 */
object LocaleManager {

    private const val TAG = "LocaleManager"

    /**
     * Applies locale globally and returns a wrapped [Context] for [attachBaseContext].
     */
    fun setLocale(context: Context, languageCode: String): Context {
        return try {
            val locale = AppLanguage.fromCode(languageCode).toLocale()
            applyLocale(locale)
            wrapContext(context, locale)
        } catch (e: Exception) {
            Log.e(TAG, "setLocale failed for code=$languageCode", e)
            val fallback = AppLanguage.INDONESIAN.toLocale()
            applyLocale(fallback)
            wrapContext(context, fallback)
        }
    }

    fun getLocale(context: Context): AppLanguage {
        return try {
            val locale = context.resources.configuration.locales[0]
            AppLanguage.fromCode(locale.language)
        } catch (e: Exception) {
            Log.e(TAG, "getLocale failed", e)
            AppLanguage.INDONESIAN
        }
    }

    fun getLanguageCode(context: Context): String = getLocale(context).code

    private fun applyLocale(locale: Locale) {
        Locale.setDefault(locale)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
        Log.d(TAG, "applyLocale — ${locale.language}")
    }

    private fun wrapContext(context: Context, locale: Locale): Context {
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(android.os.LocaleList(locale))
        }
        return context.createConfigurationContext(config)
    }
}
