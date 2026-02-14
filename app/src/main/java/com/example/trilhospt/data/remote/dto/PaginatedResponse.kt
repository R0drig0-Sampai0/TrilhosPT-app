package com.example.trilhospt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PaginatedResponse<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>
)
