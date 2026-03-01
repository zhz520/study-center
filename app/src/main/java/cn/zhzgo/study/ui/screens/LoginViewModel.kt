package cn.zhzgo.study.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.LoginRequest
import cn.zhzgo.study.data.UserPreferences
import cn.zhzgo.study.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.create(application)
    private val userPreferences = UserPreferences(application)

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _needBindQQ = MutableStateFlow<String?>(null)
    val needBindQQ = _needBindQQ.asStateFlow()

    fun resetBindQQ() {
        _needBindQQ.value = null
    }

    fun onUsernameChange(newValue: String) {
        _username.value = newValue
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    fun login() {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            try {
                val response = apiService.login(LoginRequest(_username.value, _password.value))
                userPreferences.saveAuthToken(response.token)
                userPreferences.saveRefreshToken(response.refreshToken)
                userPreferences.saveUserName(response.user.username)
                userPreferences.saveUserRole(response.user.role)
                response.user.avatar_icon?.let { userPreferences.setAvatarIcon(it) }
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _loginError.value = e.message ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun qqLogin(openId: String, accessToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            try {
                val response = apiService.qqLogin(cn.zhzgo.study.data.QQLoginRequest(openId, accessToken))
                if (response.isSuccessful) {
                    if (response.code() == 202) {
                        // Needs to bind
                        _needBindQQ.value = openId
                    } else {
                        val body = response.body()
                        if (body?.token != null) {
                            userPreferences.saveAuthToken(body.token)
                            userPreferences.saveRefreshToken(body.refreshToken ?: "")
                            userPreferences.saveUserName(body.user?.username ?: "User")
                            userPreferences.saveUserRole(body.user?.role ?: "user")
                            body.user?.avatar_icon?.let { userPreferences.setAvatarIcon(it) }
                            _isLoggedIn.value = true
                        } else {
                            _loginError.value = "QQ Login failed: Invalid response"
                        }
                    }
                } else {
                    _loginError.value = "QQ Login failed: ${response.message()}"
                }
            } catch (e: Exception) {
                _loginError.value = e.message ?: "QQ Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
