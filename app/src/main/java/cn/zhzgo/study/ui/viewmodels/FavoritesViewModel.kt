package cn.zhzgo.study.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.FavoriteQuestion
import cn.zhzgo.study.data.FavoriteAddRequest
import cn.zhzgo.study.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {

    private val _favorites = MutableStateFlow<List<FavoriteQuestion>>(emptyList())
    val favorites: StateFlow<List<FavoriteQuestion>> = _favorites.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    val filteredFavorites: StateFlow<List<FavoriteQuestion>> =
        combine(_favorites, _selectedType) { list, type ->
            if (type == null) list else list.filter { it.type == type }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stats: count per type
    val typeStats: StateFlow<Map<String, Int>> =
        _favorites.combine(_favorites) { list, _ ->
            list.groupBy { it.type ?: "unknown" }.mapValues { it.value.size }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun selectType(type: String?) {
        _selectedType.value = type
    }

    fun fetchFavorites(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val api = RetrofitClient.create(context)
                val list = api.getFavorites()
                _favorites.value = list
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to fetch favorites"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFavorite(context: Context, questionId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.create(context)
                api.addFavorite(FavoriteAddRequest(questionId))
                onSuccess()
                fetchFavorites(context) // Refresh
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add favorite")
            }
        }
    }

    fun removeFavorite(context: Context, favoriteId: Int) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.create(context)
                api.removeFavorite(favoriteId)
                // Remove from local list to avoid waiting for network refresh
                _favorites.value = _favorites.value.filter { it.id != favoriteId }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to remove favorite"
            }
        }
    }
}
