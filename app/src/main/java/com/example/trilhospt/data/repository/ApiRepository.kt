package com.example.trilhospt.data.repository

import com.example.trilhospt.data.remote.api.ApiService
import com.example.trilhospt.data.remote.api.RetrofitClient
import com.example.trilhospt.data.remote.dto.*
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
    
    // ========== TRILHOS ==========
    
    suspend fun getTrails(
        difficulty: String? = null,
        minDistance: Double? = null,
        maxDistance: Double? = null,
        search: String? = null
    ): Response<List<TrailDto>> {
        return apiService.getTrails(difficulty, minDistance, maxDistance, search)
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
            return apiService.getMyTrails("Token $token")
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
    
    // ========== HELPERS ==========
    
    fun isUserAuthenticated(): Boolean {
        return !authInterceptor.getToken().isNullOrEmpty()
    }
    
    fun getCurrentToken(): String? {
        return authInterceptor.getToken()
    }
}
