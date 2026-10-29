package com.example.verlibr.ui.viewmodels

import com.example.verlibr.data.PoemsPage
import com.example.verlibr.data.network.PoemService

class FavoriteViewModel : PagedPoemsViewModel() {

    private var userId: String? = null

    /** При смене пользователя список перезагружается. */
    fun setUser(id: String?) {
        if (id == userId) return
        userId = id
        refresh()
    }

    override suspend fun load(page: Int, pageSize: Int): PoemsPage {
        val uid = userId ?: return PoemsPage(emptyList(), 0)
        return PoemService.favorites(uid, page, pageSize)
    }
}
