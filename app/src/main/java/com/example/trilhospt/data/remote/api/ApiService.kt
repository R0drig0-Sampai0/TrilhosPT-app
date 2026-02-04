package com.example.trilhospt.data.remote.api

import com.example.trilhospt.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ========== AUTENTICAÇÃO ==========
    
    @POST("api/auth/register/")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("api/auth/login/")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("api/auth/logout/")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>
    
    @GET("api/auth/profile/")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserDto>
    
    @PUT("api/auth/profile/")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body user: UserDto
    ): Response<UserDto>
    
    @Multipart
    @POST("api/auth/profile/photo/")
    suspend fun uploadProfilePhoto(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part
    ): Response<UserDto>
    
    
    // ========== TRILHOS ==========
    
    @GET("api/trails/")
    suspend fun getTrails(
        @Query("difficulty") difficulty: String? = null,
        @Query("min_distance") minDistance: Double? = null,
        @Query("max_distance") maxDistance: Double? = null,
        @Query("search") search: String? = null
    ): Response<List<TrailDto>>
    
    @POST("api/trails/")
    suspend fun createTrail(
        @Header("Authorization") token: String,
        @Body trail: TrailDto
    ): Response<TrailDto>
    
    @GET("api/trails/{id}/")
    suspend fun getTrailById(
        @Path("id") id: Int
    ): Response<TrailDto>
    
    @PUT("api/trails/{id}/")
    suspend fun updateTrail(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body trail: TrailDto
    ): Response<TrailDto>
    
    @DELETE("api/trails/{id}/")
    suspend fun deleteTrail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
    
    @GET("api/trails/my/")
    suspend fun getMyTrails(
        @Header("Authorization") token: String
    ): Response<List<TrailDto>>
    
    @GET("api/trails/{id}/stats/")
    suspend fun getTrailStats(
        @Path("id") id: Int
    ): Response<Map<String, Any>>
    
    
    // ========== FOTOS ==========
    
    @GET("api/photos/")
    suspend fun getPhotos(
        @Query("trail_id") trailId: Int? = null
    ): Response<List<PhotoDto>>
    
    @Multipart
    @POST("api/photos/")
    suspend fun uploadPhoto(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part,
        @Part("trail_id") trailId: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("description") description: RequestBody? = null
    ): Response<PhotoDto>
    
    @DELETE("api/photos/{id}/")
    suspend fun deletePhoto(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
    
    
    // ========== POIs ==========
    
    @GET("api/pois/")
    suspend fun getPois(
        @Query("trail_id") trailId: Int? = null
    ): Response<List<PoiDto>>
    
    @POST("api/pois/")
    suspend fun createPoi(
        @Header("Authorization") token: String,
        @Body poi: PoiDto
    ): Response<PoiDto>
    
    @DELETE("api/pois/{id}/")
    suspend fun deletePoi(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
    
    
    // ========== REVIEWS ==========
    
    @GET("api/reviews/")
    suspend fun getReviews(
        @Query("trail_id") trailId: Int? = null
    ): Response<List<ReviewDto>>
    
    @POST("api/reviews/")
    suspend fun createReview(
        @Header("Authorization") token: String,
        @Body review: ReviewDto
    ): Response<ReviewDto>
    
    @PUT("api/reviews/{id}/")
    suspend fun updateReview(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body review: ReviewDto
    ): Response<ReviewDto>
    
    @DELETE("api/reviews/{id}/")
    suspend fun deleteReview(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
    
    
    // ========== BADGES ==========
    
    @GET("api/badges/my/")
    suspend fun getMyBadges(
        @Header("Authorization") token: String
    ): Response<List<BadgeDto>>
    
    @GET("api/badges/user/{userId}/")
    suspend fun getUserBadges(
        @Path("userId") userId: Int
    ): Response<List<BadgeDto>>
}
