package com.example.verlibr.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.verlibr.data.MAX_REVIEW_LENGTH
import com.example.verlibr.data.MIN_REVIEW_LENGTH
import com.example.verlibr.data.Poem
import com.example.verlibr.data.RateMode
import com.example.verlibr.data.Review
import com.example.verlibr.data.ReviewSort
import com.example.verlibr.data.network.PoemData
import com.example.verlibr.data.network.PoemDetailsService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PoemViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val id: Int? = savedStateHandle.get<String>("id")?.toIntOrNull()
    private var userId: String? = null
    private var loadJob: Job? = null
    private var likeJob: Job? = null
    private var favoriteJob: Job? = null
    private val busyReviewLikes = mutableSetOf<String>()

    var poem by mutableStateOf<Poem?>(null)
        private set
    var loading by mutableStateOf(true)
        private set
    var error by mutableStateOf(false)
        private set
    var submitting by mutableStateOf(false)
        private set

    /** Одноразовое сообщение для пользователя (Toast). */
    var message by mutableStateOf<String?>(null)
        private set

    fun consumeMessage() { message = null }

    /** Загружает стих; userId = null для гостя. Вызывается при открытии и при смене пользователя. */
    fun load(user: String?) {
        userId = user
        val poemId = id
        if (poemId == null) {
            error = true
            loading = false
            return
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loading = poem == null
            error = false
            try {
                apply(PoemDetailsService.load(poemId, user))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                if (poem == null) error = true else message = "Не удалось обновить данные"
            } finally {
                loading = false
            }
        }
    }

    private fun apply(data: PoemData) {
        poem = data.poem
        likes = data.likes
        liked = data.liked
        favorite = data.favorite
        reviews = data.reviews
        allTotals = data.allTotals
        data.mine?.let { mine ->
            mine.scores.forEachIndexed { i, v -> scores[i] = v }
            rateMode = if (mine.hasReview) RateMode.Review else RateMode.Plain
            reviewTitle = mine.title
            reviewText = mine.text
        }
    }

    // ---- лайк / избранное ----
    var likes by mutableIntStateOf(0)
        private set
    var liked by mutableStateOf(false)
        private set
    var favorite by mutableStateOf(false)
        private set

    fun toggleLike() {
        val user = userId ?: return
        val poemId = id ?: return
        if (likeJob?.isActive == true) return
        val target = !liked
        liked = target
        likes += if (target) 1 else -1
        likeJob = viewModelScope.launch {
            try {
                PoemDetailsService.setLike(poemId, user, target)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                liked = !target
                likes += if (target) -1 else 1
                message = "Не удалось поставить лайк"
            }
        }
    }

    fun toggleFavorite() {
        val user = userId ?: return
        val poemId = id ?: return
        if (favoriteJob?.isActive == true) return
        val target = !favorite
        favorite = target
        favoriteJob = viewModelScope.launch {
            try {
                PoemDetailsService.setFavorite(poemId, user, target)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                favorite = !target
                message = "Не удалось изменить избранное"
            }
        }
    }

    var infoExpanded by mutableStateOf(false)
        private set
    var rateExpanded by mutableStateOf(false)
        private set

    fun toggleInfo() { infoExpanded = !infoExpanded }
    fun toggleRate() { rateExpanded = !rateExpanded }

    // ---- рецензии ----
    private var reviews by mutableStateOf<List<Review>>(emptyList())
    private var allTotals by mutableStateOf<List<Int>>(emptyList())

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
        val user = userId ?: return
        val review = reviews.firstOrNull { it.id == reviewId } ?: return
        val dbId = reviewId.toIntOrNull() ?: return
        if (!busyReviewLikes.add(reviewId)) return
        val target = !review.liked
        updateReview(reviewId, target)
        viewModelScope.launch {
            try {
                PoemDetailsService.setReviewLike(dbId, user, target)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                updateReview(reviewId, !target)
                message = "Не удалось поставить лайк"
            } finally {
                busyReviewLikes.remove(reviewId)
            }
        }
    }

    private fun updateReview(reviewId: String, liked: Boolean) {
        reviews = reviews.map {
            if (it.id != reviewId) it
            else it.copy(liked = liked, likes = (it.likes + if (liked) 1 else -1).coerceAtLeast(0))
        }
    }

    val ratingsCount: Int get() = allTotals.size

    val averageWithReview: Int? get() = reviews.map { it.total }.averageRounded()

    val averageWithoutReview: Int? get() = allTotals.averageRounded()

    private fun List<Int>.averageRounded(): Int? = if (isEmpty()) null else average().roundToInt()

    // ---- форма оценки ----
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

    /** Одна оценка на стих: повторная отправка заменяет прежнюю. */
    fun submit() {
        val user = userId ?: return
        val poemId = id ?: return
        if (!canSubmit || submitting) return
        val review = if (rateMode == RateMode.Review) reviewTitle.trim() to reviewText.trim() else null
        val values = scores.toList()
        viewModelScope.launch {
            submitting = true
            try {
                PoemDetailsService.submitRating(poemId, user, values, review)
                apply(PoemDetailsService.load(poemId, user))
                rateExpanded = false
                message = "Оценка сохранена"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                message = "Не удалось отправить оценку"
            } finally {
                submitting = false
            }
        }
    }
}
