package com.example.verlibr.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.verlibr.data.PoemPreview
import com.example.verlibr.data.PoemsPage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class PagedPoemsViewModel : ViewModel() {

    private val pageSize = 7

    var poems by mutableStateOf<List<PoemPreview>>(emptyList())
        private set
    var page by mutableIntStateOf(1)
        private set
    var pageCount by mutableIntStateOf(1)
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf(false)
        private set

    private var job: Job? = null

    /** Загружает одну страницу; page начинается с 1. */
    protected abstract suspend fun load(page: Int, pageSize: Int): PoemsPage

    fun refresh() = openPage(page)

    fun openPage(value: Int) {
        job?.cancel()
        job = viewModelScope.launch {
            loading = true
            error = false
            try {
                val target = value.coerceAtLeast(1)
                val result = load(target, pageSize)
                pageCount = maxOf(1, (result.total + pageSize - 1) / pageSize)
                page = target.coerceAtMost(pageCount)
                poems = result.items
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                error = true
            } finally {
                loading = false
            }
        }
    }
}
