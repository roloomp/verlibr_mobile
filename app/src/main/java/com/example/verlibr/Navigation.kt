package com.example.verlibr

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun VerlibrNavigator() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            TODO()
        }

        composable("poem") {
            TODO()
        }

        composable("favorite") {
            TODO()
        }

        composable("hot") {
            TODO()
        }

        composable("profile") {
            TODO()
        }

        composable("search") {
            TODO()
        }
    }
}

