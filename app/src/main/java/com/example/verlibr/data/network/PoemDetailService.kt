package com.example.verlibr.data.network

import com.example.verlibr.data.MAX_CRITERION_SCORE
import com.example.verlibr.data.Poem
import com.example.verlibr.data.PoemTag
import com.example.verlibr.data.Review
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

// ---------- DTO ----------

@Serializable
private data class PoemRow(
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
)

@Serializable
private data class IdRow(val id: Int)

@Serializable
private data class AuthorName(val name: String)

@Serializable
private data class RatingRow(
    val id: Int,
    @SerialName("user_id") val userId: String,
    @SerialName("has_review") val hasReview: Boolean,
    @SerialName("score_rhyme") val rhyme: Int = 0,
    @SerialName("score_structure") val structure: Int = 0,
    @SerialName("score_style") val style: Int = 0,
    @SerialName("score_individuality") val individuality: Int = 0,
    @SerialName("score_atmosphere") val atmosphere: Int = 0,
    @SerialName("total_score") val total: Int = 0,
    @SerialName("review_title") val reviewTitle: String? = null,
    @SerialName("review_text") val reviewText: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    val profile: AuthorName? = null
)

// В классах для insert нет значений по умолчанию намеренно: так все поля точно уйдут в запрос.
@Serializable
private data class UserPoemRow(
    @SerialName("user_id") val userId: String,
    @SerialName("poem_id") val poemId: Int
)

@Serializable
private data class ReviewLikeRow(
    @SerialName("user_id") val userId: String,
    @SerialName("review_id") val reviewId: Int
)

@Serializable
private data class RatingInsert(
    @SerialName("poem_id") val poemId: Int,
    @SerialName("user_id") val userId: String,
    @SerialName("has_review") val hasReview: Boolean,
    @SerialName("score_rhyme") val rhyme: Int,
    @SerialName("score_structure") val structure: Int,
    @SerialName("score_style") val style: Int,
    @SerialName("score_individuality") val individuality: Int,
    @SerialName("score_atmosphere") val atmosphere: Int,
    @SerialName("total_score") val total: Int,
    @SerialName("review_title") val reviewTitle: String?,
    @SerialName("review_text") val reviewText: String?
)

// ---------- Результаты ----------

class AlreadyRatedException : Exception("Вы уже оценили это стихотворение")

class PoemRatings(
    /** Только оценки с рецензией. */
    val reviews: List<Review>,
    /** Итоговые баллы оценок с рецензией. */
    val reviewTotals: List<Int>,
    /** Итоговые баллы всех оценок (с рецензией и без). */
    val allTotals: List<Int>,
    /** Текущий пользователь уже оценивал это стихотворение. */
    val ratedByMe: Boolean
)

// ---------- Сервис ----------

object PoemDetailService {

    private val db get() = SupabaseConnect.client

    suspend fun getPoem(id: Int): Poem? {
        val row = db.from("poems")
            .select(
                Columns.list(
                    "id", "title", "author", "year", "content",
                    "tag_type", "tag_genre", "tag_mood", "tag_versification",
                    "tag_foot", "tag_stanza", "tag_rhyme"
                )
            ) { filter { eq("id", id) } }
            .decodeSingleOrNull<PoemRow>() ?: return null

        return Poem(
            id = row.id.toString(),
            title = row.title,
            author = row.author,
            year = row.year,
            text = row.content,
            tags = listOfNotNull(
                tag("Тип", row.tagType),
                tag("Жанр", row.tagGenre),
                tag("Настроение", row.tagMood),
                tag("Стихосложение", row.tagVersification),
                tag("Базовая стопа", row.tagFoot),
                tag("Строфа", row.tagStanza),
                tag("Способ рифмовки", row.tagRhyme)
            )
        )
    }

    private fun tag(label: String, value: String?): PoemTag? =
        value?.takeIf { it.isNotBlank() }?.let { PoemTag(label, it) }

    suspend fun likesCount(poemId: Int): Int {
        val result = db.from("likes").select(Columns.list("id")) {
            filter { eq("poem_id", poemId) }
            count(Count.EXACT)
            limit(1)
        }
        return result.countOrNull()?.toInt() ?: 0
    }

    suspend fun isLiked(poemId: Int, userId: String): Boolean = exists("likes", poemId, userId)

    suspend fun isFavorite(poemId: Int, userId: String): Boolean = exists("favorites", poemId, userId)

    suspend fun setLiked(poemId: Int, userId: String, on: Boolean) = setMark("likes", poemId, userId, on)

    suspend fun setFavorite(poemId: Int, userId: String, on: Boolean) = setMark("favorites", poemId, userId, on)

    /** В БД нет UNIQUE на (user_id, poem_id), поэтому дубли отсекаем проверкой перед вставкой. */
    private suspend fun exists(table: String, poemId: Int, userId: String): Boolean =
        db.from(table).select(Columns.list("id")) {
            filter {
                eq("poem_id", poemId)
                eq("user_id", userId)
            }
            limit(1)
        }.decodeList<IdRow>().isNotEmpty()

    private suspend fun setMark(table: String, poemId: Int, userId: String, on: Boolean) {
        if (on) {
            if (!exists(table, poemId, userId)) {
                db.from(table).insert(UserPoemRow(userId = userId, poemId = poemId))
            }
        } else {
            db.from(table).delete {
                filter {
                    eq("poem_id", poemId)
                    eq("user_id", userId)
                }
            }
        }
    }

    suspend fun getRatings(poemId: Int, userId: String?): PoemRatings {
        val rows = db.from("ratings")
            .select(
                Columns.list(
                    "id", "user_id", "has_review",
                    "score_rhyme", "score_structure", "score_style",
                    "score_individuality", "score_atmosphere", "total_score",
                    "review_title", "review_text", "created_at",
                    "profile(name)"
                )
            ) {
                filter { eq("poem_id", poemId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<RatingRow>()

        val reviewRows = rows.filter { it.hasReview }
        val reviewIds = reviewRows.map { it.id }

        val likeRows = if (reviewIds.isEmpty()) {
            emptyList()
        } else {
            db.from("review_likes")
                .select(Columns.list("user_id", "review_id")) {
                    filter { isIn("review_id", reviewIds) }
                }
                .decodeList<ReviewLikeRow>()
        }
        val likesByReview = likeRows.groupBy { it.reviewId }

        val reviews = reviewRows.map { row ->
            val likes = likesByReview[row.id].orEmpty()
            Review(
                id = row.id.toString(),
                author = row.profile?.name ?: "Пользователь",
                title = row.reviewTitle.orEmpty(),
                text = row.reviewText.orEmpty(),
                // порядок как в ratingCriteria: рифмы, структура, стиль, индивидуальность, атмосфера
                scores = listOf(row.rhyme, row.structure, row.style, row.individuality, row.atmosphere),
                date = parseDate(row.createdAt),
                likes = likes.size,
                liked = userId != null && likes.any { it.userId == userId }
            )
        }

        return PoemRatings(
            reviews = reviews,
            reviewTotals = reviewRows.map { it.total },
            allTotals = rows.map { it.total },
            ratedByMe = userId != null && rows.any { it.userId == userId }
        )
    }

    /** scores — в порядке ratingCriteria. title/text = null для оценки без рецензии. */
    suspend fun submitRating(
        poemId: Int,
        userId: String,
        scores: List<Int>,
        title: String?,
        text: String?
    ): Result<Unit> {
        return try {
            if (exists("ratings", poemId, userId)) {
                return Result.failure(AlreadyRatedException())
            }
            val s = scores.map { it.coerceIn(0, MAX_CRITERION_SCORE) }
            db.from("ratings").insert(
                RatingInsert(
                    poemId = poemId,
                    userId = userId,
                    hasReview = text != null,
                    rhyme = s[0],
                    structure = s[1],
                    style = s[2],
                    individuality = s[3],
                    atmosphere = s[4],
                    total = s.sum(),
                    reviewTitle = title,
                    reviewText = text
                )
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Не удалось сохранить оценку. Проверьте соединение"))
        }
    }

    suspend fun setReviewLiked(reviewId: Int, userId: String, on: Boolean) {
        if (on) {
            val already = db.from("review_likes").select(Columns.list("id")) {
                filter {
                    eq("review_id", reviewId)
                    eq("user_id", userId)
                }
                limit(1)
            }.decodeList<IdRow>().isNotEmpty()
            if (!already) {
                db.from("review_likes").insert(ReviewLikeRow(userId = userId, reviewId = reviewId))
            }
        } else {
            db.from("review_likes").delete {
                filter {
                    eq("review_id", reviewId)
                    eq("user_id", userId)
                }
            }
        }
    }

    private fun parseDate(value: String): LocalDate =
        runCatching { LocalDate.parse(value.take(10)) }.getOrDefault(LocalDate.now())
}
