package com.example.androidspaceproject.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Detail : Screen("launch_detail/{launchId}") {
        fun createRoute(launchId: String) = "launch_detail/$launchId"
    }
}

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Launches : BottomBarScreen(
        route = "launches_tab",
        title = "Запуски",
        icon = Icons.Default.RocketLaunch
    )

    object Favorites : BottomBarScreen(
        route = "favorites_tab",
        title = "Избранное",
        icon = Icons.Default.Favorite
    )
}

val bottomBarItems = listOf(
    BottomBarScreen.Launches,
    BottomBarScreen.Favorites
)
