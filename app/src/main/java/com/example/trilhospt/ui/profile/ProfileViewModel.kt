package com.example.trilhospt.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trilhospt.data.remote.dto.UserDto
import com.example.trilhospt.data.repository.ApiRepository
import com.example.trilhospt.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class ProfileViewModel(private val repository: ApiRepository) : ViewModel() {

    private val _profile = MutableLiveData<Resource<UserDto>>()
    val profile: LiveData<Resource<UserDto>> = _profile

    private val _badges = MutableLiveData<Resource<List<com.example.trilhospt.data.remote.dto.BadgeDto>>>()
    val badges: LiveData<Resource<List<com.example.trilhospt.data.remote.dto.BadgeDto>>> = _badges

    private val _photos = MutableLiveData<Resource<List<com.example.trilhospt.data.remote.dto.PhotoDto>>>()
    val photos: LiveData<Resource<List<com.example.trilhospt.data.remote.dto.PhotoDto>>> = _photos

    private val _uploadStatus = MutableLiveData<Resource<com.example.trilhospt.data.remote.dto.PhotoDto>?>()
    val uploadStatus: LiveData<Resource<com.example.trilhospt.data.remote.dto.PhotoDto>?> = _uploadStatus

    private val _createdTrails = MutableLiveData<Resource<List<com.example.trilhospt.data.remote.dto.TrailDto>>>()
    val createdTrails: LiveData<Resource<List<com.example.trilhospt.data.remote.dto.TrailDto>>> = _createdTrails

    private val _completedTrails = MutableLiveData<Resource<List<com.example.trilhospt.data.remote.dto.TrailDto>>>()
    val completedTrails: LiveData<Resource<List<com.example.trilhospt.data.remote.dto.TrailDto>>> = _completedTrails

    fun getProfile(userId: Int? = null) {
        _profile.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = if (userId != null) {
                    repository.getUserById(userId)
                } else {
                    repository.getProfile()
                }
                
                if (response.isSuccessful && response.body() != null) {
                    _profile.value = Resource.Success(response.body()!!)
                    // Fetch badges and photos after profile success
                    val id = userId ?: response.body()!!.id
                    getBadges(id)
                    getUserPhotos(id)
                } else {
                    val error = response.errorBody()?.string() ?: response.message()
                    _profile.value = Resource.Error(error)
                }
            } catch (e: Exception) {
                _profile.value = Resource.Error(e.message ?: "Failed to fetch profile")
            }
        }
    }

    fun getBadges(userId: Int? = null) {
        _badges.value = Resource.Loading
        viewModelScope.launch {
            try {
                // If userId is null, we assume we want MY badges, but the API endpoint for badges
                // might differ. Let's rely on the strategy: if userId passed, fetching for specific user.
                // If not passed, fetch my badges.
                val response = if (userId != null) {
                    repository.getUserBadges(userId)
                } else {
                    repository.getMyBadges()
                }
                
                if (response.isSuccessful && response.body() != null) {
                    _badges.value = Resource.Success(response.body()!!)
                } else {
                    val error = response.errorBody()?.string() ?: response.message()
                    _badges.value = Resource.Error(error)
                }
            } catch (e: Exception) {
                _badges.value = Resource.Error(e.message ?: "Failed to fetch badges")
            }
        }
    }

    fun getUserPhotos(userId: Int) {
        _photos.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = repository.getUserPhotos(userId)
                if (response.isSuccessful && response.body() != null) {
                    _photos.value = Resource.Success(response.body()!!)
                } else {
                    _photos.value = Resource.Error("Error fetching photos")
                }
            } catch (e: Exception) {
                _photos.value = Resource.Error(e.message ?: "Failed to fetch photos")
            }
        }
    }

    fun uploadPhoto(imagePart: okhttp3.MultipartBody.Part, trailId: Int, lat: Double, lng: Double, description: String?) {
        _uploadStatus.value = Resource.Loading
        viewModelScope.launch {
            try {
                val trailIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), trailId.toString())
                val latBody = RequestBody.create("text/plain".toMediaTypeOrNull(), lat.toString())
                val lngBody = RequestBody.create("text/plain".toMediaTypeOrNull(), lng.toString())
                val descBody = description?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }

                val response = repository.uploadPhoto(imagePart, trailIdBody, latBody, lngBody, descBody)
                
                if (response.isSuccessful && response.body() != null) {
                    _uploadStatus.value = Resource.Success(response.body()!!)
                    // Refresh current photos if updating my own profile
                    // But we need the userId. We can get it from _profile.value
                    (_profile.value as? Resource.Success)?.data?.id?.let { getUserPhotos(it) }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Upload failed: ${response.code()}"
                    _uploadStatus.value = Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uploadStatus.value = Resource.Error("Upload exception: ${e.message}")
            }
        }
    }

    fun clearUploadStatus() {
        _uploadStatus.value = null
    }

    fun getCreatedTrails(userId: Int? = null) {
        _createdTrails.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = if (userId != null) {
                    repository.getTrails(userId = userId)
                } else {
                    repository.getMyTrails()
                }
                
                if (response.isSuccessful && response.body() != null) {
                    _createdTrails.value = Resource.Success(response.body()!!)
                } else {
                    _createdTrails.value = Resource.Error("Erro ao carregar trilhos criados")
                }
            } catch (e: Exception) {
                _createdTrails.value = Resource.Error(e.message ?: "Erro ao carregar trilhos criados")
            }
        }
    }

    fun getCompletedTrails(userId: Int? = null) {
        _completedTrails.value = Resource.Loading
        viewModelScope.launch {
            try {
                // If fetching for another user, pass userId. If null, use my trails (which repository handles)
                // However, API endpoint supports filtering by user_id for any user or default to me.
                val response = repository.getCompletedTrails(userId)
                
                if (response.isSuccessful && response.body() != null) {
                    _completedTrails.value = Resource.Success(response.body()!!)
                } else {
                    _completedTrails.value = Resource.Error("Erro ao carregar trilhos concluídos")
                }
            } catch (e: Exception) {
                _completedTrails.value = Resource.Error(e.message ?: "Erro ao carregar trilhos concluídos")
            }
        }
    }
}
