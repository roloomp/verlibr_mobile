package com.example.verlibr.data.network

import com.example.verlibr.data.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object AuthService {

    /** Состояние сессии: Initializing -> Authenticated / NotAuthenticated. */
    val sessionStatus: StateFlow<SessionStatus>
        get() = SupabaseConnect.client.auth.sessionStatus

    fun currentUserId(): String? =
        SupabaseConnect.client.auth.currentUserOrNull()?.id

    /** Ошибка приходит уже готовым текстом для пользователя (в exception.message). */
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            SupabaseConnect.client.auth.signUpWith(Email) {
                this.email = email.trim()
                this.password = password
                this.data = buildJsonObject {
                    put("name", name.trim())
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception(describe(e, "Не удалось создать аккаунт")))
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            SupabaseConnect.client.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception(describe(e, "Не удалось войти")))
        }
    }

    suspend fun logout(): Boolean {
        return try {
            SupabaseConnect.client.auth.signOut()
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getCurrentProfile(): Profile? {
        return try {
            val userId = SupabaseConnect.client.auth.currentUserOrNull()?.id
                ?: return null

            SupabaseConnect.client
                .from("profile")
                .select(Columns.list("id", "name", "email", "avatar", "banner", "bio", "created_at")) {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<Profile>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Переводит типичные ответы Supabase в понятные сообщения. */
    private fun describe(e: Exception, fallback: String): String {
        val msg = e.message.orEmpty()
        return when {
            "Invalid login credentials" in msg -> "Неверный email или пароль"
            "already registered" in msg -> "Этот email уже зарегистрирован"
            "at least" in msg && "characters" in msg -> "Пароль слишком короткий (минимум 6 символов)"
            "valid email" in msg || "invalid format" in msg -> "Некорректный email"
            e is java.io.IOException ||
                "Unable to resolve host" in msg ||
                "HTTP request" in msg ||
                "timeout" in msg.lowercase() -> "Нет соединения с сервером"
            else -> fallback
        }
    }
}
