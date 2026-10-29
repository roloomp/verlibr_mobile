package com.example.verlibr.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.verlibr.data.network.AuthorInfo
import com.example.verlibr.ui.theme.Cream
import com.example.verlibr.ui.theme.Stone
import com.example.verlibr.ui.theme.Taupe

/** Шапка экрана автора: аватар, имя, годы жизни, биография и заголовок списка стихов. */
@Composable
fun AuthorHeader(
    author: AuthorInfo?,
    loading: Boolean,
    error: Boolean,
    onRetry: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        when {
            author != null -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // TODO: аватар из authors.avatar, когда появится загрузка изображений
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Taupe, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(author.name.take(1).uppercase(), fontSize = 32.sp, color = Cream)
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(author.name, fontSize = 24.sp, fontWeight = FontWeight.Light)
                        if (author.years.isNotEmpty()) {
                            Text(author.years, fontSize = 14.sp, color = Stone)
                        }
                    }
                }
                author.bio?.let { Text(it, fontSize = 14.sp, lineHeight = 20.sp) }
            }
            loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            error -> Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Не удалось загрузить автора", fontSize = 16.sp, color = Taupe)
                SheetButton(
                    text = "Повторить",
                    filled = true,
                    modifier = Modifier.fillMaxWidth(0.6f),
                    onClick = onRetry
                )
            }
            else -> Text("Автор не найден", fontSize = 16.sp, color = Taupe)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Text(
            text = "Стихи",
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Light
        )
    }
}
