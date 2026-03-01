package cn.zhzgo.study.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.AppDatabase
import cn.zhzgo.study.data.ToolEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ToolsViewModel(application: Application) : AndroidViewModel(application) {

    private val toolDao = AppDatabase.getDatabase(application).toolDao()

    val allTools: StateFlow<List<ToolEntity>> = toolDao.getAllTools()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topTools: StateFlow<List<ToolEntity>> = toolDao.getTopTools(4)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getToolsByIds(toolIds: List<String>): StateFlow<List<ToolEntity>> {
        return toolDao.getToolsByIds(toolIds)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun incrementUsage(toolId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            toolDao.incrementUsage(toolId)
        }
    }
    
    fun toggleFavorite(toolId: String, isFavorite: Boolean) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            toolDao.setFavorite(toolId, !isFavorite)
        }
    }
}
