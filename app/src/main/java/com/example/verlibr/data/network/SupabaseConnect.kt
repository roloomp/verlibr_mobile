package com.example.verlibr.data.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConnect {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://qkmswwfbogwcajvhtnim.supabase.co",
        supabaseKey = "sb_publishable_d6losMPEEmxnl06T-IRG0w_OyXVBWod"
    ) {
        install(Postgrest.Companion)
        install(Auth.Companion)
    }
}