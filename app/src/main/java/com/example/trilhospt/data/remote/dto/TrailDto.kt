package com.example.trilhospt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TrailDto(
    val id: Int? = null,
    @SerializedName("user_id")
    val userId: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val difficulty: String? = null,
    val distance: Double? = null,
    val duration: Int? = null,
    @SerializedName("gpx_data")
    val gpxData: Any? = null,  // Can be String, Array, or Object - we'll handle it flexibly
    @SerializedName("is_public")
    val isPublic: Boolean? = null,
    @SerializedName("avg_rating")
    val avgRating: Double? = null,
    @SerializedName("total_reviews")
    val totalReviews: Int? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("first_photo_url")
    val firstPhotoUrl: String? = null,
    val photos: List<PhotoDto>? = null,
    val reviews: List<ReviewDto>? = null,
    @SerializedName("completed_trail_id")
    val completedTrailId: Int? = null,
    val pois: List<PointOfInterestDto>? = null
)
