package com.example.verlibr.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.verlibr.data.PoemPreview
import com.example.verlibr.data.PoemRating
import com.example.verlibr.data.Profile
import com.example.verlibr.data.Review
import com.example.verlibr.data.network.ProfileService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class ProfileSection(val label: String) {
    MyReviews("Рецензии"),
    MyRatings("Оценки"),
    LikedReviews("Рецензии"),
    LikedPoems("Стихи")
}

enum class ProfileTab(val label: String, val sections: List<ProfileSection>) {
    Reviews("Рецензии и оценка", listOf(ProfileSection.MyReviews, ProfileSection.MyRatings)),
    Stats("Статистика", emptyList()),
    Liked("Понравилось", listOf(ProfileSection.LikedReviews, ProfileSection.LikedPoems))
}

class ProfileViewModel : ViewModel() {

    private var currentProfile: Profile? = null
    private var createdDate: LocalDate? = null
    private var job: Job? = null

    // ---- загрузка ----
    /** Данные хотя бы раз успешно загружены для текущего пользователя. */
    var loaded by mutableStateOf(false)
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf(false)
        private set

    val showSpinner: Boolean get() = !loaded && !error
    val showError: Boolean get() = !loaded && error

    private var myReviews by mutableStateOf<List<Review>>(emptyList())
    private var myRatings by mutableStateOf<List<PoemRating>>(emptyList())
    private var likedReviews by mutableStateOf<List<Review>>(emptyList())
    private var likedPoems by mutableStateOf<List<PoemPreview>>(emptyList())
    private var favoritesCount by mutableIntStateOf(0)

    /** Вызывается при открытии экрана; уже загруженные данные остаются на экране, пока идёт обновление. */
    fun load(profile: Profile) {
        if (currentProfile?.id != profile.id) {
            myReviews = emptyList()
            myRatings = emptyList()
            likedReviews = emptyList()
            likedPoems = emptyList()
            favoritesCount = 0
            loaded = false
            page = 1
        }
        currentProfile = profile
        createdDate = runCatching { LocalDate.parse(profile.createdAt!!.take(10)) }.getOrNull()

        job?.cancel()
        job = viewModelScope.launch {
            loading = true
            error = false
            try {
                val data = ProfileService.load(profile.id, profile.name)
                myReviews = data.myReviews
                myRatings = data.myRatings
                likedReviews = data.likedReviews
                likedPoems = data.likedPoems
                favoritesCount = data.favoritesCount
                loaded = true
                page = page.coerceIn(1, pageCount)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                error = true
            } finally {
                loading = false
            }
        }
    }

    fun retry() {
        currentProfile?.let { load(it) }
    }

    // ---- вкладки и страницы ----
    var tab by mutableStateOf(ProfileTab.Reviews)
        private set
    var section by mutableStateOf(ProfileSection.MyReviews)
        private set
    var page by mutableIntStateOf(1)
        private set

    private val pageSize = 5

    private val total: Int
        get() = when (section) {
            ProfileSection.MyReviews -> myReviews.size
            ProfileSection.MyRatings -> myRatings.size
            ProfileSection.LikedReviews -> likedReviews.size
            ProfileSection.LikedPoems -> likedPoems.size
        }

    val isStats: Boolean get() = tab == ProfileTab.Stats

    val isEmpty: Boolean get() = total == 0

    val pageCount: Int get() = maxOf(1, (total + pageSize - 1) / pageSize)

    private fun <T> List<T>.pageSlice() = drop((page - 1) * pageSize).take(pageSize)

    val reviewsPage: List<Review>
        get() = (if (section == ProfileSection.LikedReviews) likedReviews else myReviews).pageSlice()

    val ratingsPage: List<PoemRating> get() = myRatings.pageSlice()

    val poemsPage: List<PoemPreview> get() = likedPoems.pageSlice()

    val statRows: List<Pair<String, String>>
        get() = listOf(
            "Дней с нами" to daysRegistered().toString(),
            "Рецензий написано" to myReviews.size.toString(),
            // рецензия — тоже оценка, поэтому считаем обе группы
            "Оценок поставлено" to (myReviews.size + myRatings.size).toString(),
            "Стихов в избранном" to favoritesCount.toString(),
            "Лайков поставлено" to (likedReviews.size + likedPoems.size).toString(),
            "Лайков получено" to myReviews.sumOf { it.likes }.toString()
        )

    private fun daysRegistered(): Long =
        createdDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()) }?.coerceAtLeast(0) ?: 0

    fun openTab(value: ProfileTab) {
        tab = value
        section = value.sections.firstOrNull() ?: section
        page = 1
    }

    fun openSection(value: ProfileSection) {
        section = value
        page = 1
    }

    fun openPage(value: Int) {
        page = value.coerceIn(1, pageCount)
    }
}
