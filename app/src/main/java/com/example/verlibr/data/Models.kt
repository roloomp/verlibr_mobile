package com.example.verlibr.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val name: String,
    val email: String,
    val avatar: String? = null,
    val banner: String? = null,
    val bio: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)