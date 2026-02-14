package com.example.trilhospt.data.repository

import android.content.Context
import com.example.trilhospt.data.local.dao.TrailDao
import com.example.trilhospt.data.local.database.AppDatabase
import com.example.trilhospt.data.local.entity.TrailEntity
import com.example.trilhospt.data.remote.dto.TrailDto
import kotlinx.coroutines.flow.Flow

class LocalRepository(context: Context) {
    
    private val trailDao: TrailDao = AppDatabase.getDatabase(context).trailDao()
    
    // ========== TRAIL OPERATIONS ==========
    
    fun getAllCachedTrails(): Flow<List<TrailEntity>> {
        return trailDao.getAllTrails()
    }
    
    suspend fun getTrailById(trailId: Int): TrailEntity? {
        return trailDao.getTrailById(trailId)
    }
    
    fun getFavoriteTrails(): Flow<List<TrailEntity>> {
        return trailDao.getFavoriteTrails()
    }
    
    suspend fun cacheTrail(trail: TrailDto) {
        val entity = TrailEntity(
            id = trail.id ?: return,
            name = trail.name ?: "",
            description = trail.description,
            difficulty = trail.difficulty,
            distance = trail.distance,
            duration = trail.duration,
            firstPhotoUrl = trail.firstPhotoUrl,
            isFavorite = false,
            cachedAt = System.currentTimeMillis()
        )
        trailDao.insertTrail(entity)
    }
    
    suspend fun cacheTrails(trails: List<TrailDto>) {
        val entities = trails.mapNotNull { trail ->
            trail.id?.let {
                TrailEntity(
                    id = it,
                    name = trail.name ?: "",
                    description = trail.description,
                    difficulty = trail.difficulty,
                    distance = trail.distance,
                    duration = trail.duration,
                    firstPhotoUrl = trail.firstPhotoUrl,
                    isFavorite = false,
                    cachedAt = System.currentTimeMillis()
                )
            }
        }
        trailDao.insertTrails(entities)
    }
    
    suspend fun toggleFavorite(trailId: Int) {
        val trail = trailDao.getTrailById(trailId)
        if (trail != null) {
            trailDao.updateFavoriteStatus(trailId, !trail.isFavorite)
        }
    }
    
    suspend fun clearOldCache() {
        // Delete trails cached more than 7 days ago
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        trailDao.deleteOldTrails(sevenDaysAgo)
    }
    
    suspend fun clearAllCache() {
        trailDao.deleteAllTrails()
    }
}
