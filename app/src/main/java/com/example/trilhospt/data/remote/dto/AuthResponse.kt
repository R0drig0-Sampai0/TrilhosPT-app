package com.example.trilhospt.data.remote.dto

data class AuthResponse(
    val token: String,
    val user: UserDto? = null
)
