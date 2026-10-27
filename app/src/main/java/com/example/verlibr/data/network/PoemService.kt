package com.example.verlibr.data.network

import com.example.verlibr.data.PoemPreview
import com.example.verlibr.data.PoemsPage
import io.github.jan.supabase.postgrest.from
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

object PoemService {

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
