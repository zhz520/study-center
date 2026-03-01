package cn.zhzgo.study.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.DailyContentResponse
import cn.zhzgo.study.data.UserStats
import cn.zhzgo.study.data.Major
import cn.zhzgo.study.data.Subject
import cn.zhzgo.study.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.create(application)
    private val userPreferences = cn.zhzgo.study.data.UserPreferences(application)

    private val _dailyContent = MutableStateFlow<DailyContentResponse?>(null)
    val dailyContent = _dailyContent.asStateFlow()

    private val _countdowns = MutableStateFlow<List<cn.zhzgo.study.data.Countdown>>(emptyList())
    val countdowns = _countdowns.asStateFlow()

    private val _externalLinks = MutableStateFlow<List<cn.zhzgo.study.data.ExternalLink>>(emptyList())
    val externalLinks = _externalLinks.asStateFlow()

    private val _userStats = MutableStateFlow(
        UserStats(
            active_days = 0,
            total_questions = 0,
            accuracy = 0
        )
    )
    val userStats = _userStats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _majors = MutableStateFlow<List<Major>>(emptyList())
    val majors = _majors.asStateFlow()

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects = _subjects.asStateFlow()
    
    // User name state
    private val _userName = MutableStateFlow("Student")
    val userName = _userName.asStateFlow()

    private val _userRole = MutableStateFlow("")
    val userRole = _userRole.asStateFlow()

    init {
        loadData()
        viewModelScope.launch {
            userPreferences.userName.collect { name ->
                _userName.value = name ?: "同学"
            }
        }
        viewModelScope.launch {
            userPreferences.userRole.collect { role ->
                _userRole.value = role ?: ""
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            // Use supervisorScope to ensure one failure doesn't cancel other requests
            kotlinx.coroutines.supervisorScope {
                // Parallel fetch with error handling
                val dailyDeferred = async { 
                    try { apiService.getDailyContent() } catch (e: Exception) { null }
                }
                val homeDeferred = async { 
                    try { apiService.getHomeData() } catch (e: Exception) { null }
                }
                val statsDeferred = async { 
                    try { apiService.getUserStats() } catch (e: Exception) { null }
                }
                val majorsDeferred = async { 
                     try { apiService.getMajors() } catch (e: Exception) { emptyList() }
                }

                // Process results independently
                val daily = dailyDeferred.await()
                if (daily != null) {
                    _dailyContent.value = daily
                }
                
                val homeData = homeDeferred.await()
                if (homeData != null) {
                    _countdowns.value = homeData.countdowns
                    _externalLinks.value = homeData.externalLinks
                } else {
                    // Fallback or just empty
                    _countdowns.value = emptyList()
                    _externalLinks.value = emptyList()
                }
                
                val statsObj = statsDeferred.await()
                if (statsObj != null) {
                    _userStats.value = statsObj
                }

                val majorsList = majorsDeferred.await()
                _majors.value = majorsList
                if (majorsList.isNotEmpty()) {
                    loadSubjects(majorsList.first().id)
                }
            }
            _isLoading.value = false
        }
    }

    fun loadSubjects(majorId: Int) {
        viewModelScope.launch {
            try {
                val subjectsList = apiService.getSubjects(majorId)
                _subjects.value = subjectsList
            } catch (e: Exception) {
                e.printStackTrace()
                _subjects.value = emptyList()
            }
        }
    }

    fun refreshUserStats() {
        viewModelScope.launch {
            try {
                val statsObj = apiService.getUserStats()
                _userStats.value = statsObj
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearAuthToken()
        }
    }
}
