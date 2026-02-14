package com.example.trilhospt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PointOfInterestDto(
    val id: Int? = null,
    val trail: Int? = null,
    val user: UserDto? = null,
    val type: String? = null,
    @SerializedName("type_display")
    val typeDisplay: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)
