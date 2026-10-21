package com.example.verlibr

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.verlibr.ui.components.AppHeader
import com.example.verlibr.ui.components.AuthDialogs
import com.example.verlibr.ui.components.BottomBar
import com.example.verlibr.ui.components.BottomTab
import com.example.verlibr.ui.components.rememberAuthController
import com.example.verlibr.ui.screens.FavoriteScreen
import com.example.verlibr.ui.screens.HomeScreen
import com.example.verlibr.ui.screens.HotScreen
import com.example.verlibr.ui.screens.PlaceholderScreen
import com.example.verlibr.ui.screens.PoemScreen

private val tabs = listOf(
    BottomTab("hot", "Горячее", R.drawable.ic_fire),
    BottomTab("favorite", "Избранное", R.drawable.ic_star),
    BottomTab("home", "Главная", R.drawable.ic_home),
    BottomTab("search", "Поиск", R.drawable.ic_search),
    BottomTab("profile", "Профиль", R.drawable.ic_profile)
)

@Composable
fun VerlibrNavigator() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val authController = rememberAuthController()

    fun openTab(route: String) {
        navController.navigate(route) {
            popUpTo("home")
            launchSingleTop = true
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                onLogoClick = { openTab("home") },
                onLoginClick = authController::openLogin
            )
        },
        bottomBar = {
            BottomBar(
                tabs = tabs,
                currentRoute = backStackEntry?.destination?.route,
                onTabClick = { openTab(it.route) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(
                    onPoemClick = {navController.navigate("poem/$it")},
                    onAuthorClick = {navController.navigate("author/$it")},
                    onSearchClick = {openTab("search") }
                )
            }
            composable("hot") {
                HotScreen(
                    onPoemClick = {navController.navigate("poem/$it")}
                )
            }
            composable("favorite") {
                FavoriteScreen(
                    onPoemClick = {navController.navigate("poem/$it")}
                )
            }
            composable("search") { PlaceholderScreen("Поиск") }
            composable("profile") { PlaceholderScreen("Профиль") }
            composable("poem/{id}") { PoemScreen() }
            composable("author/{id}") { PlaceholderScreen("Автор") }
        }
    }

    AuthDialogs(controller = authController, snackbarHostState = snackbarHostState)
}
