package com.example.trilhospt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ReviewDto(
    val id: Int? = null,
    @SerializedName("trail_id")
    val trailId: Int,
    @SerializedName("user_id")
    val userId: Int? = null,
    val rating: Int,
    val comment: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)
