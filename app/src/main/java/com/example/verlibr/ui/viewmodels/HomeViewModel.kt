package com.example.verlibr.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.verlibr.data.AuthorPreview
import com.example.verlibr.data.PoemPreview
import com.example.verlibr.data.network.HomeService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    var poemsOfTheDay by mutableStateOf<List<PoemPreview>>(emptyList())
        private set
    var editorsPicks by mutableStateOf<List<PoemPreview>>(emptyList())
        private set
    var topAuthors by mutableStateOf<List<AuthorPreview>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf(false)
        private set

    val hasData: Boolean
        get() = poemsOfTheDay.isNotEmpty() || editorsPicks.isNotEmpty() || topAuthors.isNotEmpty()

    private var job: Job? = null

    /** Вызывается при каждом открытии экрана; прежние данные остаются, пока идёт обновление. */
    fun load() {
        job?.cancel()
        job = viewModelScope.launch {
            loading = true
            error = false
            try {
                val data = HomeService.load()
                poemsOfTheDay = data.poemsOfTheDay
                editorsPicks = data.editorsPicks
                topAuthors = data.topAuthors
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
