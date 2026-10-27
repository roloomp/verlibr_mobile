package com.example.verlibr.ui.viewmodels

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.verlibr.data.MAX_TOTAL_SCORE
import com.example.verlibr.data.PoemPreview
import com.example.verlibr.data.SearchPoem

enum class SearchSort(val label: String) {
    Popularity("Популярности"),
    Rating("Рейтингу"),
    Year("Году")
}

private val tagKeys = listOf(
    "Тип", "Жанр", "Настроение", "Стихосложение", "Базовая стопа", "Строфа", "Способ рифмовки"
)

private fun <T> List<T>.at(i: Int) = this[i % size]

// Пока данных из Supabase нет — тестовая выборка (как в PagedPoemsViewModel).
private fun mockPoem(i: Int) = SearchPoem(
    preview = PoemPreview(
        id = "s$i",
        title = listOf("Канал Грибоедова", "Над водой", "Ночной разговор", "Осенний свет").at(i),
        author = listOf("Владислав Савенко", "Анна Лесная", "Игорь Белов").at(i / 2),
        year = 2026 - i % 5,
        likes = (i * 7) % 50,
        comments = (i * 3) % 30
    ),
    text = listOf(
        "Над каналом заледенелым",
        "Жизнь оттает и серой льдинкой",
        "Где крылатые львы застыли"
    ).at(i / 3),
    tags = mapOf(
        "Тип" to listOf("авторский стих", "перевод").at(i),
        "Жанр" to listOf("философская лирика", "пейзажная лирика", "любовная лирика").at(i / 2),
        "Настроение" to listOf("меланхоличное", "светлое", "тревожное").at(i / 3),
        "Стихосложение" to listOf("силлабо-тоническое", "верлибр").at(i / 5),
        "Базовая стопа" to listOf("двусложник", "трёхсложник").at(i / 7),
        "Строфа" to listOf("катрены", "терцеты").at(i / 4),
        "Способ рифмовки" to listOf("ABAB", "AABB", "ABBA").at(i / 6)
    ),
    rating = (i * 13) % (MAX_TOTAL_SCORE + 1),
    favorite = i % 6 == 0,
    rated = i % 4 == 0
)

class SearchViewModel : ViewModel() {
    private val all = List(70, ::mockPoem)
    private val pageSize = 7

    val ratingBounds = 0f..MAX_TOTAL_SCORE.toFloat()
    val options: Map<String, List<String>> =
        tagKeys.associateWith { key -> all.map { it.tags.getValue(key) }.distinct() }

    var query by mutableStateOf("")
        private set
    var sort by mutableStateOf(SearchSort.Popularity)
        private set
    var hideFavorite by mutableStateOf(false)
        private set
    var hideRated by mutableStateOf(false)
        private set
    var ratingRange by mutableStateOf(ratingBounds)
        private set
    var selected by mutableStateOf<Map<String, Set<String>>>(emptyMap())
        private set
    var page by mutableIntStateOf(1)
        private set

    val results: List<SearchPoem> by derivedStateOf {
        val q = query.trim()
        val order = when (sort) {
            SearchSort.Popularity -> compareByDescending<SearchPoem> { it.preview.likes }
            SearchSort.Rating -> compareByDescending<SearchPoem> { it.rating }
            SearchSort.Year -> compareByDescending<SearchPoem> { it.preview.year }
        }
        all.filter { p ->
            (q.isEmpty() || listOf(p.preview.title, p.preview.author, p.text, p.tags["Настроение"].orEmpty())
                .any { it.contains(q, ignoreCase = true) }) &&
                !(hideFavorite && p.favorite) &&
                !(hideRated && p.rated) &&
                p.rating.toFloat() in ratingRange &&
                selected.all { (key, values) -> values.isEmpty() || p.tags[key] in values }
        }.sortedWith(order)
    }

    val pageCount: Int by derivedStateOf { maxOf(1, (results.size + pageSize - 1) / pageSize) }

    val pagePoems: List<SearchPoem> by derivedStateOf {
        results.drop((page - 1) * pageSize).take(pageSize)
    }

    val activeFilters: Int by derivedStateOf {
        selected.values.sumOf { it.size } +
            (if (hideFavorite) 1 else 0) +
            (if (hideRated) 1 else 0) +
            (if (ratingRange != ratingBounds) 1 else 0)
    }

    fun onQueryChange(value: String) { query = value; page = 1 }
    fun onSortChange(value: SearchSort) { sort = value; page = 1 }
    fun onHideFavoriteChange(value: Boolean) { hideFavorite = value; page = 1 }
    fun onHideRatedChange(value: Boolean) { hideRated = value; page = 1 }
    fun setRating(value: ClosedFloatingPointRange<Float>) { ratingRange = value; page = 1 }

    fun toggleOption(key: String, value: String) {
        val current = selected[key].orEmpty()
        selected = selected + (key to if (value in current) current - value else current + value)
        page = 1
    }

    fun resetFilters() {
        hideFavorite = false
        hideRated = false
        ratingRange = ratingBounds
        selected = emptyMap()
        page = 1
    }

    fun openPage(value: Int) {
        page = value.coerceIn(1, pageCount)
    }
}
