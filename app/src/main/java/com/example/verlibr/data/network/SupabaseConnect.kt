package com.example.verlibr.data.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConnect {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://qkmswwfbogwcajvhtnim.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFrbXN3d2Zib2d3Y2Fqdmh0bmltIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODkwODE5MjksImV4cCI6MjEwNDY1NzkyOX0.D_pMHwUXmXGh3zzRbyu7KeWT-6MPg3kJ_QYgVxSLuRk"
    ) {
        install(Postgrest)
        install(Auth)
    }
}