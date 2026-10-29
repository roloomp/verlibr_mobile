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
import com.example.verlibr.data.network.PoemDetailService
import com.example.verlibr.data.network.PoemRatings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class PoemLoadState { Loading, Error, Content }

class PoemViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val poemId: Int? = savedStateHandle.get<String>("id")?.toIntOrNull()

    // ---- загрузка ----
    var poem by mutableStateOf<Poem?>(null)
        private set
    var loadState by mutableStateOf(PoemLoadState.Loading)
        private set
    var errorText by mutableStateOf("")
        private set

    private var userId: String? = null
    private var userKnown = false
    private var loadJob: Job? = null

    /** Вызывается экраном при открытии и при каждой смене пользователя (вход / выход). */
    fun onUserChanged(id: String?) {
        if (userKnown && id == userId) return
        userKnown = true
        userId = id
        load()
    }

    fun retry() = load()

    private fun load() {
        val id = poemId
        if (id == null) {
            errorText = "Некорректная ссылка на стихотворение"
            loadState = PoemLoadState.Error
            return
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (poem == null) loadState = PoemLoadState.Loading
            val uid = userId
            try {
                coroutineScope {
                    val poemD = async { PoemDetailService.getPoem(id) }
                    val likesD = async { PoemDetailService.likesCount(id) }
                    val ratingsD = async { PoemDetailService.getRatings(id, uid) }
                    val likedD = async { uid?.let { PoemDetailService.isLiked(id, it) } ?: false }
                    val favoriteD = async { uid?.let { PoemDetailService.isFavorite(id, it) } ?: false }

                    val loaded = poemD.await() ?: throw NoSuchElementException()
                    val ratings = ratingsD.await()
                    poem = loaded
                    likes = likesD.await()
                    liked = likedD.await()
                    favorite = favoriteD.await()
                    applyRatings(ratings)
                }
                loadState = PoemLoadState.Content
            } catch (e: CancellationException) {
                throw e
            } catch (e: NoSuchElementException) {
                errorText = "Стихотворение не найдено"
                if (poem == null) loadState = PoemLoadState.Error
            } catch (e: Exception) {
                e.printStackTrace()
                errorText = "Не удалось загрузить стихотворение. Проверьте соединение"
                if (poem == null) loadState = PoemLoadState.Error else message = errorText
            }
        }
    }

    // ---- одноразовые события для экрана ----
    var message by mutableStateOf<String?>(null)
        private set
    var needLogin by mutableStateOf(false)
        private set

    fun messageShown() { message = null }
    fun loginRequestHandled() { needLogin = false }

    // ---- лайк / избранное ----
    var likes by mutableIntStateOf(0)
        private set
    var liked by mutableStateOf(false)
        private set
    var favorite by mutableStateOf(false)
        private set

    private var likeBusy = false
    private var favoriteBusy = false

    fun toggleLike() {
        val uid = userId ?: run { needLogin = true; return }
        val id = poemId ?: return
        if (likeBusy) return
        likeBusy = true
        val newValue = !liked
        liked = newValue
        likes += if (newValue) 1 else -1
        viewModelScope.launch {
            try {
                PoemDetailService.setLiked(id, uid, newValue)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                liked = !newValue
                likes += if (newValue) -1 else 1
                message = "Не удалось сохранить лайк"
            } finally {
                likeBusy = false
            }
        }
    }

    fun toggleFavorite() {
        val uid = userId ?: run { needLogin = true; return }
        val id = poemId ?: return
        if (favoriteBusy) return
        favoriteBusy = true
        val newValue = !favorite
        favorite = newValue
        viewModelScope.launch {
            try {
                PoemDetailService.setFavorite(id, uid, newValue)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                favorite = !newValue
                message = "Не удалось обновить избранное"
            } finally {
                favoriteBusy = false
            }
        }
    }

    var infoExpanded by mutableStateOf(false)
        private set
    var rateExpanded by mutableStateOf(false)
        private set

    fun toggleInfo() { infoExpanded = !infoExpanded }
    fun toggleRate() { rateExpanded = !rateExpanded }

    // ---- рецензии и оценки ----
    private var reviews by mutableStateOf<List<Review>>(emptyList())
    private var reviewTotals by mutableStateOf<List<Int>>(emptyList())
    private var allTotals by mutableStateOf<List<Int>>(emptyList())
    private var ratedByMe by mutableStateOf(false)

    private fun applyRatings(ratings: PoemRatings) {
        reviews = ratings.reviews
        reviewTotals = ratings.reviewTotals
        allTotals = ratings.allTotals
        ratedByMe = ratings.ratedByMe
    }

    var sort by mutableStateOf(ReviewSort.New)
        private set

    fun changeSort(value: ReviewSort) { sort = value }

    val sortedReviews: List<Review>
        get() = when (sort) {
            ReviewSort.New -> reviews.sortedByDescending { it.date }
            ReviewSort.Popular -> reviews.sortedByDescending { it.likes }
        }

    val reviewsCount: Int get() = reviews.size

    private val busyReviews = mutableSetOf<String>()

    fun toggleReviewLike(reviewId: String) {
        val uid = userId ?: run { needLogin = true; return }
        val id = reviewId.toIntOrNull() ?: return
        val current = reviews.firstOrNull { it.id == reviewId } ?: return
        if (!busyReviews.add(reviewId)) return
        val newValue = !current.liked
        updateReview(reviewId) { it.copy(liked = newValue, likes = it.likes + if (newValue) 1 else -1) }
        viewModelScope.launch {
            try {
                PoemDetailService.setReviewLiked(id, uid, newValue)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                updateReview(reviewId) { it.copy(liked = !newValue, likes = it.likes + if (newValue) -1 else 1) }
                message = "Не удалось сохранить лайк"
            } finally {
                busyReviews.remove(reviewId)
            }
        }
    }

    private fun updateReview(reviewId: String, transform: (Review) -> Review) {
        reviews = reviews.map { if (it.id == reviewId) transform(it) else it }
    }

    val ratingsCount: Int get() = allTotals.size

    val averageWithReview: Int? get() = reviewTotals.averageRounded()

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
    var submitting by mutableStateOf(false)
        private set

    val draftTotal: Int get() = scores.sum()

    val canSubmit: Boolean
        get() = !submitting && (rateMode == RateMode.Plain ||
            (reviewTitle.isNotBlank() && reviewText.trim().length >= MIN_REVIEW_LENGTH))

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
        val uid = userId ?: run { needLogin = true; return }
        val id = poemId ?: return
        if (ratedByMe) {
            message = "Вы уже оценили это стихотворение"
            return
        }
        val isReview = rateMode == RateMode.Review
        val title = if (isReview) reviewTitle.trim() else null
        val text = if (isReview) reviewText.trim() else null
        submitting = true
        viewModelScope.launch {
            try {
                val result = PoemDetailService.submitRating(id, uid, scores.toList(), title, text)
                if (result.isSuccess) {
                    clearDraft()
                    scores.indices.forEach { scores[it] = 9 }
                    rateExpanded = false
                    ratedByMe = true
                    message = "Оценка сохранена"
                    try {
                        applyRatings(PoemDetailService.getRatings(id, uid))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    message = result.exceptionOrNull()?.message ?: "Не удалось сохранить оценку"
                }
            } finally {
                submitting = false
            }
        }
    }
}
