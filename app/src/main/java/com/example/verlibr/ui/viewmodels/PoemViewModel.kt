package com.example.verlibr.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.verlibr.data.MAX_REVIEW_LENGTH
import com.example.verlibr.data.MIN_REVIEW_LENGTH
import com.example.verlibr.data.Poem
import com.example.verlibr.data.PoemTag
import com.example.verlibr.data.RateMode
import com.example.verlibr.data.Review
import com.example.verlibr.data.ReviewSort
import java.time.LocalDate
import java.util.UUID
import kotlin.math.roundToInt

class PoemViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val id: String = savedStateHandle["id"] ?: ""

    val poem = Poem(
        id = id,
        title = "Канал Грибоедова",
        author = "Владислав Савенко",
        year = 2026,
        text = """
            Над каналом заледенелым,
            Где в соборе подсветки след,
            Покурить – не благое дело,
            Дописать, не допеть куплет.

            Жизнь оттает и серой льдинкой
            По водице уйдёт в залив,
            Непредсказанным поединком,
            Все печали к единой слив,

            Где крылатые львы застыли,
            Золотистый рисуя взмах,
            Где не встречу тебя, не ты ли
            В мой вчерашний вселилась страх?

            Что из жизни, как крошки хлеба,
            Сгинет прошлое, не всерьёз.
            И какого нам ждать ночлега?
            Остаётся немым вопрос.
        """.trimIndent(),
        tags = listOf(
            PoemTag("Тип", "авторский стих"),
            PoemTag("Жанр", "философская лирика"),
            PoemTag("Настроение", "меланхоличное"),
            PoemTag("Стихосложение", "силлабо-тоническое"),
            PoemTag("Базовая стопа", "трёхсложник"),
            PoemTag("Строфа", "катрены"),
            PoemTag("Способ рифмовки", "ABAB")
        )
    )

    // ---- лайк / избранное ----
    var likes by mutableIntStateOf(7)
        private set
    var liked by mutableStateOf(false)
        private set
    var favorite by mutableStateOf(false)
        private set

    fun toggleLike() {
        liked = !liked
        likes += if (liked) 1 else -1
    }
    fun toggleFavorite() {
        favorite = !favorite
    }
    var infoExpanded by mutableStateOf(false)
        private set
    var rateExpanded by mutableStateOf(false)
        private set

    fun toggleInfo() { infoExpanded = !infoExpanded }
    fun toggleRate() { rateExpanded = !rateExpanded }

    private val reviews = mutableStateListOf(
        Review(
            id = "r0",
            author = "Владислав Савенко",
            title = "Над заледенелым каналом: разговор с городом и собой",
            text = "Короткое, но ёмкое стихотворение, в котором пейзаж становится исповедью. " +
                "Автору удивительно точно удалось передать то щемящее чувство, когда прошлое давит " +
                "на плечи, а настоящее утекает сквозь пальцы, как талая вода в залив.\n\n" +
                "Особую прелесть придают детали: «подсветки след» в соборе, застывшие крылатые львы, " +
                "вопрос, обращённый то ли к городу, то ли к ушедшему человеку. Стихотворение держит " +
                "читателя в состоянии лёгкой грусти до самой последней строки — и отпускает уже другим.",
            scores = listOf(9, 9, 9, 9, 9),
            date = LocalDate.of(2026, 2, 28),
            likes = 7
        )
    )
    private val plainRatings = mutableStateListOf<Int>()

    var sort by mutableStateOf(ReviewSort.New)
        private set

    fun changeSort(value: ReviewSort) { sort = value }

    val sortedReviews: List<Review>
        get() = when (sort) {
            ReviewSort.New -> reviews.sortedByDescending { it.date }
            ReviewSort.Popular -> reviews.sortedByDescending { it.likes }
        }

    val reviewsCount: Int get() = reviews.size

    fun toggleReviewLike(reviewId: String) {
        val index = reviews.indexOfFirst { it.id == reviewId }
        if (index < 0) return
        val r = reviews[index]
        reviews[index] = r.copy(liked = !r.liked, likes = r.likes + if (r.liked) -1 else 1)
    }

    val ratingsCount: Int get() = reviews.size + plainRatings.size

    val averageWithReview: Int? get() = reviews.map { it.total }.averageRounded()

    val averageWithoutReview: Int? get() = (reviews.map { it.total } + plainRatings).averageRounded()

    private fun List<Int>.averageRounded(): Int? = if (isEmpty()) null else average().roundToInt()

    var rateMode by mutableStateOf(RateMode.Review)
        private set
    val scores = mutableStateListOf(9, 9, 9, 9, 9)
    var reviewTitle by mutableStateOf("")
        private set
    var reviewText by mutableStateOf("")
        private set

    val draftTotal: Int get() = scores.sum()

    val canSubmit: Boolean
        get() = rateMode == RateMode.Plain ||
            (reviewTitle.isNotBlank() && reviewText.trim().length >= MIN_REVIEW_LENGTH)

    fun changeRateMode(value: RateMode) { rateMode = value }
    fun changeScore(index: Int, value: Int) { scores[index] = value }
    fun changeReviewTitle(value: String) { reviewTitle = value }
    fun changeReviewText(value: String) { reviewText = value.take(MAX_REVIEW_LENGTH) }

    fun clearDraft() {
        reviewTitle = ""
        reviewText = ""
    }

    fun submit() {
        if (!canSubmit) return
        if (rateMode == RateMode.Review) {
            reviews.add(
                Review(
                    id = UUID.randomUUID().toString(),
                    author = "Вы", // TODO: имя текущего пользователя после реализации авторизации
                    title = reviewTitle.trim(),
                    text = reviewText.trim(),
                    scores = scores.toList(),
                    date = LocalDate.now()
                )
            )
        } else {
            plainRatings.add(draftTotal)
        }
        clearDraft()
        scores.indices.forEach { scores[it] = 9 }
        rateExpanded = false
    }
}
