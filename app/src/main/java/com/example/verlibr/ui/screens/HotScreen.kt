package com.example.verlibr.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.verlibr.ui.viewmodels.HotViewModel

@Composable
fun HotScreen(
    onPoemClick: (String) -> Unit,
    viewModel: HotViewModel = viewModel()
) {
    PoemListScreen(viewModel, onPoemClick)
}
