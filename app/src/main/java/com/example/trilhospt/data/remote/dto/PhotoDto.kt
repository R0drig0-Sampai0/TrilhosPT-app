package com.example.trilhospt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PhotoDto(
    val id: Int? = null,
    @SerializedName("trail_id")
    val trailId: Int,
    val image: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    @SerializedName("taken_at")
    val takenAt: String? = null
)
