package com.example.scancaptureapp.presentation.screens.main

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.scancaptureapp.presentation.components.BackgroundGradient
import com.example.scancaptureapp.presentation.components.glassSurfaceColor
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.scancaptureapp.R
import com.example.scancaptureapp.presentation.navigation.Screen
import com.example.scancaptureapp.presentation.screens.history.HistoryScreen
import com.example.scancaptureapp.presentation.screens.home.HomeScreen
import com.example.scancaptureapp.presentation.screens.profile.ProfileScreen

private data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    @StringRes val labelRes: Int
)

@Composable
fun MainScreen(
    onOpenCamera: () -> Unit,
    onHistoryItemClick: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomItems = listOf(
        BottomNavItem(Screen.Home, Icons.Default.Home, R.string.nav_home),
        BottomNavItem(Screen.History, Icons.Default.History, R.string.nav_history),
        BottomNavItem(Screen.Profile, Icons.Default.Person, R.string.nav_profile)
    )

    BackgroundGradient {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = glassSurfaceColor(),
                    tonalElevation = 8.dp
                ) {
                    bottomItems.forEach { (screen, icon, labelRes) ->
                        val label = stringResource(labelRes)
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onOpenCamera = onOpenCamera,
                        snackbarHostState = snackbarHostState
                    )
                }
                composable(Screen.History.route) {
                    HistoryScreen(onItemClick = onHistoryItemClick)
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(onLogout = onLogout)
                }
            }
        }
    }
}
