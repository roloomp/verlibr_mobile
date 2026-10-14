package com.example.verlibr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.verlibr.ui.components.AppHeader
import com.example.verlibr.ui.components.AuthDialogs
import com.example.verlibr.ui.components.rememberAuthController


@Composable
fun MyScreen(modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val authController = rememberAuthController()
    Scaffold(
        topBar = {
            AppHeader(
                onLogoClick = { },
                onLoginClick = { authController.openLogin() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

        }
    }

    AuthDialogs(
        controller = authController,
        snackbarHostState = snackbarHostState
    )
}