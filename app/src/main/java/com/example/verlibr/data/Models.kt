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

data class PoemPreview(
    val id: String,
    val title: String,
    val author: String,
    val year: Int,
    val likes: Int = 0,
    val comments: Int = 0
)

data class AuthorPreview(
    val id: String,
    val name: String,
    val years: String
)
