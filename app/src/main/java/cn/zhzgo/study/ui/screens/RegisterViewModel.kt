package cn.zhzgo.study.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.LoginRequest
import cn.zhzgo.study.network.RetrofitClient
import cn.zhzgo.study.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.create(application)
    private val userPreferences = UserPreferences(application)

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()
    
    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    
    private val _isRegistered = MutableStateFlow(false)
    val isRegistered = _isRegistered.asStateFlow()

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }
    
    fun onConfirmPasswordChange(newPassword: String) {
        _confirmPassword.value = newPassword
    }

    fun register() {
        if (_username.value.isBlank() || _password.value.isBlank()) {
            _error.value = "用户名和密码不能为空"
            return
        }
        
        if (_password.value != _confirmPassword.value) {
            _error.value = "两次密码输入不一致"
            return
        }
        
        val regex = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}\$".toRegex()
        if (!regex.matches(_password.value)) {
            _error.value = "密码长度至少8位，且必须包含字母和数字"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.register(LoginRequest(_username.value, _password.value))
                if (response != null && response.message != null && response.message.isNotEmpty()) {
                    // Registration successful, the backend does not return a token.
                    // Guide the user to Login manually.
                    _isRegistered.value = true
                } else {
                    _error.value = "注册失败，请稍后重试"
                }
            } catch (e: Exception) {
                _error.value = "注册失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
