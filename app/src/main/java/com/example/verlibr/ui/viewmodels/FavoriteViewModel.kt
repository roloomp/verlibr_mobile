package com.example.verlibr.ui.viewmodels

import com.example.verlibr.data.PoemPreview
import com.example.verlibr.data.PoemsPage

class FavoriteViewModel : PagedPoemsViewModel() {

    // TODO: заменить заглушку запросом к Supabase (таблица favorites)
    private val all = List(70) {
        PoemPreview("p$it", "Канал Грибоедова", "Владислав Савенко", 2026, likes = 45, comments = 45)
    }

    override suspend fun load(page: Int, pageSize: Int): PoemsPage =
        PoemsPage(all.drop((page - 1) * pageSize).take(pageSize), all.size)
}
