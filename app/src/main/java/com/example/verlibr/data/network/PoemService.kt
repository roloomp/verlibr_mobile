package com.example.verlibr.data.network

import com.example.verlibr.data.PoemPreview
import com.example.verlibr.data.PoemsPage
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class PoemPreviewDto(
    val id: Int,
    val title: String,
    val author: String,
    val year: Int? = null,
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("reviews_count") val reviewsCount: Int = 0
) {
    fun toPreview() = PoemPreview(id.toString(), title, author, year, likesCount, reviewsCount)
}

@Serializable
private data class FavoritePoemIdRow(@SerialName("poem_id") val poemId: Int)

object PoemService {

    /** Превью стихов по списку id (представление poem_previews). Порядок не гарантируется. */
    suspend fun previewsByIds(ids: List<Int>): Map<Int, PoemPreview> {
        if (ids.isEmpty()) return emptyMap()
        return SupabaseConnect.client
            .from("poem_previews")
            .select { filter { isIn("id", ids.distinct()) } }
            .decodeList<PoemPreviewDto>()
            .associate { it.id to it.toPreview() }
    }

    /** Избранное пользователя: новые сверху. */
    suspend fun favorites(userId: String, page: Int, pageSize: Int): PoemsPage {
        val from = ((page - 1) * pageSize).toLong()
        val result = SupabaseConnect.client
            .from("favorites")
            .select(Columns.list("poem_id")) {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
                order("id", Order.DESCENDING)
                range(from, from + pageSize - 1)
                count(Count.EXACT)
            }
        // distinct: в БД нет UNIQUE, а одинаковые key в LazyColumn роняют приложение
        val ids = result.decodeList<FavoritePoemIdRow>().map { it.poemId }.distinct()
        val total = result.countOrNull()?.toInt() ?: ids.size
        val previews = previewsByIds(ids)
        return PoemsPage(ids.mapNotNull { previews[it] }, total)
    }

    /** «Горячее»: по числу лайков за последние 30 дней (представление poem_previews). */
    suspend fun hot(page: Int, pageSize: Int): PoemsPage {
        val from = ((page - 1) * pageSize).toLong()
        val result = SupabaseConnect.client
            .from("poem_previews")
            .select {
                order("likes_month", Order.DESCENDING)
                order("likes_count", Order.DESCENDING)
                order("id", Order.ASCENDING)
                range(from, from + pageSize - 1)
                count(Count.EXACT)
            }
        val items = result.decodeList<PoemPreviewDto>().map { it.toPreview() }
        return PoemsPage(items, result.countOrNull()?.toInt() ?: items.size)
    }
}
