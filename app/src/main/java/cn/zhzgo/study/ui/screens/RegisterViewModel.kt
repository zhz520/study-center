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

    // Field-level validation hints
    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError = _usernameError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError = _confirmPasswordError.asStateFlow()

    // Success message for Snackbar
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
        _usernameError.value = null
        _error.value = null
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _passwordError.value = null
        _error.value = null
    }
    
    fun onConfirmPasswordChange(newPassword: String) {
        _confirmPassword.value = newPassword
        _confirmPasswordError.value = null
        _error.value = null
    }

    fun register() {
        // Field-level validation
        var valid = true
        
        if (_username.value.isBlank()) {
            _usernameError.value = "请输入用户名"
            valid = false
        } else if (_username.value.length < 2) {
            _usernameError.value = "用户名至少2个字符"
            valid = false
        } else if (_username.value.length > 20) {
            _usernameError.value = "用户名不能超过20个字符"
            valid = false
        }
        
        if (_password.value.isBlank()) {
            _passwordError.value = "请输入密码"
            valid = false
        } else {
            val regex = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$".toRegex()
            if (!regex.matches(_password.value)) {
                _passwordError.value = "密码至少8位，需包含字母和数字"
                valid = false
            }
        }
        
        if (_confirmPassword.value.isBlank()) {
            _confirmPasswordError.value = "请再次输入密码"
            valid = false
        } else if (_password.value != _confirmPassword.value) {
            _confirmPasswordError.value = "两次密码输入不一致"
            valid = false
        }
        
        if (!valid) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.register(LoginRequest(_username.value, _password.value))
                if (response != null && response.message != null && response.message.isNotEmpty()) {
                    _successMessage.value = "注册成功！请使用新账号登录"
                    _isRegistered.value = true
                } else {
                    _error.value = "注册失败，请稍后重试"
                }
            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                _error.value = when (code) {
                    409 -> "该用户名已被注册，请换一个"
                    400 -> "注册信息格式不正确"
                    429 -> "操作过于频繁，请稍后再试"
                    500 -> "服务器异常，请稍后重试"
                    else -> "注册失败 ($code)"
                }
            } catch (e: java.net.UnknownHostException) {
                _error.value = "无法连接到服务器，请检查网络"
            } catch (e: java.net.SocketTimeoutException) {
                _error.value = "连接超时，请检查网络后重试"
            } catch (e: Exception) {
                _error.value = "注册失败：${e.localizedMessage ?: "未知错误"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
