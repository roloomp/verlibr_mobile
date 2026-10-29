package com.example.verlibr.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.verlibr.data.PoemsPage
import com.example.verlibr.data.network.AuthorInfo
import com.example.verlibr.data.network.AuthorService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Список стихов автора (пагинация из базового класса) + данные самого автора для шапки экрана. */
class AuthorViewModel(savedStateHandle: SavedStateHandle) : PagedPoemsViewModel() {

    private val authorId: Int? = savedStateHandle.get<String>("id")?.toIntOrNull()

    var author by mutableStateOf<AuthorInfo?>(null)
        private set
    var authorLoading by mutableStateOf(true)
        private set
    var authorError by mutableStateOf(false)
        private set

    init {
        loadAuthor()
    }

    fun loadAuthor() {
        val id = authorId
        if (id == null) {
            authorLoading = false
            return
        }
        viewModelScope.launch {
            authorLoading = true
            authorError = false
            try {
                author = AuthorService.getAuthor(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                authorError = true
            } finally {
                authorLoading = false
            }
        }
    }

    override suspend fun load(page: Int, pageSize: Int): PoemsPage {
        val id = authorId ?: return PoemsPage(emptyList(), 0)
        return AuthorService.poems(id, page, pageSize)
    }
}
