package com.example.verlibr.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.verlibr.data.PoemPreview

abstract class PagedPoemsViewModel : ViewModel() {
    private val all = List(70) {
        PoemPreview("p$it", "Канал Грибоедова", "Владислав Савенко", 2026, likes = 45, comments = 45)
    }

    private val pageSize = 7
    val pageCount = (all.size + pageSize - 1) / pageSize

    var page by mutableIntStateOf(1)
        private set

    val poems: List<PoemPreview>
        get() = all.drop((page - 1) * pageSize).take(pageSize)

    fun openPage(value: Int) {
        page = value.coerceIn(1, pageCount)
    }
}
