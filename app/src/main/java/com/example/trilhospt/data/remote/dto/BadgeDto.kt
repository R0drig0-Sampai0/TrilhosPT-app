package com.example.trilhospt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BadgeDto(
    val id: Int? = null,
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("badge_type")
    val badgeType: String,
    @SerializedName("earned_at")
    val earnedAt: String? = null
)
