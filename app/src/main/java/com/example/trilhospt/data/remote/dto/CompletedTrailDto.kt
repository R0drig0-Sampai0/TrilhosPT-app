package com.example.trilhospt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CompletedTrailDto(
    val id: Int,
    val user: Int, // Or UserDto if expand, but serializer says UserPublicSerializer? No, serializer definition in backend has user? Usually user ID if not expanding.
    val trail: TrailDto,
    @SerializedName("completed_at")
    val completedAt: String
)
