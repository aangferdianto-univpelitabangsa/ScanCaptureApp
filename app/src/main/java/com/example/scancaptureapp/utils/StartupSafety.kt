package com.example.scancaptureapp.utils

import android.util.Log

/**
 * Global handlers and helpers to keep the app from force-closing on startup.
 */
object StartupSafety {

    private const val TAG = "StartupSafety"

    fun installGlobalHandlers() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception on thread=${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    fun <T> runCatchingStartup(
        step: String,
        default: T,
        block: () -> T
    ): T {
        return try {
            block().also { Log.d(TAG, "OK: $step") }
        } catch (e: Exception) {
            Log.e(TAG, "FAILED: $step — using fallback", e)
            default
        }
    }

    suspend fun <T> runCatchingStartupSuspend(
        step: String,
        default: T,
        block: suspend () -> T
    ): T {
        return try {
            block().also { Log.d(TAG, "OK: $step") }
        } catch (e: Exception) {
            Log.e(TAG, "FAILED: $step — using fallback", e)
            default
        }
    }
}
