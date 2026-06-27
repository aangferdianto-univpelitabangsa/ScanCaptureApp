package com.example.scancaptureapp.localization

import android.content.Context

/**
 * @see LocaleManager
 */
object LocaleHelper {

    fun wrap(context: Context, languageCode: String): Context =
        LocaleManager.setLocale(context, languageCode)

    fun getCurrentLanguage(context: Context): AppLanguage =
        LocaleManager.getLocale(context)
}
