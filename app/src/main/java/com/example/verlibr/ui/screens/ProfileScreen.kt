package com.example.verlibr.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.verlibr.ui.components.EmptyHint
import com.example.verlibr.ui.components.Pagination
import com.example.verlibr.ui.components.PoemCard
import com.example.verlibr.ui.components.ProfileBanner
import com.example.verlibr.ui.components.ProfileChip
import com.example.verlibr.ui.components.ProfileInfoCard
import com.example.verlibr.ui.components.RatedPoemCard
import com.example.verlibr.ui.components.ReviewCard
import com.example.verlibr.ui.components.SectionCard
import com.example.verlibr.ui.components.StatRow
import com.example.verlibr.ui.theme.Taupe
import com.example.verlibr.ui.viewmodels.ProfileSection
import com.example.verlibr.ui.viewmodels.ProfileTab
import com.example.verlibr.ui.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    onPoemClick: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.page) { listState.scrollToItem(0) }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ProfileBanner() }
        item { ProfileInfoCard(viewModel.profile, viewModel.registeredAt) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileTab.entries.forEach { entry ->
                    ProfileChip(entry.label, viewModel.tab == entry) { viewModel.openTab(entry) }
                }
            }
        }
        if (viewModel.tab.sections.isNotEmpty()) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.tab.sections.forEach { entry ->
                        ProfileChip(entry.label, viewModel.section == entry) { viewModel.openSection(entry) }
                    }
                }
            }
        }
        item { HorizontalDivider(color = Taupe, thickness = 1.5.dp) }

        when {
            viewModel.isStats -> item {
                SectionCard("Статистика", viewModel.statRows) { StatRow(it.first, it.second) }
            }
            viewModel.isEmpty -> item { EmptyHint() }
            else -> when (viewModel.section) {
                ProfileSection.MyReviews, ProfileSection.LikedReviews ->
                    items(viewModel.reviewsPage, key = { it.id }) { review -> ReviewCard(review) }
                ProfileSection.MyRatings ->
                    items(viewModel.ratingsPage, key = { it.poem.id }) { rating ->
                        RatedPoemCard(rating) { onPoemClick(rating.poem.id) }
                    }
                ProfileSection.LikedPoems ->
                    items(viewModel.poemsPage, key = { it.id }) { poem ->
                        PoemCard(poem) { onPoemClick(poem.id) }
                    }
            }
        }

        if (!viewModel.isStats) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Pagination(viewModel.page, viewModel.pageCount, viewModel::openPage)
                }
            }
        }
    }
}
