package com.example.scancaptureapp.presentation.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.scancaptureapp.presentation.screens.auth.LoginScreen
import com.example.scancaptureapp.presentation.screens.auth.RegisterScreen
import com.example.scancaptureapp.presentation.screens.camera.CameraScreen
import com.example.scancaptureapp.presentation.screens.crop.CropScreen
import com.example.scancaptureapp.presentation.screens.history.ScanDetailScreen
import com.example.scancaptureapp.presentation.screens.main.MainScreen
import com.example.scancaptureapp.presentation.screens.splash.SplashScreen
import com.example.scancaptureapp.presentation.viewmodel.HomeViewModel
import com.example.scancaptureapp.utils.ImageFileManager
import java.io.File
import android.util.Base64

@Composable
fun ScanCaptureNavGraph(
    navController: NavHostController,
    imageFileManager: ImageFileManager,
    startDestination: String = Screen.Splash.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onOpenCamera = { navController.navigate(Screen.Camera.route) },
                onHistoryItemClick = { id ->
                    navController.navigate(Screen.ScanDetail.createRoute(id))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { path ->
                    navController.navigate(Screen.Crop.createRoute(path))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Crop.route,
            arguments = listOf(navArgument("imagePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val activity = LocalContext.current as ComponentActivity
            val homeViewModel: HomeViewModel = hiltViewModel(activity)
            val encoded = backStackEntry.arguments?.getString("imagePath").orEmpty()
            val imagePath = remember(encoded) {
                String(
                    Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP),
                    Charsets.UTF_8
                )
            }
            CropScreen(
                imagePath = imagePath,
                imageFileManager = imageFileManager,
                onCropComplete = { croppedPath ->
                    val uri = imageFileManager.getUriForFile(File(croppedPath))
                    homeViewModel.setCapturedImage(croppedPath, uri, showPreview = true)
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Camera.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ScanDetail.route,
            arguments = listOf(navArgument("scanId") { type = NavType.LongType })
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getLong("scanId") ?: 0L
            ScanDetailScreen(
                scanId = scanId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
