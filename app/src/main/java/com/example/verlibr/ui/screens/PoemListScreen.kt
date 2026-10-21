package com.example.verlibr.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.verlibr.ui.components.Pagination
import com.example.verlibr.ui.components.PoemCard
import com.example.verlibr.ui.viewmodels.PagedPoemsViewModel

@Composable
fun PoemListScreen(
    viewModel: PagedPoemsViewModel,
    onPoemClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.page) { listState.scrollToItem(0) }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(viewModel.poems, key = { it.id }) { poem ->
            PoemCard(poem) { onPoemClick(poem.id) }
        }
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Pagination(viewModel.page, viewModel.pageCount, viewModel::openPage)
            }
        }
    }
}
