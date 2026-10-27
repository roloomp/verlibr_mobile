package com.example.verlibr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.verlibr.data.PoemRating
import com.example.verlibr.data.Profile
import com.example.verlibr.ui.theme.Cream
import com.example.verlibr.ui.theme.Stone
import com.example.verlibr.ui.theme.Taupe

// TODO: баннер из Profile.banner, когда появится загрузка изображений
@Composable
fun ProfileBanner() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Taupe)
    )
}

@Composable
fun ProfileInfoCard(profile: Profile, registeredAt: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Taupe, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TODO: аватар из Profile.avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Taupe, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(profile.name.take(1).uppercase(), fontSize = 32.sp, color = Cream)
        }
        Spacer(Modifier.width(20.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(profile.name, fontSize = 20.sp)
            Text("Дата регистрации: $registeredAt", fontSize = 12.sp)
            profile.bio?.let { Text(it, fontSize = 12.sp) }
        }
    }
}

@Composable
fun ProfileChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Text(
        text = text,
        fontSize = 12.sp,
        maxLines = 1,
        color = if (selected) Cream else MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) Taupe else Color.Transparent)
            .border(1.5.dp, Taupe, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp)
    )
}

@Composable
fun EmptyHint(text: String = "Пока что тут пусто") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 20.sp, fontStyle = FontStyle.Italic, color = Taupe)
    }
}

/** Оценка без рецензии: стих слева, итог и баллы по критериям справа. */
@Composable
fun RatedPoemCard(rating: PoemRating, onClick: () -> Unit) {
    val poem = rating.poem
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
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
        Column(horizontalAlignment = Alignment.End) {
            Text(rating.total.toString(), fontSize = 24.sp, fontWeight = FontWeight.Light)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rating.scores.forEachIndexed { i, s ->
                    Text(
                        text = s.toString(),
                        fontSize = 11.sp,
                        color = if (i == rating.scores.lastIndex) MaterialTheme.colorScheme.primary else Stone
                    )
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(value, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
    }
}
