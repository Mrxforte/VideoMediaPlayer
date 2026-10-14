package com.videomediaplayer.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.videomediaplayer.app.presentation.screens.home.HomeScreen
import com.videomediaplayer.app.presentation.screens.player.PlayerScreen
import com.videomediaplayer.app.presentation.screens.settings.SettingsScreen
import com.videomediaplayer.app.presentation.screens.folders.FolderDetailScreen
import com.videomediaplayer.app.presentation.screens.vault.SafeVaultScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Player : Screen("player/{mediaId}/{uri}") {
        fun createRoute(mediaId: Long, uri: String) = "player/$mediaId/${android.net.Uri.encode(uri)}"
    }
    data object Settings : Screen("settings")
    data object Vault : Screen("vault")
    data object FolderDetail : Screen("folder/{folderPath}/{folderName}") {
        fun createRoute(path: String, name: String) =
            "folder/${android.net.Uri.encode(path)}/${android.net.Uri.encode(name)}"
    }
}

@Composable
fun AppNavigation(
    onEnterPip: () -> Unit,
    onHideSystemBars: () -> Unit,
    onShowSystemBars: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToPlayer = { item ->
                    navController.navigate(
                        Screen.Player.createRoute(item.id, item.uri.toString())
                    )
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToVault = {
                    navController.navigate(Screen.Vault.route)
                },
                onNavigateToFolder = { folder ->
                    navController.navigate(
                        Screen.FolderDetail.createRoute(folder.path, folder.name)
                    )
                }
            )
        }

        composable(Screen.Vault.route) {
            SafeVaultScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { item ->
                    navController.navigate(
                        Screen.Player.createRoute(item.id, item.uri.toString())
                    )
                }
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("mediaId") { type = NavType.LongType },
                navArgument("uri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getLong("mediaId") ?: 0L
            val uriString = backStackEntry.arguments?.getString("uri") ?: ""
            PlayerScreen(
                mediaId = mediaId,
                uriString = uriString,
                onBack = {
                    onShowSystemBars()
                    navController.popBackStack()
                },
                onEnterPip = onEnterPip,
                onHideSystemBars = onHideSystemBars,
                onShowSystemBars = onShowSystemBars
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.FolderDetail.route,
            arguments = listOf(
                navArgument("folderPath") { type = NavType.StringType },
                navArgument("folderName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val path = android.net.Uri.decode(backStackEntry.arguments?.getString("folderPath") ?: "")
            val name = android.net.Uri.decode(backStackEntry.arguments?.getString("folderName") ?: "")
            FolderDetailScreen(
                folderPath = path,
                folderName = name,
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { item ->
                    navController.navigate(
                        Screen.Player.createRoute(item.id, item.uri.toString())
                    )
                }
            )
        }
    }
}
