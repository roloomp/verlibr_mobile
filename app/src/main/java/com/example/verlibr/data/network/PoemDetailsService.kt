package com.example.verlibr.data.network

import com.example.verlibr.data.Poem
import com.example.verlibr.data.PoemTag
import com.example.verlibr.data.Review
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate

@Serializable
private data class IdDto(val id: Int)

@Serializable
private data class ReviewLikeDto(
    @SerialName("review_id") val reviewId: Int,
    @SerialName("user_id") val userId: String
)

@Serializable
private data class AuthorNameDto(val name: String)

@Serializable
private data class PoemDto(
    val id: Int,
    val title: String,
    val author: String,
    val year: Int? = null,
    val content: String,
    @SerialName("tag_type") val tagType: String? = null,
    @SerialName("tag_genre") val tagGenre: String? = null,
    @SerialName("tag_mood") val tagMood: String? = null,
    @SerialName("tag_versification") val tagVersification: String? = null,
    @SerialName("tag_foot") val tagFoot: String? = null,
    @SerialName("tag_stanza") val tagStanza: String? = null,
    @SerialName("tag_rhyme") val tagRhyme: String? = null
) {
    fun toPoem() = Poem(
        id = id.toString(),
        title = title,
        author = author,
        year = year,
        text = content,
        tags = listOf(
            "Тип" to tagType,
            "Жанр" to tagGenre,
            "Настроение" to tagMood,
            "Стихосложение" to tagVersification,
            "Базовая стопа" to tagFoot,
            "Строфа" to tagStanza,
            "Способ рифмовки" to tagRhyme
        ).mapNotNull { (label, value) -> value?.takeIf { it.isNotBlank() }?.let { PoemTag(label, it) } }
    )
}

@Serializable
private data class RatingDto(
    val id: Int,
    @SerialName("user_id") val userId: String,
    @SerialName("has_review") val hasReview: Boolean = false,
    @SerialName("score_rhyme") val rhyme: Int = 0,
    @SerialName("score_style") val style: Int = 0,
    @SerialName("score_structure") val structure: Int = 0,
    @SerialName("score_individuality") val individuality: Int = 0,
    @SerialName("score_atmosphere") val atmosphere: Int = 0,
    @SerialName("review_title") val reviewTitle: String? = null,
    @SerialName("review_text") val reviewText: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val profile: AuthorNameDto? = null
) {
    /** Порядок критериев в приложении: рифмы, структура, стиль, индивидуальность, атмосфера. */
    val appScores: List<Int> get() = listOf(rhyme, structure, style, individuality, atmosphere)
}

/** Собственная оценка пользователя (для заполнения формы). */
class MyRating(val scores: List<Int>, val hasReview: Boolean, val title: String, val text: String)

class PoemData(
    val poem: Poem,
    val likes: Int,
    val liked: Boolean,
    val favorite: Boolean,
    val reviews: List<Review>,
    /** Итоги всех оценок (с рецензией и без). */
    val allTotals: List<Int>,
    val mine: MyRating?
)

object PoemDetailsService {

    private val db get() = SupabaseConnect.client

    suspend fun load(poemId: Int, userId: String?): PoemData = coroutineScope {
        val poem = async { getPoem(poemId) }
        val ratings = async { getRatings(poemId) }
        val likes = async { countLikes(poemId) }
        val liked = async { userId != null && exists("likes", poemId, userId) }
        val favorite = async { userId != null && exists("favorites", poemId, userId) }

        val rows = ratings.await()
        val reviewRows = rows.filter { it.hasReview }
        val reviewLikes = if (reviewRows.isEmpty()) emptyList() else getReviewLikes(reviewRows.map { it.id })

        val reviews = reviewRows.map { r ->
            val own = reviewLikes.filter { it.reviewId == r.id }
            Review(
                id = r.id.toString(),
                author = r.profile?.name ?: "Аноним",
                title = r.reviewTitle.orEmpty(),
                text = r.reviewText.orEmpty(),
                scores = r.appScores,
                date = r.createdAt?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                    ?: LocalDate.now(),
                likes = own.size,
                liked = userId != null && own.any { it.userId == userId }
            )
        }
        val mine = userId?.let { id -> rows.firstOrNull { it.userId == id } }?.let {
            MyRating(it.appScores, it.hasReview, it.reviewTitle.orEmpty(), it.reviewText.orEmpty())
        }

        PoemData(
            poem = poem.await() ?: throw NoSuchElementException("poem $poemId"),
            likes = likes.await(),
            liked = liked.await(),
            favorite = favorite.await(),
            reviews = reviews,
            allTotals = rows.map { it.appScores.sum() },
            mine = mine
        )
    }

    private suspend fun getPoem(poemId: Int): Poem? = db.from("poems")
        .select(
            Columns.list(
                "id", "title", "author", "year", "content",
                "tag_type", "tag_genre", "tag_mood", "tag_versification",
                "tag_foot", "tag_stanza", "tag_rhyme"
            )
        ) { filter { eq("id", poemId) } }
        .decodeSingleOrNull<PoemDto>()?.toPoem()

    private suspend fun getRatings(poemId: Int): List<RatingDto> = db.from("ratings")
        .select(
            Columns.raw(
                "id,user_id,has_review,score_rhyme,score_style,score_structure," +
                    "score_individuality,score_atmosphere,review_title,review_text,created_at,profile(name)"
            )
        ) {
            filter { eq("poem_id", poemId) }
            order("created_at", Order.DESCENDING)
        }
        .decodeList<RatingDto>()

    private suspend fun getReviewLikes(reviewIds: List<Int>): List<ReviewLikeDto> = db.from("review_likes")
        .select(Columns.list("review_id", "user_id")) { filter { isIn("review_id", reviewIds) } }
        .decodeList<ReviewLikeDto>()

    private suspend fun countLikes(poemId: Int): Int {
        val result = db.from("likes").select(Columns.list("id")) {
            filter { eq("poem_id", poemId) }
            count(Count.EXACT)
            range(0L, 0L)
        }
        return result.countOrNull()?.toInt() ?: result.decodeList<IdDto>().size
    }

    /** table: likes или favorites. */
    private suspend fun exists(table: String, poemId: Int, userId: String): Boolean =
        db.from(table).select(Columns.list("id")) {
            filter {
                eq("poem_id", poemId)
                eq("user_id", userId)
            }
            range(0L, 0L)
        }.decodeList<IdDto>().isNotEmpty()

    suspend fun setLike(poemId: Int, userId: String, on: Boolean) = setMark("likes", poemId, userId, on)

    suspend fun setFavorite(poemId: Int, userId: String, on: Boolean) = setMark("favorites", poemId, userId, on)

    private suspend fun setMark(table: String, poemId: Int, userId: String, on: Boolean) {
        if (on) {
            db.from(table).insert(buildJsonObject {
                put("user_id", userId)
                put("poem_id", poemId)
            })
        } else {
            db.from(table).delete {
                filter {
                    eq("poem_id", poemId)
                    eq("user_id", userId)
                }
            }
        }
    }

    suspend fun setReviewLike(reviewId: Int, userId: String, on: Boolean) {
        if (on) {
            db.from("review_likes").insert(buildJsonObject {
                put("user_id", userId)
                put("review_id", reviewId)
            })
        } else {
            db.from("review_likes").delete {
                filter {
                    eq("review_id", reviewId)
                    eq("user_id", userId)
                }
            }
        }
    }

    /**
     * Создаёт или обновляет оценку пользователя (одна на стих, нужен unique (poem_id, user_id)).
     * scores — в порядке приложения: рифмы, структура, стиль, индивидуальность, атмосфера.
     * review = null — оценка без рецензии.
     */
    suspend fun submitRating(poemId: Int, userId: String, scores: List<Int>, review: Pair<String, String>?) {
        db.from("ratings").upsert(
            buildJsonObject {
                put("poem_id", poemId)
                put("user_id", userId)
                put("has_review", review != null)
                put("score_rhyme", scores[0])
                put("score_structure", scores[1])
                put("score_style", scores[2])
                put("score_individuality", scores[3])
                put("score_atmosphere", scores[4])
                put("total_score", scores.sum())
                if (review != null) {
                    put("review_title", review.first)
                    put("review_text", review.second)
                } else {
                    put("review_title", JsonNull)
                    put("review_text", JsonNull)
                }
            }
        ) { onConflict = "poem_id,user_id" }
    }
}
