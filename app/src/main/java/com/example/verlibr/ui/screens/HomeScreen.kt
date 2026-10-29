package com.example.verlibr.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.verlibr.ui.components.AuthorRow
import com.example.verlibr.ui.components.PoemRow
import com.example.verlibr.ui.components.SearchPill
import com.example.verlibr.ui.components.SectionCard
import com.example.verlibr.ui.components.SheetButton
import com.example.verlibr.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    onPoemClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { Hero(onSearchClick) }
        when {
            viewModel.error && !viewModel.hasData -> item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Не удалось загрузить главную", fontSize = 16.sp)
                    SheetButton(
                        text = "Повторить",
                        filled = true,
                        modifier = Modifier.fillMaxWidth(0.6f),
                        onClick = viewModel::load
                    )
                }
            }
            viewModel.loading && !viewModel.hasData -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            else -> {
                if (viewModel.poemsOfTheDay.isNotEmpty()) item {
                    SectionCard("Находки дня", viewModel.poemsOfTheDay) { PoemRow(it) { onPoemClick(it.id) } }
                }
                if (viewModel.editorsPicks.isNotEmpty()) item {
                    SectionCard("Выбор редакции", viewModel.editorsPicks) { PoemRow(it) { onPoemClick(it.id) } }
                }
                if (viewModel.topAuthors.isNotEmpty()) item {
                    SectionCard("Лучшие авторы", viewModel.topAuthors) { AuthorRow(it) { onAuthorClick(it.id) } }
                }
            }
        }
    }
}

@Composable
private fun Hero(onSearchClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("— ПОЭЗИЯ НА КАЖДЫЙ ДЕНЬ", fontSize = 14.sp, letterSpacing = 3.sp)
        Text(
            text = buildAnnotatedString {
                append("Находите стихи, которые ")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("говорят") }
                append(" с вами")
            },
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Light
        )
        SearchPill(onSearchClick)
    }
}
