package com.example.trilhospt.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trails")
data class TrailEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val description: String?,
    val difficulty: String?,
    val distance: Double?,
    val duration: Int?,
    val firstPhotoUrl: String?,
    val isFavorite: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
)
