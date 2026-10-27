package com.example.verlibr.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.verlibr.ui.components.EmptyHint
import com.example.verlibr.ui.components.Pagination
import com.example.verlibr.ui.components.PoemCard
import com.example.verlibr.ui.components.SheetButton
import com.example.verlibr.ui.theme.Taupe
import com.example.verlibr.ui.viewmodels.PagedPoemsViewModel

@Composable
fun PoemListScreen(
    viewModel: PagedPoemsViewModel,
    onPoemClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) { viewModel.refresh() }
    LaunchedEffect(viewModel.page) { listState.scrollToItem(0) }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when {
            viewModel.error -> item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Не удалось загрузить стихи", fontSize = 16.sp, color = Taupe)
                    SheetButton(
                        text = "Повторить",
                        filled = true,
                        modifier = Modifier.fillMaxWidth(0.6f),
                        onClick = viewModel::refresh
                    )
                }
            }
            viewModel.loading && viewModel.poems.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            viewModel.poems.isEmpty() -> item { EmptyHint() }
            else -> items(viewModel.poems, key = { it.id }) { poem ->
                PoemCard(poem) { onPoemClick(poem.id) }
            }
        }
        if (!viewModel.error) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Pagination(viewModel.page, viewModel.pageCount, viewModel::openPage)
                }
            }
        }
    }
}
