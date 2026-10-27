package com.example.verlibr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.verlibr.R
import com.example.verlibr.data.MAX_CRITERION_SCORE
import com.example.verlibr.data.MAX_TOTAL_SCORE
import com.example.verlibr.data.PoemTag
import com.example.verlibr.data.RateMode
import com.example.verlibr.data.Review
import com.example.verlibr.data.ReviewSort
import com.example.verlibr.ui.theme.Cream
import com.example.verlibr.ui.theme.Fog
import com.example.verlibr.ui.theme.Slate
import com.example.verlibr.ui.theme.Stone
import com.example.verlibr.ui.theme.Taupe
import java.time.format.DateTimeFormatter

private val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@Composable
fun CollapsiblePanel(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(2.dp, Taupe, shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 22.sp, modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = if (expanded) "Свернуть" else "Развернуть",
                modifier = Modifier
                    .size(28.dp)
                    .rotate(if (expanded) 180f else 0f)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                content = content
            )
        }
    }
}

@Composable
fun CircleIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}


@Composable
fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TagRow(tag: PoemTag) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${tag.label}:", fontSize = 14.sp)
        Text(
            text = tag.value,
            fontSize = 12.sp,
            modifier = Modifier
                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        )
    }
}

@Composable
fun AverageScoreRow(value: Int?, label: String, highlighted: Boolean) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = value?.toString() ?: "—",
            fontSize = 40.sp,
            fontWeight = FontWeight.Light,
            color = if (highlighted) MaterialTheme.colorScheme.primary else Stone
        )
        Text(
            text = "/$MAX_TOTAL_SCORE",
            fontSize = 14.sp,
            color = if (highlighted) MaterialTheme.colorScheme.onBackground else Stone,
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp, bottom = 10.dp)
        )
    }
}


@Composable
fun RateModeToggle(mode: RateMode, onChange: (RateMode) -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.5.dp, Slate, shape)
    ) {
        listOf(RateMode.Review to "Рецензия", RateMode.Plain to "Оценка без рецензии").forEach { (value, label) ->
            val selected = mode == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (selected) Slate else Cream)
                    .clickable { onChange(value) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, fontSize = 14.sp, color = if (selected) Cream else MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun ScoreSliderGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Taupe, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        content = content
    )
}

@Composable
fun ScoreSlider(label: String, value: Int, trackColor: Color, onChange: (Int) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text(value.toString(), fontSize = 16.sp)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(Math.round(it)) },
            valueRange = 0f..MAX_CRITERION_SCORE.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Slate,
                activeTrackColor = trackColor,
                inactiveTrackColor = Fog
            )
        )
    }
}

@Composable
fun ReviewsHeader(count: Int, sort: ReviewSort, onSortChange: (ReviewSort) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Рецензии пользователей", fontSize = 22.sp, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(count.toString(), fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        SortDropdown(sort, onSortChange)
    }
}

@Composable
private fun SortDropdown(sort: ReviewSort, onSelect: (ReviewSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .border(1.5.dp, Taupe, RoundedCornerShape(6.dp))
                .clickable { open = true }
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(sort.label, fontSize = 12.sp)
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                modifier = Modifier
                    .size(12.dp)
                    .rotate(180f)
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            ReviewSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label, fontSize = 14.sp) },
                    onClick = {
                        onSelect(option)
                        open = false
                    }
                )
            }
        }
    }
}

@Composable
fun ReviewCard(review: Review, onLike: (() -> Unit)? = null) {
    var expanded by rememberSaveable(review.id) { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(2.dp, Taupe, shape)
            .clickable { expanded = !expanded }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Avatar(review.author)
            Spacer(Modifier.width(12.dp))
            Text(
                text = review.author,
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(review.total.toString(), fontSize = 24.sp, fontWeight = FontWeight.Light)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    review.scores.forEachIndexed { i, s ->
                        Text(
                            text = s.toString(),
                            fontSize = 11.sp,
                            color = if (i == review.scores.lastIndex) MaterialTheme.colorScheme.primary else Stone
                        )
                    }
                }
            }
        }

        Text(review.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp)

        Text(
            text = review.text,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis
        )

        if (expanded) {
            Text(review.date.format(dateFormat), fontSize = 11.sp, color = Stone)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (expanded) LikePill(review.likes, review.liked, onLike)
            Text(
                text = if (expanded) "Меньше…" else "Ещё…",
                fontSize = 14.sp,
                color = Stone,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Avatar(name: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(Taupe, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(name.take(1).uppercase(), fontSize = 18.sp, color = Cream)
    }
}

@Composable
fun LikePill(count: Int, liked: Boolean, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(if (liked) R.drawable.ic_heart_filled else R.drawable.ic_heart),
            contentDescription = "Нравится",
            tint = if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(20.dp)
        )
        Text(count.toString(), fontSize = 14.sp)
    }
}
