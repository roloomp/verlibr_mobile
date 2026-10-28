package com.example.verlibr.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.verlibr.R
import com.example.verlibr.data.MAX_REVIEW_LENGTH
import com.example.verlibr.data.MAX_TOTAL_SCORE
import com.example.verlibr.data.RateMode
import com.example.verlibr.data.ratingCriteria
import com.example.verlibr.ui.components.AverageScoreRow
import com.example.verlibr.ui.components.CircleIconButton
import com.example.verlibr.ui.components.CollapsiblePanel
import com.example.verlibr.ui.components.RateModeToggle
import com.example.verlibr.ui.components.ReviewCard
import com.example.verlibr.ui.components.ReviewsHeader
import com.example.verlibr.ui.components.ScoreSlider
import com.example.verlibr.ui.components.ScoreSliderGroup
import com.example.verlibr.ui.components.SectionLabel
import com.example.verlibr.ui.components.TagRow
import com.example.verlibr.ui.theme.Moss
import com.example.verlibr.ui.theme.Stone
import com.example.verlibr.ui.theme.Taupe
import com.example.verlibr.ui.viewmodels.PoemViewModel

@Composable
fun PoemScreen(
    userId: String?,
    onRequireLogin: () -> Unit,
    viewModel: PoemViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(userId) { viewModel.load(userId) }
    viewModel.message?.let { text ->
        LaunchedEffect(text) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
    }

    val poem = viewModel.poem
    if (poem == null) {
        PoemStatus(viewModel.loading, viewModel.error) { viewModel.load(userId) }
        return
    }

    fun requireLogin(action: () -> Unit) {
        if (userId == null) onRequireLogin() else action()
    }

    val shareText = "«${poem.title}» — ${poem.author}"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(poem.title, fontSize = 32.sp, fontWeight = FontWeight.Light)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(poem.author, fontSize = 20.sp, fontStyle = FontStyle.Italic)
                    Text("·", fontSize = 20.sp, color = Stone)
                    Text("${poem.year?.toString().orEmpty()} г.", fontSize = 20.sp, color = Stone)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }

        item {
            Text(
                text = poem.text,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier.clickable { requireLogin(viewModel::toggleLike) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(if (viewModel.liked) R.drawable.ic_heart_filled else R.drawable.ic_heart),
                            contentDescription = "Нравится",
                            tint = if (viewModel.liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(viewModel.likes.toString(), fontSize = 11.sp)
                    }
                    Spacer(Modifier.size(20.dp))
                    Icon(
                        painter = painterResource(if (viewModel.favorite) R.drawable.ic_star_filled else R.drawable.ic_star),
                        contentDescription = "В избранное",
                        tint = if (viewModel.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { requireLogin(viewModel::toggleFavorite) }
                    )
                    Spacer(Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircleIconButton({ share(context, shareText) }) {
                            Icon(painterResource(R.drawable.ic_share), "Поделиться", Modifier.size(18.dp))
                        }
                        CircleIconButton({ copy(context, shareText) }) {
                            Icon(painterResource(R.drawable.ic_link), "Скопировать", Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item {
            CollapsiblePanel("Информация", viewModel.infoExpanded, viewModel::toggleInfo) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel("МЕТКИ")
                    poem.tags.forEach { TagRow(it) }
                    Spacer(Modifier.height(8.dp))
                    SectionLabel("РЕЙТИНГ")
                    AverageScoreRow(viewModel.averageWithReview, "Средняя оценка с рецензией", highlighted = true)
                    AverageScoreRow(viewModel.averageWithoutReview, "Средняя оценка без рецензии", highlighted = false)
                    Row {
                        Text("Всего оценок: ", fontSize = 14.sp)
                        Text(
                            viewModel.ratingsCount.toString(),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            CollapsiblePanel("Оценить работу", viewModel.rateExpanded, viewModel::toggleRate) {
                RateForm(viewModel) { requireLogin(viewModel::submit) }
            }
        }

        item {
            ReviewsHeader(viewModel.reviewsCount, viewModel.sort, viewModel::changeSort)
        }

        items(viewModel.sortedReviews, key = { it.id }) { review ->
            ReviewCard(review, onLike = { requireLogin { viewModel.toggleReviewLike(review.id) } })
        }
    }
}

@Composable
private fun RateForm(viewModel: PoemViewModel, onSubmit: () -> Unit) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Taupe,
        unfocusedBorderColor = Taupe,
        cursorColor = Taupe
    )
    val fieldShape = RoundedCornerShape(8.dp)
    val fieldText = TextStyle(fontSize = 14.sp)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RateModeToggle(viewModel.rateMode, viewModel::changeRateMode)

        ScoreSliderGroup {
            ratingCriteria.dropLast(1).forEachIndexed { i, label ->
                ScoreSlider(label, viewModel.scores[i], Moss) { viewModel.changeScore(i, it) }
            }
        }
        ScoreSliderGroup {
            val last = ratingCriteria.lastIndex
            ScoreSlider(ratingCriteria[last], viewModel.scores[last], MaterialTheme.colorScheme.primary) {
                viewModel.changeScore(last, it)
            }
        }

        if (viewModel.rateMode == RateMode.Review) {
            OutlinedTextField(
                value = viewModel.reviewTitle,
                onValueChange = viewModel::changeReviewTitle,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Заголовок рецензии", fontSize = 14.sp, color = Stone) },
                textStyle = fieldText,
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors
            )
            OutlinedTextField(
                value = viewModel.reviewText,
                onValueChange = viewModel::changeReviewText,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = { Text("Текст рецензии (от 300 до 8500 символов)", fontSize = 14.sp, color = Stone) },
                textStyle = fieldText,
                shape = fieldShape,
                colors = fieldColors
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, Taupe, RoundedCornerShape(6.dp))
                        .clickable(onClick = viewModel::clearDraft)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_broom), null, Modifier.size(16.dp))
                    Text("Очистить черновик", fontSize = 12.sp)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${viewModel.reviewText.length.toString().padStart(4, '0')}/$MAX_REVIEW_LENGTH",
                    fontSize = 12.sp,
                    color = Stone
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Text(viewModel.draftTotal.toString(), fontSize = 48.sp, fontWeight = FontWeight.Light)
            Text(
                text = "/$MAX_TOTAL_SCORE",
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 2.dp, end = 16.dp, top = 16.dp)
            )
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .alpha(if (viewModel.canSubmit) 1f else 0.4f)
                    .background(Moss, CircleShape)
                    .clip(CircleShape)
                    .clickable(enabled = viewModel.canSubmit && !viewModel.submitting, onClick = onSubmit),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "Отправить",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun PoemStatus(loading: Boolean, error: Boolean, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            error -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Не удалось загрузить стих", fontSize = 16.sp)
                OutlinedButton(onClick = onRetry) { Text("Повторить") }
            }
            loading -> CircularProgressIndicator(color = Taupe)
        }
    }
}

private fun share(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, null))
}

private fun copy(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("poem", text))
    Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
}
