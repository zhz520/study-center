package cn.zhzgo.study.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.Article
import cn.zhzgo.study.data.ResourceCategory
import cn.zhzgo.study.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ResourcesViewModel : ViewModel() {

    private val _categories = MutableStateFlow<List<ResourceCategory>>(emptyList())
    val categories: StateFlow<List<ResourceCategory>> = _categories.asStateFlow()

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Computed: unique categories from articles
    val articleCategories: StateFlow<List<String>> =
        _articles.combine(_articles) { list, _ ->
            list.mapNotNull { it.category }.distinct()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered articles
    val filteredArticles: StateFlow<List<Article>> =
        combine(_articles, _selectedCategory, _searchQuery) { list, category, query ->
            var filtered = list
            if (category != null) {
                filtered = filtered.filter { it.category == category }
            }
            if (query.isNotBlank()) {
                val q = query.lowercase()
                filtered = filtered.filter {
                    it.title.lowercase().contains(q) ||
                    (it.summary ?: "").lowercase().contains(q)
                }
            }
            filtered
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun fetchData(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val api = RetrofitClient.create(context)
                val fetchedArticles = api.getArticles()
                _articles.value = fetchedArticles
                // Try to fetch categories too, but don't fail if endpoint missing
                try {
                    val fetchedCategories = api.getResourceCategories()
                    _categories.value = fetchedCategories
                } catch (_: Exception) {}
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to fetch resources"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
