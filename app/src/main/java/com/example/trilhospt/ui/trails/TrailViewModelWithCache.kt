package com.example.trilhospt.ui.trails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trilhospt.data.local.entity.TrailEntity
import com.example.trilhospt.data.remote.dto.TrailDto
import com.example.trilhospt.data.repository.ApiRepository
import com.example.trilhospt.data.repository.LocalRepository
import com.example.trilhospt.utils.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * EXEMPLO DE COMO INTEGRAR ROOM DATABASE NO VIEWMODEL
 * 
 * Este é um exemplo de como usar o LocalRepository junto com o ApiRepository
 * para implementar cache offline e sistema de favoritos.
 */
class TrailViewModelWithCache(
    private val apiRepository: ApiRepository,
    private val localRepository: LocalRepository
) : ViewModel() {
    
    private val _trails = MutableLiveData<Resource<List<TrailDto>>>()
    val trails: LiveData<Resource<List<TrailDto>>> = _trails
    
    private val _favoriteTrails = MutableLiveData<List<TrailEntity>>()
    val favoriteTrails: LiveData<List<TrailEntity>> = _favoriteTrails
    
    /**
     * Buscar trilhos com estratégia de cache-first
     * 1. Mostra dados em cache imediatamente
     * 2. Busca dados frescos da API
     * 3. Atualiza cache com novos dados
     */
    fun getTrailsWithCache() {
        _trails.value = Resource.Loading
        
        viewModelScope.launch {
            try {
                // 1. Primeiro, mostrar dados em cache (se existirem)
                localRepository.getAllCachedTrails().collect { cachedTrails ->
                    if (cachedTrails.isNotEmpty()) {
                        // Converter TrailEntity para TrailDto
                        val trailDtos = cachedTrails.map { entity ->
                            TrailDto(
                                id = entity.id,
                                name = entity.name,
                                description = entity.description,
                                difficulty = entity.difficulty,
                                distance = entity.distance,
                                duration = entity.duration,
                                firstPhotoUrl = entity.firstPhotoUrl
                            )
                        }
                        _trails.value = Resource.Success(trailDtos)
                    }
                }
                
                // 2. Buscar dados frescos da API
                val response = apiRepository.getTrails()
                
                if (response.isSuccessful && response.body() != null) {
                    val freshTrails = response.body()!!
                    
                    // 3. Atualizar cache
                    localRepository.cacheTrails(freshTrails)
                    
                    // 4. Atualizar UI com dados frescos
                    _trails.value = Resource.Success(freshTrails)
                } else {
                    // Se API falhar mas temos cache, manter cache
                    // Se não temos cache, mostrar erro
                    if (_trails.value !is Resource.Success) {
                        _trails.value = Resource.Error("Erro ao carregar trilhos")
                    }
                }
                
            } catch (e: Exception) {
                // Em caso de erro (ex: sem internet), usar cache
                if (_trails.value !is Resource.Success) {
                    _trails.value = Resource.Error(e.message ?: "Erro de conexão")
                }
            }
        }
    }
    
    /**
     * Buscar apenas trilhos favoritos
     */
    fun getFavoriteTrails() {
        viewModelScope.launch {
            localRepository.getFavoriteTrails().collect { favorites ->
                _favoriteTrails.value = favorites
            }
        }
    }
    
    /**
     * Marcar/desmarcar trilho como favorito
     */
    fun toggleFavorite(trailId: Int) {
        viewModelScope.launch {
            localRepository.toggleFavorite(trailId)
        }
    }
    
    /**
     * Limpar cache antigo (trilhos com mais de 7 dias)
     */
    fun clearOldCache() {
        viewModelScope.launch {
            localRepository.clearOldCache()
        }
    }
    
    /**
     * Limpar todo o cache
     */
    fun clearAllCache() {
        viewModelScope.launch {
            localRepository.clearAllCache()
        }
    }
}

/**
 * COMO USAR NO FRAGMENT:
 * 
 * val apiRepo = ApiRepository()
 * val localRepo = LocalRepository(requireContext())
 * val factory = ViewModelFactoryWithCache(apiRepo, localRepo)
 * viewModel = ViewModelProvider(this, factory)[TrailViewModelWithCache::class.java]
 * 
 * // Buscar trilhos com cache
 * viewModel.getTrailsWithCache()
 * 
 * // Observar trilhos
 * viewModel.trails.observe(viewLifecycleOwner) { resource ->
 *     when(resource) {
 *         is Resource.Loading -> showLoading()
 *         is Resource.Success -> showTrails(resource.data)
 *         is Resource.Error -> showError(resource.message)
 *     }
 * }
 * 
 * // Marcar como favorito
 * viewModel.toggleFavorite(trailId)
 * 
 * // Ver favoritos
 * viewModel.getFavoriteTrails()
 * viewModel.favoriteTrails.observe(viewLifecycleOwner) { favorites ->
 *     // Mostrar favoritos
 * }
 */
