package com.example.trilhospt.data.remote.api

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // Base URL para o servidor de produção (PythonAnywhere)
    const val BASE_URL = "https://rodrigosampaio.pythonanywhere.com/"
    
    // Timeout em segundos
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L
    
    private var authInterceptor: AuthInterceptor? = null
    private var retrofit: Retrofit? = null
    
    /**
     * Inicializa o RetrofitClient com o contexto da aplicação
     * DEVE ser chamado antes de usar getApiService()
     */
    fun initialize(context: Context) {
        authInterceptor = AuthInterceptor(context.applicationContext)
        retrofit = null // Reset para forçar recriação com o novo interceptor
    }
    
    /**
     * Retorna o AuthInterceptor para gerenciar tokens
     */
    fun getAuthInterceptor(): AuthInterceptor {
        return authInterceptor ?: throw IllegalStateException(
            "RetrofitClient não foi inicializado. Chame initialize(context) primeiro."
        )
    }
    
    /**
     * Cria ou retorna a instância do Gson configurada
     */
    private fun getGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }
    
    /**
     * Cria o OkHttpClient com interceptors
     */
    private fun getOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val authInterceptor = this.authInterceptor ?: throw IllegalStateException(
            "RetrofitClient não foi inicializado. Chame initialize(context) primeiro."
        )
        
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()
    }
    
    /**
     * Cria ou retorna a instância do Retrofit
     */
    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .build()
        }
        return retrofit!!
    }
    
    /**
     * Retorna a instância do ApiService
     */
    fun getApiService(): ApiService {
        return getRetrofit().create(ApiService::class.java)
    }
    
    /**
     * Permite alterar a BASE_URL se necessário (útil para testes ou diferentes ambientes)
     */
    fun createApiService(baseUrl: String): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(getGson()))
            .build()
        
        return retrofit.create(ApiService::class.java)
    }
}
