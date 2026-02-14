package com.example.trilhospt.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trilhospt.data.repository.ApiRepository
import com.example.trilhospt.data.remote.dto.AuthResponse
import com.example.trilhospt.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: ApiRepository) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<AuthResponse>>()
    val loginState: LiveData<Resource<AuthResponse>> = _loginState

    private val _registerState = MutableLiveData<Resource<AuthResponse>>()
    val registerState: LiveData<Resource<AuthResponse>> = _registerState

    fun login(username: String, password: String) {
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            try {
                 val response = repository.login(username, password)
                 if (response.isSuccessful && response.body() != null) {
                     _loginState.value = Resource.Success(response.body()!!)
                 } else {
                     _loginState.value = Resource.Error(response.message() ?: "Login failed")
                 }
            } catch (e: Exception) {
                _loginState.value = Resource.Error(e.message ?: "Login failed", e)
            }
        }
    }
    
    fun register(name: String, email: String, password: String) {
        _registerState.value = Resource.Loading
        viewModelScope.launch {
            try {
                 val response = repository.register(name, email, password)
                 if (response.isSuccessful && response.body() != null) {
                     _registerState.value = Resource.Success(response.body()!!)
                 } else {
                     _registerState.value = Resource.Error(response.message() ?: "Registration failed")
                 }
            } catch (e: Exception) {
                _registerState.value = Resource.Error(e.message ?: "Registration failed", e)
            }
        }
    }
}
