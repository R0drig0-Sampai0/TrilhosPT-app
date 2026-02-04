package com.example.trilhospt

import android.app.Application
import com.example.trilhospt.data.remote.api.RetrofitClient

class TrilhosPTApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializa o RetrofitClient com o contexto da aplicação
        RetrofitClient.initialize(this)
    }
}
