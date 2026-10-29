package com.example.verlibr.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.verlibr.data.Profile
import com.example.verlibr.data.network.AuthService
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface AuthState {
    /** Сессия ещё поднимается из хранилища или грузится профиль. */
    object Loading : AuthState

    object Guest : AuthState

    data class LoggedIn(val profile: Profile) : AuthState
}

/**
 * Единственный владелец состояния авторизации. Создаётся один раз на уровне
 * навигатора; источник правды — sessionStatus из supabase-kt.
 */
class AuthViewModel : ViewModel() {

    var state by mutableStateOf<AuthState>(AuthState.Loading)
        private set

    val profile: Profile? get() = (state as? AuthState.LoggedIn)?.profile

    val userId: String? get() = profile?.id

    init {
        viewModelScope.launch {
            AuthService.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> onAuthenticated(status)
                    is SessionStatus.NotAuthenticated -> state = AuthState.Guest
                    is SessionStatus.Initializing ->
                        if (state !is AuthState.LoggedIn) state = AuthState.Loading
                    else -> Unit
                }
            }
        }
    }

    private suspend fun onAuthenticated(status: SessionStatus.Authenticated) {
        val user = status.session.user
        val id = user?.id ?: AuthService.currentUserId() ?: return
        // обновление токена тоже приходит как Authenticated — профиль заново не грузим
        if (profile?.id == id) return
        state = AuthState.LoggedIn(loadProfile(id, user?.email.orEmpty()))
    }

    /**
     * Строка в profile создаётся на стороне Supabase и может появиться с небольшой
     * задержкой после регистрации, поэтому пробуем несколько раз. Если так и не
     * нашлась — входим с минимальным профилем, чтобы приложение не зависло в гостях.
     */
    private suspend fun loadProfile(id: String, email: String): Profile {
        repeat(3) {
            AuthService.getCurrentProfile()?.let { return it }
            delay(400)
        }
        return Profile(
            id = id,
            name = email.substringBefore('@').ifBlank { "Пользователь" },
            email = email
        )
    }

    // Профиль подтянется сам через sessionStatus, поэтому здесь только результат запроса.
    suspend fun login(email: String, password: String): Result<Unit> =
        AuthService.login(email, password)

    suspend fun register(name: String, email: String, password: String): Result<Unit> =
        AuthService.register(name, email, password)

    suspend fun logout(): Boolean = AuthService.logout()
}
