package cn.zhzgo.study.ui.viewmodels

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.LeaderboardUser
import cn.zhzgo.study.data.UserPreferences
import android.content.Context
import android.app.Application
import cn.zhzgo.study.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import cn.zhzgo.study.data.Subject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)

    private val _currentUser = MutableStateFlow<String?>("")
    val currentUser: StateFlow<String?> = _currentUser.asStateFlow()

    private val _subjects = MutableStateFlow<List<Pair<String, String>>>(listOf("all" to "全部科目"))
    val subjects: StateFlow<List<Pair<String, String>>> = _subjects.asStateFlow()

    private val _selectedSubjectId = MutableStateFlow("all")
    val selectedSubjectId: StateFlow<String> = _selectedSubjectId.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.userName.collect { name ->
                _currentUser.value = name
            }
        }
    }

    private val _totalQuestionsRanking = MutableStateFlow<List<LeaderboardUser>>(emptyList())
    val totalQuestionsRanking: StateFlow<List<LeaderboardUser>> = _totalQuestionsRanking.asStateFlow()

    private val _accuracyRanking = MutableStateFlow<List<LeaderboardUser>>(emptyList())
    val accuracyRanking: StateFlow<List<LeaderboardUser>> = _accuracyRanking.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Init block removed, call explicitly from UI

    fun loadSubjects(context: Context) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.create(context)
                val fetchedSubjects = api.getSubjects(null)
                val newList = mutableListOf("all" to "全部科目")
                newList.addAll(fetchedSubjects.map { it.id.toString() to it.name })
                _subjects.value = newList
            } catch (e: Exception) {
                // Ignore error, fallback to 'all'
            }
        }
    }

    fun selectSubject(subjectId: String, context: Context) {
        if (_selectedSubjectId.value != subjectId) {
            _selectedSubjectId.value = subjectId
            loadRankings(context)
        }
    }

    fun loadRankings(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val api = RetrofitClient.create(context)
                val currentSubject = _selectedSubjectId.value
                val totalData = api.getLeaderboard("total", subjectId = currentSubject, limit = 100)
                _totalQuestionsRanking.value = totalData

                val accData = api.getLeaderboard("accuracy", subjectId = currentSubject, limit = 100)
                _accuracyRanking.value = accData
                
            } catch (e: Exception) {
                _error.value = "加载排行榜失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
