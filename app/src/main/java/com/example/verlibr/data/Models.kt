package com.example.verlibr.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val name: String,
    val email: String,
    val avatar: String? = null,
    val banner: String? = null,
    val bio: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

data class PoemPreview(
    val id: String,
    val title: String,
    val author: String,
    val year: Int?,
    val likes: Int = 0,
    val comments: Int = 0
)

/** Страница списка стихов и общее число записей. */
class PoemsPage(val items: List<PoemPreview>, val total: Int)

data class AuthorPreview(
    val id: String,
    val name: String,
    val years: String
)

const val CRITERIA_COUNT = 5
const val MAX_CRITERION_SCORE = 18
const val MAX_TOTAL_SCORE = CRITERIA_COUNT * MAX_CRITERION_SCORE
const val MIN_REVIEW_LENGTH = 300
const val MAX_REVIEW_LENGTH = 8500

/** Названия критериев; последний («Атмосфера / Вайб») выделяется отдельно. */
val ratingCriteria = listOf(
    "Рифмы / Образы",
    "Структура / Ритмика",
    "Реализация стиля",
    "Индивидуальность / Харизма",
    "Атмосфера/ Вайб"
)

data class PoemTag(val label: String, val value: String)

data class Poem(
    val id: String,
    val title: String,
    val author: String,
    val year: Int?,
    val text: String,
    val tags: List<PoemTag>
)

data class Review(
    val id: String,
    val author: String,
    val title: String,
    val text: String,
    val scores: List<Int>,
    val date: java.time.LocalDate,
    val likes: Int = 0,
    val liked: Boolean = false
) {
    val total: Int get() = scores.sum()
}

enum class ReviewSort(val label: String) {
    New("новые"),
    Popular("популярные")
}

enum class RateMode { Review, Plain }

/** Стихотворение в результатах поиска: превью + данные для поиска и фильтров. */
data class SearchPoem(
    val preview: PoemPreview,
    val text: String,
    val tags: Map<String, String>,
    val rating: Int,
    val favorite: Boolean,
    val rated: Boolean
)

/** Оценка пользователя без рецензии. */
data class PoemRating(
    val poem: PoemPreview,
    val scores: List<Int>
) {
    val total: Int get() = scores.sum()
}
