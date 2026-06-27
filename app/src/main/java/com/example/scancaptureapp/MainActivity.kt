package com.example.scancaptureapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.scancaptureapp.data.local.LanguagePreferences
import com.example.scancaptureapp.localization.LanguageManager
import com.example.scancaptureapp.localization.LocaleManager
import com.example.scancaptureapp.presentation.navigation.ScanCaptureNavGraph
import com.example.scancaptureapp.ui.theme.ScanCaptureAppTheme
import com.example.scancaptureapp.utils.ImageFileManager
import com.example.scancaptureapp.utils.StartupSafety
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imageFileManager: ImageFileManager

    @Inject
    lateinit var languageManager: LanguageManager

    override fun attachBaseContext(newBase: Context) {
        val languageCode = LanguagePreferences.resolveLanguageCodeSync(newBase)
        val wrapped = LocaleManager.setLocale(newBase, languageCode)
        super.attachBaseContext(wrapped)
        Log.d(TAG, "attachBaseContext — locale=$languageCode")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = LanguagePreferences.resolveLanguageCodeSync(this)
        Log.d(TAG, "onCreate — language=$languageCode")

        lifecycleScope.launch {
            StartupSafety.runCatchingStartupSuspend("language.initialize", Unit) {
                languageManager.initialize()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                languageManager.localeChanged.collect {
                    Log.d(TAG, "Locale changed — recreating activity")
                    recreate()
                }
            }
        }

        enableEdgeToEdge()

        try {
            setContent {
                // Force full Compose tree refresh when language changes after recreate
                key(languageCode) {
                    ScanCaptureAppTheme {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            val navController = rememberNavController()
                            ScanCaptureNavGraph(
                                navController = navController,
                                imageFileManager = imageFileManager
                            )
                        }
                    }
                }
            }
            Log.d(TAG, "onCreate — setContent completed")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate — setContent failed, showing fallback UI", e)
            setContent {
                ScanCaptureAppTheme {
                    StartupErrorScreen()
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
private fun StartupErrorScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.startup_error_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}
