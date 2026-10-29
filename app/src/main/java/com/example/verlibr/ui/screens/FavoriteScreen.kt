package com.example.verlibr.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.verlibr.ui.components.GuestHint
import com.example.verlibr.ui.viewmodels.FavoriteViewModel

@Composable
fun FavoriteScreen(
    userId: String?,
    loading: Boolean,
    onPoemClick: (String) -> Unit,
    onLoginClick: () -> Unit,
    viewModel: FavoriteViewModel = viewModel()
) {
    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        userId == null -> GuestHint("Войдите, чтобы видеть избранное", onLoginClick)
        else -> {
            LaunchedEffect(userId) { viewModel.setUser(userId) }
            PoemListScreen(viewModel, onPoemClick)
        }
    }
}
