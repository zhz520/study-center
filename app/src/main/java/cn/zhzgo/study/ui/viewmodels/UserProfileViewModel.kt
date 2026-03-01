package cn.zhzgo.study.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.UserProfileResponse
import cn.zhzgo.study.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {
    private val _profile = MutableStateFlow<UserProfileResponse?>(null)
    val profile: StateFlow<UserProfileResponse?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchProfile(context: Context, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val api = RetrofitClient.create(context)
                val response = api.getUserProfile(userId)
                _profile.value = response
            } catch (e: Exception) {
                _error.value = e.message ?: "获取用户信息失败"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
