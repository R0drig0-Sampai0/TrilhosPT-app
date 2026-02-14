package com.example.trilhospt.data.repository

import com.example.trilhospt.data.remote.api.ApiService
import com.example.trilhospt.data.remote.api.RetrofitClient
import com.example.trilhospt.data.remote.dto.AuthResponse
import com.example.trilhospt.data.remote.dto.BadgeDto
import com.example.trilhospt.data.remote.dto.CompletedTrailDto
import com.example.trilhospt.data.remote.dto.LoginRequest
import com.example.trilhospt.data.remote.dto.PhotoDto
import com.example.trilhospt.data.remote.dto.RegisterRequest
import com.example.trilhospt.data.remote.dto.ReviewDto
import com.example.trilhospt.data.remote.dto.TrailDto
import com.example.trilhospt.data.remote.dto.UserDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

/**
 * Repositório para gerenciar chamadas à API
 * Exemplo de como usar o Retrofit configurado
 */
class ApiRepository {
    
    private val apiService: ApiService = RetrofitClient.getApiService()
    private val authInterceptor = RetrofitClient.getAuthInterceptor()
    
    // ========== AUTENTICAÇÃO ==========
    
    suspend fun register(username: String, email: String, password: String): Response<AuthResponse> {
        val request = RegisterRequest(username, email, password)
        return apiService.register(request)
    }
    
    suspend fun login(username: String, password: String): Response<AuthResponse> {
        val request = LoginRequest(username, password)
        val response = apiService.login(request)
        
        // Se o login for bem-sucedido, salvar o token
        if (response.isSuccessful) {
            response.body()?.token?.let { token ->
                authInterceptor.saveToken(token)
            }
        }
        
        return response
    }
    
    suspend fun logout(): Response<Unit> {
        val token = authInterceptor.getToken()
        if (token != null) {
            val response = apiService.logout("Token $token")
            // Limpar o token após logout
            authInterceptor.clearToken()
            return response
        }
        throw IllegalStateException("Nenhum token disponível para logout")
    }
    
    suspend fun getProfile(): Response<UserDto> {
        val token = authInterceptor.getToken()
        if (token != null) {
            return apiService.getProfile("Token $token")
        }
        throw IllegalStateException("Usuário não autenticado")
    }

    suspend fun getUserById(userId: Int): Response<UserDto> {
        val token = authInterceptor.getToken()
        if (token != null) {
            return apiService.getUserById("Token $token", userId)
        }
        throw IllegalStateException("Usuário não autenticado")
    }
    
    // ========== TRILHOS ==========
    
    suspend fun getTrails(
        difficulty: String? = null,
        minDistance: Double? = null,
        maxDistance: Double? = null,
        search: String? = null,
        userId: Int? = null
    ): Response<List<TrailDto>> {
        val paginatedResponse = apiService.getTrails(difficulty, minDistance, maxDistance, search, userId)
        
        // Unwrap the paginated response to return just the list
        return if (paginatedResponse.isSuccessful && paginatedResponse.body() != null) {
            val results = paginatedResponse.body()!!.results
            Response.success(results)
        } else {
            Response.error(paginatedResponse.code(), paginatedResponse.errorBody()!!)
        }
    }
    
    suspend fun getTrailById(id: Int): Response<TrailDto> {
        return apiService.getTrailById(id)
    }
    
    suspend fun createTrail(trail: TrailDto): Response<TrailDto> {
        val token = authInterceptor.getToken()
        if (token != null) {
            return apiService.createTrail("Token $token", trail)
        }
        throw IllegalStateException("Usuário não autenticado")
    }
    
    suspend fun getMyTrails(): Response<List<TrailDto>> {
        val token = authInterceptor.getToken()
        if (token != null) {
            val response = apiService.getMyTrails("Token $token")
            return if (response.isSuccessful && response.body() != null) {
                Response.success(response.body()!!.results)
            } else {
                if (response.errorBody() != null)
                     Response.error(response.code(), response.errorBody()!!)
                else
                     Response.error(response.code(), okhttp3.ResponseBody.create(null, ""))
            }
        }
        throw IllegalStateException("Usuário não autenticado")
    }

    suspend fun updateTrail(id: Int, trail: TrailDto): Response<TrailDto> {
        val token = authInterceptor.getToken()
        if (token != null) {
            return apiService.updateTrail("Token $token", id, trail)
        }
        throw IllegalStateException("Usuário não autenticado")
    }

    suspend fun deleteTrail(id: Int): Response<Unit> {
        val token = authInterceptor.getToken()
        if (token != null) {
            return apiService.deleteTrail("Token $token", id)
        }
        throw IllegalStateException("Usuário não autenticado")
    }
    
    // ========== REVIEWS ==========
    
    suspend fun getReviews(trailId: Int? = null): Response<List<ReviewDto>> {
        return apiService.getReviews(trailId)
    }
    
    suspend fun createReview(review: ReviewDto): Response<ReviewDto> {
        val token = authInterceptor.getToken()
        if (token != null) {
            return apiService.createReview("Token $token", review)
        }
        throw IllegalStateException("Usuário não autenticado")
    }
    
    // ========== BADGES ==========
    
    suspend fun getMyBadges(): Response<List<BadgeDto>> {
        val token = authInterceptor.getToken()
        if (token != null) {
            return apiService.getMyBadges("Token $token")
        }
        throw IllegalStateException("Usuário não autenticado")
    }

    suspend fun getUserBadges(userId: Int): Response<List<BadgeDto>> {
        val token = authInterceptor.getToken()
        if (token != null) {
            return apiService.getUserBadges(userId)
        }
        throw IllegalStateException("Usuário não autenticado")
    }

    suspend fun getUserPhotos(userId: Int): Response<List<PhotoDto>> {
        return apiService.getUserPhotos(userId)
    }

    suspend fun uploadPhoto(
        image: MultipartBody.Part,
        trailId: RequestBody,
        lat: RequestBody,
        lon: RequestBody,
        description: RequestBody?
    ): Response<PhotoDto> {
        val token = authInterceptor.getToken()
        if (token != null) {
            val authToken = "Token $token"
            return apiService.uploadPhoto(authToken, image, trailId, lat, lon, description)
        }
        throw IllegalStateException("Usuário não autenticado")
    }
    
    // ========== HELPERS ==========
    
    fun isUserAuthenticated(): Boolean {
        return !authInterceptor.getToken().isNullOrEmpty()
    }
    
    fun getCurrentToken(): String? {
        return authInterceptor.getToken()
    }

    // ========== COMPLETED TRAILS ==========

    suspend fun markTrailCompleted(trailId: Int): Response<CompletedTrailDto> {
        val token = authInterceptor.getToken()
        if (token != null) {
            val request = mapOf("trail_id" to trailId)
            return apiService.markTrailCompleted("Token $token", request)
        }
        throw IllegalStateException("User not authenticated")
    }

    suspend fun deleteCompletedTrail(id: Int): Response<Unit> {
        val token = authInterceptor.getToken()
        if (token != null) {
            return apiService.deleteCompletedTrail("Token $token", id)
        }
        throw IllegalStateException("User not authenticated")
    }

    suspend fun getCompletedTrails(userId: Int? = null): Response<List<TrailDto>> {
        val token = authInterceptor.getToken()
        if (token != null) {
            val response = apiService.getCompletedTrails("Token $token", userId)
            if (response.isSuccessful && response.body() != null) {
                val completedTrails = response.body()!!.results.map { it.trail }
                return Response.success(completedTrails)
            } else {
                 if (response.errorBody() != null)
                     return Response.error(response.code(), response.errorBody()!!)
                 else
                     return Response.error(response.code(), okhttp3.ResponseBody.create(null, ""))
            }
        }
        throw IllegalStateException("User not authenticated")
    }

}
