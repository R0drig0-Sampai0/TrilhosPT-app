package com.example.trilhospt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PoiDto(
    val id: Int? = null,
    @SerializedName("trail_id")
    val trailId: Int,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    @SerializedName("created_at")
    val createdAt: String? = null
)
