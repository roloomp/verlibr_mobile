package com.example.verlibr.data.network

import com.example.verlibr.data.AuthorPreview
import com.example.verlibr.data.PoemPreview
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class HomeIdRow(val id: Int)

@Serializable
private data class HomePoemAuthorRow(
    val id: Int,
    @SerialName("author_id") val authorId: Int? = null
)

@Serializable
private data class HomePoemLikesRow(
    val id: Int,
    @SerialName("likes_count") val likesCount: Int = 0
)

@Serializable
private data class HomeAuthorRow(
    val id: Int,
    val name: String,
    @SerialName("birth_year") val birthYear: Int? = null,
    @SerialName("death_year") val deathYear: Int? = null
)

class HomeData(
    val poemsOfTheDay: List<PoemPreview>,
    val editorsPicks: List<PoemPreview>,
    val topAuthors: List<AuthorPreview>
)

object HomeService {

    private const val LIMIT = 6

    private val db get() = SupabaseConnect.client

    suspend fun load(): HomeData = coroutineScope {
        val dailyD = async { flagged("is_daily") }
        val editorsD = async { flagged("is_editors") }
        val authorsD = async { topAuthors() }
        HomeData(dailyD.await(), editorsD.await(), authorsD.await())
    }

    /** «Находки дня» / «Выбор редакции»: стихи с флагом в таблице poems, новые сверху. */
    private suspend fun flagged(column: String): List<PoemPreview> {
        val ids = db.from("poems")
            .select(Columns.list("id")) {
                filter { eq(column, true) }
                order("id", Order.DESCENDING)
                limit(LIMIT.toLong())
            }
            .decodeList<HomeIdRow>()
            .map { it.id }
        val previews = PoemService.previewsByIds(ids)
        return ids.mapNotNull { previews[it] }
    }

    /**
     * «Лучшие авторы»: сумма лайков всех стихов автора, при равенстве — больше стихов,
     * затем по имени. Считается на клиенте по двум лёгким запросам (id + author_id и id + likes_count).
     */
    private suspend fun topAuthors(): List<AuthorPreview> = coroutineScope {
        val poemsD = async {
            db.from("poems")
                .select(Columns.list("id", "author_id"))
                .decodeList<HomePoemAuthorRow>()
        }
        val likesD = async {
            db.from("poem_previews")
                .select(Columns.list("id", "likes_count"))
                .decodeList<HomePoemLikesRow>()
                .associate { it.id to it.likesCount }
        }
        val poems = poemsD.await().filter { it.authorId != null }
        val likes = likesD.await()

        val stats = poems.groupBy { it.authorId!! }
            .map { (authorId, items) ->
                Triple(authorId, items.sumOf { likes[it.id] ?: 0 }, items.size)
            }
        val topIds = stats
            .sortedWith(compareByDescending<Triple<Int, Int, Int>> { it.second }.thenByDescending { it.third })
            .take(LIMIT * 2) // запас: имена нужны для итоговой сортировки при равенстве
            .map { it.first }
        if (topIds.isEmpty()) return@coroutineScope emptyList()

        val authors = db.from("authors")
            .select(Columns.list("id", "name", "birth_year", "death_year")) {
                filter { isIn("id", topIds) }
            }
            .decodeList<HomeAuthorRow>()
            .associateBy { it.id }

        val rank = stats.associate { it.first to it }
        topIds
            .mapNotNull { id -> authors[id]?.let { id to it } }
            .sortedWith(
                compareByDescending<Pair<Int, HomeAuthorRow>> { rank[it.first]!!.second }
                    .thenByDescending { rank[it.first]!!.third }
                    .thenBy { it.second.name }
            )
            .take(LIMIT)
            .map { (_, a) -> AuthorPreview(a.id.toString(), a.name, years(a.birthYear, a.deathYear)) }
    }

    private fun years(birth: Int?, death: Int?): String =
        if (birth == null && death == null) "" else "${birth ?: "?"} - ${death ?: "н.д."}"
}
