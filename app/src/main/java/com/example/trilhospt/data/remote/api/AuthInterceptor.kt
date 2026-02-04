package com.example.trilhospt.data.remote.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("TrilhosPT_Prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Se a requisição já tem um header de Authorization, não sobrescrever
        if (originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }
        
        // Obter o token do SharedPreferences
        val token = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        
        // Se não houver token, prosseguir com a requisição original
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }
        
        // Adicionar o header de Authorization com o token
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Token $token")
            .build()
        
        return chain.proceed(newRequest)
    }
    
    // Métodos helper para salvar e limpar o token
    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }
    
    fun clearToken() {
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .apply()
    }
    
    fun getToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }
}
