package com.example.verlibr.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.verlibr.data.MAX_TOTAL_SCORE
import com.example.verlibr.ui.components.CheckRow
import com.example.verlibr.ui.components.FilterSection
import com.example.verlibr.ui.components.Pagination
import com.example.verlibr.ui.components.PoemCard
import com.example.verlibr.ui.components.SearchField
import com.example.verlibr.ui.components.SheetButton
import com.example.verlibr.ui.components.SortFilterBar
import com.example.verlibr.ui.theme.Fog
import com.example.verlibr.ui.theme.Taupe
import com.example.verlibr.ui.viewmodels.SearchViewModel
import kotlin.math.roundToInt

@Composable
fun SearchScreen(
    onPoemClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    var filtersOpen by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.page) { listState.scrollToItem(0) }

    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SearchField(viewModel.query, viewModel::onQueryChange)
            SortFilterBar(
                sort = viewModel.sort,
                onSortChange = viewModel::onSortChange,
                activeFilters = viewModel.activeFilters,
                onFiltersClick = { filtersOpen = true }
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (viewModel.results.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        Text("Ничего не найдено", fontSize = 16.sp)
                    }
                }
            }
            items(viewModel.pagePoems, key = { it.preview.id }) { poem ->
                PoemCard(poem.preview) { onPoemClick(poem.preview.id) }
            }
            if (viewModel.pageCount > 1) {
                item {
                    Pagination(viewModel.page, viewModel.pageCount, viewModel::openPage)
                }
            }
        }
    }

    if (filtersOpen) FiltersSheet(viewModel) { filtersOpen = false }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersSheet(viewModel: SearchViewModel, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text(
                text = "Фильтры",
                fontSize = 22.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                FilterSection("Предпочтения", initiallyExpanded = true) {
                    CheckRow("Не показывать избранное", viewModel.hideFavorite, viewModel::onHideFavoriteChange)
                    CheckRow("Не показывать с оценкой", viewModel.hideRated, viewModel::onHideRatedChange)
                }

                FilterSection("Рейтинг") {
                    val range = viewModel.ratingRange
                    Text(
                        text = "от ${range.start.roundToInt()} до ${range.endInclusive.roundToInt()}",
                        fontSize = 14.sp
                    )
                    RangeSlider(
                        value = range,
                        onValueChange = viewModel::setRating,
                        valueRange = viewModel.ratingBounds,
                        steps = MAX_TOTAL_SCORE - 1,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Fog,
                            activeTickColor = Taupe.copy(alpha = 0f),
                            inactiveTickColor = Taupe.copy(alpha = 0f)
                        )
                    )
                }

                viewModel.options.forEach { (key, values) ->
                    FilterSection(key, selectedCount = viewModel.selected[key].orEmpty().size) {
                        values.forEach { value ->
                            CheckRow(value, value in viewModel.selected[key].orEmpty()) {
                                viewModel.toggleOption(key, value)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SheetButton("Сбросить", filled = false, modifier = Modifier.weight(1f), onClick = viewModel::resetFilters)
                SheetButton("Показать (${viewModel.results.size})", filled = true, modifier = Modifier.weight(1f), onClick = onDismiss)
            }
        }
    }
}
