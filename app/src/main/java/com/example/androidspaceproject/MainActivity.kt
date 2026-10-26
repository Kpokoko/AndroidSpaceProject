package com.example.androidspaceproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidspaceproject.data.api.RetrofitInstance
import com.example.androidspaceproject.ui.detail.LaunchDetailScreen
import com.example.androidspaceproject.ui.favorites.FavoritesScreen
import com.example.androidspaceproject.ui.launches.LaunchesListScreen
import com.example.androidspaceproject.ui.navigation.BottomNavigationBar
import com.example.androidspaceproject.ui.navigation.BottomBarScreen
import com.example.androidspaceproject.ui.navigation.Screen
import com.example.androidspaceproject.ui.navigation.bottomBarItems
import com.example.androidspaceproject.ui.theme.AndroidSpaceProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        RetrofitInstance.init(applicationContext)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidSpaceProjectTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomBarItems.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomBarScreen.Launches.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomBarScreen.Launches.route) {
                LaunchesListScreen(
                    onLaunchClick = { launchId ->
                        navController.navigate(Screen.Detail.createRoute(launchId))
                    }
                )
            }

            composable(BottomBarScreen.Favorites.route) {
                FavoritesScreen()
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("launchId") { type = NavType.StringType })
            ) { backStackEntry ->
                val launchId = backStackEntry.arguments?.getString("launchId") ?: ""
                LaunchDetailScreen(
                    launchId = launchId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
