package com.example.trilhospt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TrailDto(
    val id: Int? = null,
    @SerializedName("user_id")
    val userId: Int? = null,
    val name: String,
    val description: String,
    val difficulty: String,
    val distance: Double,
    val duration: Int,
    @SerializedName("gpx_data")
    val gpxData: String,
    @SerializedName("is_public")
    val isPublic: Boolean,
    @SerializedName("avg_rating")
    val avgRating: Double? = null,
    @SerializedName("total_reviews")
    val totalReviews: Int? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)
