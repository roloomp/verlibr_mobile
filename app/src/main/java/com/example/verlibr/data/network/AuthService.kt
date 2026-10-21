package com.example.verlibr.data.network

import com.example.verlibr.data.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object AuthService {
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            SupabaseConnect.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("name", name)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        return try {
            SupabaseConnect.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            SupabaseConnect.client.auth.currentUserOrNull() != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun logout(): Boolean {
        return try {
            SupabaseConnect.client.auth.signOut()
            true
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
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
