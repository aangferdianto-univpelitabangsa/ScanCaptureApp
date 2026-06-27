package com.example.scancaptureapp.presentation.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Main : Screen("main")
    data object Camera : Screen("camera")
    data object Crop : Screen("crop/{imagePath}") {
        fun createRoute(imagePath: String): String {
            val encoded = android.util.Base64.encodeToString(
                imagePath.toByteArray(Charsets.UTF_8),
                android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
            )
            return "crop/$encoded"
        }
    }
    data object ScanDetail : Screen("scan_detail/{scanId}") {
        fun createRoute(scanId: Long) = "scan_detail/$scanId"
    }

    // Bottom nav destinations (nested in Main)
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Profile : Screen("profile")
}
