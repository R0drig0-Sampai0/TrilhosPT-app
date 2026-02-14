package com.example.trilhospt.ui.trails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trilhospt.data.repository.ApiRepository
import com.example.trilhospt.data.remote.dto.TrailDto
import com.example.trilhospt.data.remote.dto.ReviewDto
import com.example.trilhospt.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class TrailViewModel(private val repository: ApiRepository) : ViewModel() {

    private val _trails = MutableLiveData<Resource<List<TrailDto>>>()
    val trails: LiveData<Resource<List<TrailDto>>> = _trails

    private val _trailDetail = MutableLiveData<Resource<TrailDto>>()
    val trailDetail: LiveData<Resource<TrailDto>> = _trailDetail

    private val _reviewStatus = MutableLiveData<Resource<ReviewDto>>()
    val reviewStatus: LiveData<Resource<ReviewDto>> = _reviewStatus

    private val _createTrailStatus = MutableLiveData<Resource<TrailDto>>()
    val createTrailStatus: LiveData<Resource<TrailDto>> = _createTrailStatus

    private val _deleteStatus = MutableLiveData<Resource<Unit>>()
    val deleteStatus: LiveData<Resource<Unit>> = _deleteStatus

    private val _updateTrailStatus = MutableLiveData<Resource<TrailDto>>()
    val updateTrailStatus: LiveData<Resource<TrailDto>> = _updateTrailStatus

    private val _uploadPhotoStatus = MutableLiveData<Resource<com.example.trilhospt.data.remote.dto.PhotoDto>>()
    val uploadPhotoStatus: LiveData<Resource<com.example.trilhospt.data.remote.dto.PhotoDto>> = _uploadPhotoStatus

    private val _currentUser = MutableLiveData<Resource<com.example.trilhospt.data.remote.dto.UserDto>>()
    val currentUser: LiveData<Resource<com.example.trilhospt.data.remote.dto.UserDto>> = _currentUser



    fun getCurrentUser() {
        viewModelScope.launch {
            try {
                val response = repository.getProfile()
                if (response.isSuccessful && response.body() != null) {
                    _currentUser.value = Resource.Success(response.body()!!)
                }
            } catch (e: Exception) {
                // Ignore error for current user check
            }
        }
    }

    fun createTrail(trail: TrailDto) {
        _createTrailStatus.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = repository.createTrail(trail)
                if (response.isSuccessful && response.body() != null) {
                    _createTrailStatus.value = Resource.Success(response.body()!!)
                } else {
                    val error = response.errorBody()?.string() ?: response.message()
                    _createTrailStatus.value = Resource.Error(error)
                }
            } catch (e: Exception) {
                _createTrailStatus.value = Resource.Error(e.message ?: "Failed to create trail")
            }
        }
    }

    fun getTrails(
        difficulty: String? = null,
        minDistance: Double? = null,
        maxDistance: Double? = null,
        search: String? = null
    ) {
        _trails.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = repository.getTrails(difficulty, minDistance, maxDistance, search)
                android.util.Log.d("TrailViewModel", "getTrails response code: ${response.code()}")
                if (response.isSuccessful && response.body() != null) {
                    val trails = response.body()!!
                    android.util.Log.d("TrailViewModel", "Trails found: ${trails.size}")
                    _trails.value = Resource.Success(trails)
                } else {
                    val error = response.errorBody()?.string() ?: response.message()
                    android.util.Log.e("TrailViewModel", "Error fetching trails: $error")
                    _trails.value = Resource.Error("Error ${response.code()}: $error")
                }
            } catch (e: Exception) {
                _trails.value = Resource.Error(e.message ?: "Failed to fetch trails", e)
            }
        }
    }

    fun getTrailDetail(id: Int) {
        _trailDetail.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = repository.getTrailById(id)
                if (response.isSuccessful && response.body() != null) {
                    _trailDetail.value = Resource.Success(response.body()!!)
                } else {
                    _trailDetail.value = Resource.Error(response.message() ?: "Failed to fetch trail details")
                }
            } catch (e: Exception) {
                _trailDetail.value = Resource.Error(e.message ?: "Failed to fetch trail details", e)
            }
        }
    }

    fun createReview(review: ReviewDto) {
        _reviewStatus.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = repository.createReview(review)
                if (response.isSuccessful && response.body() != null) {
                    _reviewStatus.value = Resource.Success(response.body()!!)
                    // Refresh trail detail to show new review
                    getTrailDetail(review.trail)
                } else {
                    val error = response.errorBody()?.string() ?: response.message()
                    _reviewStatus.value = Resource.Error(error)
                }
            } catch (e: IllegalStateException) {
                _reviewStatus.value = Resource.Error(e.message ?: "Must be logged in to review")
            } catch (e: Exception) {
                _reviewStatus.value = Resource.Error(e.message ?: "Failed to post review")
            }
        }
    }
    
    private val _completionStatus = MutableLiveData<Resource<Boolean>>()
    val completionStatus: LiveData<Resource<Boolean>> = _completionStatus

    fun markAsCompleted(trailId: Int) {
        _completionStatus.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = repository.markTrailCompleted(trailId)
                if (response.isSuccessful) {
                    _completionStatus.value = Resource.Success(true)
                    getTrailDetail(trailId)
                } else {
                    val error = response.errorBody()?.string() ?: response.message()
                    _completionStatus.value = Resource.Error(error)
                }
            } catch (e: Exception) {
                _completionStatus.value = Resource.Error(e.message ?: "Error marking completed")
            }
        }
    }

    fun unmarkAsCompleted(completionId: Int, trailId: Int) {
        _completionStatus.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = repository.deleteCompletedTrail(completionId)
                if (response.isSuccessful) {
                    _completionStatus.value = Resource.Success(false)
                    getTrailDetail(trailId)
                } else {
                    val error = response.errorBody()?.string() ?: response.message()
                    _completionStatus.value = Resource.Error(error)
                }
            } catch (e: Exception) {
                _completionStatus.value = Resource.Error(e.message ?: "Error unmarking completed")
            }
        }
    }

    fun uploadPhoto(imagePart: okhttp3.MultipartBody.Part, trailId: Int, lat: Double, lng: Double, description: String?) {
        _uploadPhotoStatus.value = Resource.Loading
        viewModelScope.launch {
            try {
                val trailIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), trailId.toString())
                val latBody = RequestBody.create("text/plain".toMediaTypeOrNull(), lat.toString())
                val lngBody = RequestBody.create("text/plain".toMediaTypeOrNull(), lng.toString())
                val descBody = description?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }

                val response = repository.uploadPhoto(imagePart, trailIdBody, latBody, lngBody, descBody)
                
                if (response.isSuccessful && response.body() != null) {
                    _uploadPhotoStatus.value = Resource.Success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Upload failed: ${response.code()}"
                    _uploadPhotoStatus.value = Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uploadPhotoStatus.value = Resource.Error("Upload exception: ${e.message}")
            }
        }
    }
    
    /**
     * Atualizar trilho (apenas para admins/criadores)
     */
    fun updateTrail(trailId: Int, trail: TrailDto) {
        _updateTrailStatus.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = repository.updateTrail(trailId, trail)
                
                if (response.isSuccessful && response.body() != null) {
                    _updateTrailStatus.value = Resource.Success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Erro ao atualizar trilho"
                    _updateTrailStatus.value = Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                _updateTrailStatus.value = Resource.Error(e.message ?: "Erro ao atualizar trilho")
            }
        }
    }
    
    /**
     * Eliminar trilho (apenas para admins/criadores)
     */
    fun deleteTrail(trailId: Int) {
        _deleteStatus.value = Resource.Loading
        viewModelScope.launch {
            try {
                val response = repository.deleteTrail(trailId)
                
                if (response.isSuccessful) {
                    _deleteStatus.value = Resource.Success(Unit)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Erro ao eliminar trilho"
                    _deleteStatus.value = Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                _deleteStatus.value = Resource.Error(e.message ?: "Erro ao eliminar trilho")
            }
        }
    }
}
