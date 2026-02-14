package com.example.trilhospt.data.local.dao

import androidx.room.*
import com.example.trilhospt.data.local.entity.TrailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrailDao {
    
    @Query("SELECT * FROM trails ORDER BY cachedAt DESC")
    fun getAllTrails(): Flow<List<TrailEntity>>
    
    @Query("SELECT * FROM trails WHERE id = :trailId")
    suspend fun getTrailById(trailId: Int): TrailEntity?
    
    @Query("SELECT * FROM trails WHERE isFavorite = 1")
    fun getFavoriteTrails(): Flow<List<TrailEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrail(trail: TrailEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrails(trails: List<TrailEntity>)
    
    @Update
    suspend fun updateTrail(trail: TrailEntity)
    
    @Query("UPDATE trails SET isFavorite = :isFavorite WHERE id = :trailId")
    suspend fun updateFavoriteStatus(trailId: Int, isFavorite: Boolean)
    
    @Delete
    suspend fun deleteTrail(trail: TrailEntity)
    
    @Query("DELETE FROM trails")
    suspend fun deleteAllTrails()
    
    @Query("DELETE FROM trails WHERE cachedAt < :timestamp")
    suspend fun deleteOldTrails(timestamp: Long)
}
