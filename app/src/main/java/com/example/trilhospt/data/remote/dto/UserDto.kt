package com.example.trilhospt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int,
    val username: String,
    val email: String?,
    @SerializedName("profile_photo")
    val profilePhoto: String?,
    @SerializedName("total_distance")
    val totalDistance: Double,
    @SerializedName("total_trails")
    val totalTrails: Int,
    val level: Int,
    @SerializedName("is_staff")
    val isStaff: Boolean = false
)
