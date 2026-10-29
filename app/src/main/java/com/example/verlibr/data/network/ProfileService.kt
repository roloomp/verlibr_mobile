package com.example.verlibr.data.network

import com.example.verlibr.data.PoemPreview
import com.example.verlibr.data.PoemRating
import com.example.verlibr.data.Review
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
private data class ProfileAuthorName(val name: String)

@Serializable
private data class ProfileRatingRow(
    val id: Int,
    @SerialName("poem_id") val poemId: Int,
    @SerialName("has_review") val hasReview: Boolean,
    @SerialName("score_rhyme") val rhyme: Int = 0,
    @SerialName("score_structure") val structure: Int = 0,
    @SerialName("score_style") val style: Int = 0,
    @SerialName("score_individuality") val individuality: Int = 0,
    @SerialName("score_atmosphere") val atmosphere: Int = 0,
    @SerialName("review_title") val reviewTitle: String? = null,
    @SerialName("review_text") val reviewText: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    val profile: ProfileAuthorName? = null
) {
    // порядок как в ratingCriteria
    val scores: List<Int> get() = listOf(rhyme, structure, style, individuality, atmosphere)
}

@Serializable
private data class ProfileReviewIdRow(@SerialName("review_id") val reviewId: Int)

@Serializable
private data class ProfilePoemIdRow(@SerialName("poem_id") val poemId: Int)

class ProfileData(
    val myReviews: List<Review>,
    /** Оценки без рецензии. */
    val myRatings: List<PoemRating>,
    val likedReviews: List<Review>,
    val likedPoems: List<PoemPreview>,
    val favoritesCount: Int
)

object ProfileService {

    private val db get() = SupabaseConnect.client

    private val ratingColumns = arrayOf(
        "id", "poem_id", "has_review",
        "score_rhyme", "score_structure", "score_style",
        "score_individuality", "score_atmosphere",
        "review_title", "review_text", "created_at"
    )

    suspend fun load(userId: String, myName: String): ProfileData = coroutineScope {
        val mineD = async {
            db.from("ratings")
                .select(Columns.list(*ratingColumns)) {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<ProfileRatingRow>()
        }
        val likedReviewIdsD = async {
            db.from("review_likes")
                .select(Columns.list("review_id")) { filter { eq("user_id", userId) } }
                .decodeList<ProfileReviewIdRow>()
                .map { it.reviewId }
                .distinct()
        }
        val likedPoemIdsD = async {
            db.from("likes")
                .select(Columns.list("poem_id")) {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<ProfilePoemIdRow>()
                .map { it.poemId }
                .distinct()
        }
        val favoritesCountD = async {
            db.from("favorites")
                .select(Columns.list("id")) {
                    filter { eq("user_id", userId) }
                    count(Count.EXACT)
                    limit(1)
                }
                .countOrNull()?.toInt() ?: 0
        }

        val mine = mineD.await()
        val likedReviewIds = likedReviewIdsD.await()
        val likedPoemIds = likedPoemIdsD.await()

        val myReviewRows = mine.filter { it.hasReview }
        val myPlainRows = mine.filter { !it.hasReview }.distinctBy { it.poemId }

        val likedRowsD = async {
            if (likedReviewIds.isEmpty()) {
                emptyList()
            } else {
                db.from("ratings")
                    .select(Columns.list(*ratingColumns, "profile(name)")) {
                        filter {
                            isIn("id", likedReviewIds)
                            eq("has_review", true)
                        }
                    }
                    .decodeList<ProfileRatingRow>()
            }
        }
        val likeCountIds = (myReviewRows.map { it.id } + likedReviewIds).distinct()
        val likeRowsD = async {
            if (likeCountIds.isEmpty()) {
                emptyList()
            } else {
                db.from("review_likes")
                    .select(Columns.list("review_id")) { filter { isIn("review_id", likeCountIds) } }
                    .decodeList<ProfileReviewIdRow>()
            }
        }
        val previewsD = async { PoemService.previewsByIds(myPlainRows.map { it.poemId } + likedPoemIds) }

        val likedRows = likedRowsD.await()
        val likesByReview = likeRowsD.await().groupingBy { it.reviewId }.eachCount()
        val previews = previewsD.await()
        val likedSet = likedReviewIds.toSet()

        ProfileData(
            myReviews = myReviewRows.map {
                it.toReview(myName, likesByReview[it.id] ?: 0, it.id in likedSet)
            },
            myRatings = myPlainRows.mapNotNull { row ->
                previews[row.poemId]?.let { PoemRating(it, row.scores) }
            },
            likedReviews = likedRows
                .map { it.toReview(it.profile?.name ?: "Пользователь", likesByReview[it.id] ?: 0, true) }
                .sortedByDescending { it.date },
            likedPoems = likedPoemIds.mapNotNull { previews[it] },
            favoritesCount = favoritesCountD.await()
        )
    }

    private fun ProfileRatingRow.toReview(author: String, likes: Int, liked: Boolean) = Review(
        id = id.toString(),
        author = author,
        title = reviewTitle.orEmpty(),
        text = reviewText.orEmpty(),
        scores = scores,
        date = runCatching { LocalDate.parse(createdAt.take(10)) }.getOrDefault(LocalDate.now()),
        likes = likes,
        liked = liked
    )
}
