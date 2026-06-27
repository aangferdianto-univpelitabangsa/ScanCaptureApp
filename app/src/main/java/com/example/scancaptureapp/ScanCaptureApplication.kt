package com.example.scancaptureapp

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.scancaptureapp.data.local.LanguagePreferences
import com.example.scancaptureapp.localization.LocaleManager
import com.example.scancaptureapp.utils.StartupSafety
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ScanCaptureApplication : Application() {

    override fun attachBaseContext(base: Context) {
        val languageCode = LanguagePreferences.ensureFirstLaunchLanguage(base)
        val wrapped = LocaleManager.setLocale(base, languageCode)
        super.attachBaseContext(wrapped)
        Log.d(TAG, "attachBaseContext — locale=$languageCode")
    }

    override fun onCreate() {
        StartupSafety.installGlobalHandlers()
        super.onCreate()
        Log.d(TAG, "onCreate — application started")
    }

    companion object {
        private const val TAG = "ScanCaptureApp"
    }
}
