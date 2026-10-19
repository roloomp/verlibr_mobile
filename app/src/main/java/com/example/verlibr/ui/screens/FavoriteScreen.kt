package com.example.verlibr.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.verlibr.ui.viewmodels.FavoriteViewModel

@Composable
fun FavoriteScreen(
    onPoemClick: (String) -> Unit,
    viewModel: FavoriteViewModel = viewModel()
) {
    PoemListScreen(viewModel, onPoemClick)
}
