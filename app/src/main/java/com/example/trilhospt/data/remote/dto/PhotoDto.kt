package com.example.trilhospt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PhotoDto(
    val id: Int? = null,
    @SerializedName("trail_id")
    val trailId: Int? = null,
    val image: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String? = null,
    @SerializedName("taken_at")
    val takenAt: String? = null
)
