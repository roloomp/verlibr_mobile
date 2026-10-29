package com.example.verlibr.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.verlibr.ui.components.AuthorHeader
import com.example.verlibr.ui.viewmodels.AuthorViewModel

@Composable
fun AuthorScreen(
    onPoemClick: (String) -> Unit,
    viewModel: AuthorViewModel = viewModel()
) {
    PoemListScreen(
        viewModel = viewModel,
        onPoemClick = onPoemClick,
        header = {
            AuthorHeader(
                author = viewModel.author,
                loading = viewModel.authorLoading,
                error = viewModel.authorError,
                onRetry = viewModel::loadAuthor
            )
        }
    )
}
