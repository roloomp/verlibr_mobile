package com.example.verlibr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.verlibr.data.PoemPreview

@Composable
fun PoemCard(poem: PoemPreview, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(poem.title, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(poem.author, fontSize = 12.sp)
                Text(poem.year?.toString().orEmpty(), fontSize = 12.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CountBadge(poem.likes, filled = true)
            CountBadge(poem.comments, filled = false)
        }
    }
}

@Composable
private fun CountBadge(count: Int, filled: Boolean) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(28.dp)
            .then(
                if (filled) Modifier.background(accent, CircleShape)
                else Modifier.border(1.5.dp, accent, CircleShape)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            fontSize = 12.sp,
            color = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun Pagination(page: Int, pageCount: Int, onPageClick: (Int) -> Unit) {
    val window = if (page <= 3) 1..3 else (page - 1)..(page + 1)
    val pages = (listOf(1) + window + pageCount).filter { it in 1..pageCount }.distinct().sorted()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (page > 1) PageButton("<", selected = false, modifier = Modifier) { onPageClick(page - 1) }

        pages.forEachIndexed { index, p ->
            if (index > 0 && p - pages[index - 1] > 1) {
                PageButton("…", selected = false, enabled = false, modifier = Modifier) {}
            }
            PageButton(p.toString(), selected = p == page, modifier = Modifier) { onPageClick(p) }
        }

        if (page < pageCount) PageButton(">", selected = false, modifier = Modifier) { onPageClick(page + 1) }
    }
}


@Composable
private fun PageButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(shape)
            .background(if (selected) accent else Color.Transparent)
            .border(1.5.dp, accent, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            maxLines = 1,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
        )
    }
}
