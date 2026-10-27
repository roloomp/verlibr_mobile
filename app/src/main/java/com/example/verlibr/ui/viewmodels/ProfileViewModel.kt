package com.example.verlibr.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.verlibr.data.PoemPreview
import com.example.verlibr.data.PoemRating
import com.example.verlibr.data.Profile
import com.example.verlibr.data.Review
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

    // TODO: заменить заглушки запросами к Supabase
    val profile = Profile(
        id = "me",
        name = "Nickname",
        email = "",
        bio = "Биография так называемая",
        createdAt = "2026-02-28"
    )

    private val createdDate: LocalDate? = profile.createdAt?.take(10)?.let { LocalDate.parse(it) }

    val registeredAt: String =
        createdDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "—"

    private fun mockReviews(prefix: String, author: String, liked: Boolean) = List(8) {
        Review(
            id = "$prefix$it",
            author = author,
            title = "Над заледенелым каналом: разговор с городом и собой",
            text = "Короткое, но ёмкое стихотворение, в котором пейзаж становится исповедью. " +
                "Автору удивительно точно удалось передать щемящее чувство, когда прошлое давит на плечи.",
            scores = listOf(9, 9, 9, 9, 9),
            date = LocalDate.of(2026, 2, 28),
            likes = 7,
            liked = liked
        )
    }

    private val myReviews = mockReviews("r", profile.name, liked = false)
    private val likedReviews = mockReviews("lr", "Владислав Савенко", liked = true)

    private val myRatings = List(6) {
        PoemRating(
            poem = PoemPreview("m$it", "Канал Грибоедова", "Владислав Савенко", 2026),
            scores = listOf(9, 14, 12, 7, 16)
        )
    }

    private val likedPoems = List(12) {
        PoemPreview("l$it", "Канал Грибоедова", "Владислав Савенко", 2026, likes = 45, comments = 45)
    }

    private val favoritesCount = 14

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
            "Оценок поставлено" to myRatings.size.toString(),
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
