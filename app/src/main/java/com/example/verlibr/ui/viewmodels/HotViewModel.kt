package com.example.verlibr.ui.viewmodels

import com.example.verlibr.data.PoemsPage
import com.example.verlibr.data.network.PoemService

class HotViewModel : PagedPoemsViewModel() {
    override suspend fun load(page: Int, pageSize: Int): PoemsPage = PoemService.hot(page, pageSize)
}
