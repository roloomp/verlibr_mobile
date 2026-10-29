package com.example.verlibr

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.verlibr.ui.components.AppHeader
import com.example.verlibr.ui.components.AuthDialogs
import com.example.verlibr.ui.components.BottomBar
import com.example.verlibr.ui.components.BottomTab
import com.example.verlibr.ui.components.rememberAuthController
import com.example.verlibr.ui.screens.AuthorScreen
import com.example.verlibr.ui.screens.FavoriteScreen
import com.example.verlibr.ui.screens.HomeScreen
import com.example.verlibr.ui.screens.HotScreen
import com.example.verlibr.ui.screens.PoemScreen
import com.example.verlibr.ui.screens.ProfileScreen
import com.example.verlibr.ui.screens.SearchScreen
import com.example.verlibr.ui.viewmodels.AuthState
import com.example.verlibr.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

private val tabs = listOf(
    BottomTab("hot", "Горячее", R.drawable.ic_fire),
    BottomTab("favorite", "Избранное", R.drawable.ic_star),
    BottomTab("home", "Главная", R.drawable.ic_home),
    BottomTab("search", "Поиск", R.drawable.ic_search),
    BottomTab("profile", "Профиль", R.drawable.ic_profile)
)

@Composable
fun VerlibrNavigator(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val authController = rememberAuthController()
    val scope = rememberCoroutineScope()

    val profile = authViewModel.profile
    val authLoading = authViewModel.state is AuthState.Loading

    fun openTab(route: String) {
        navController.navigate(route) {
            popUpTo("home")
            launchSingleTop = true
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                userName = profile?.name,
                loading = authLoading,
                onLogoClick = { openTab("home") },
                onLoginClick = authController::openLogin,
                onProfileClick = { openTab("profile") }
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
                    userId = authViewModel.userId,
                    loading = authLoading,
                    onPoemClick = {navController.navigate("poem/$it")},
                    onLoginClick = authController::openLogin
                )
            }
            composable("search") {
                SearchScreen(
                    onPoemClick = {navController.navigate("poem/$it")}
                )
            }
            composable("profile") {
                ProfileScreen(
                    profile = profile,
                    loading = authLoading,
                    onPoemClick = {navController.navigate("poem/$it")},
                    onLoginClick = authController::openLogin,
                    onLogout = {
                        scope.launch {
                            val ok = authViewModel.logout()
                            snackbarHostState.showSnackbar(
                                if (ok) "Вы вышли из аккаунта" else "Не удалось выйти"
                            )
                        }
                    }
                )
            }
            composable("poem/{id}") {
                PoemScreen(
                    userId = authViewModel.userId,
                    onLoginRequired = authController::openLogin
                )
            }
            composable("author/{id}") {
                AuthorScreen(onPoemClick = { navController.navigate("poem/$it") })
            }
        }
    }

    AuthDialogs(
        controller = authController,
        snackbarHostState = snackbarHostState,
        onLogin = { email, password -> authViewModel.login(email, password) },
        onRegister = { name, email, password -> authViewModel.register(name, email, password) }
    )
}
