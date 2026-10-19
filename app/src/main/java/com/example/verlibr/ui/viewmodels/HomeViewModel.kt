package com.example.verlibr.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.verlibr.data.AuthorPreview
import com.example.verlibr.data.PoemPreview

class HomeViewModel : ViewModel() {
    val poemsOfTheDay = List(6) { PoemPreview("d$it", "Канал Грибоедова", "Владислав Савенко", 2026) }
    val editorsPicks = List(6) { PoemPreview("e$it", "Канал Грибоедова", "Владислав Савенко", 2026) }
    val topAuthors = List(6) { AuthorPreview("a$it", "Владислав Савенко", "1969 - н.д.") }
}
