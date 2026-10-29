package com.example.verlibr.data.network

import com.example.verlibr.data.PoemsPage
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class AuthorDetailRow(
    val id: Int,
    val name: String,
    @SerialName("birth_year") val birthYear: Int? = null,
    @SerialName("death_year") val deathYear: Int? = null,
    val bio: String? = null
)

@Serializable
private data class AuthorPoemIdRow(val id: Int)

class AuthorInfo(
    val id: String,
    val name: String,
    /** «1799 - 1837», «1969 - н.д.» или пустая строка, если годов нет. */
    val years: String,
    val bio: String?
)

object AuthorService {

    private val db get() = SupabaseConnect.client

    suspend fun getAuthor(id: Int): AuthorInfo? {
        val row = db.from("authors")
            .select(Columns.list("id", "name", "birth_year", "death_year", "bio")) {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<AuthorDetailRow>() ?: return null
        return AuthorInfo(
            id = row.id.toString(),
            name = row.name,
            years = years(row.birthYear, row.deathYear),
            bio = row.bio?.trim()?.takeIf { it.isNotEmpty() }
        )
    }

    /** Стихи автора по алфавиту; превью (лайки, число рецензий) берутся из poem_previews. */
    suspend fun poems(authorId: Int, page: Int, pageSize: Int): PoemsPage {
        val from = ((page - 1) * pageSize).toLong()
        val result = db.from("poems")
            .select(Columns.list("id")) {
                filter { eq("author_id", authorId) }
                order("title", Order.ASCENDING)
                order("id", Order.ASCENDING)
                range(from, from + pageSize - 1)
                count(Count.EXACT)
            }
        val ids = result.decodeList<AuthorPoemIdRow>().map { it.id }
        val total = result.countOrNull()?.toInt() ?: ids.size
        val previews = PoemService.previewsByIds(ids)
        return PoemsPage(ids.mapNotNull { previews[it] }, total)
    }

    private fun years(birth: Int?, death: Int?): String =
        if (birth == null && death == null) "" else "${birth ?: "?"} - ${death ?: "н.д."}"
}
