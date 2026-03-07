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

    // Field-level validation hints
    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError = _usernameError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    fun resetBindQQ() {
        _needBindQQ.value = null
    }

    fun onUsernameChange(newValue: String) {
        _username.value = newValue
        _usernameError.value = null
        _loginError.value = null
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
        _passwordError.value = null
        _loginError.value = null
    }

    private fun validateInputs(): Boolean {
        var valid = true
        if (_username.value.isBlank()) {
            _usernameError.value = "请输入用户名"
            valid = false
        } else if (_username.value.length < 2) {
            _usernameError.value = "用户名至少2个字符"
            valid = false
        }
        if (_password.value.isBlank()) {
            _passwordError.value = "请输入密码"
            valid = false
        } else if (_password.value.length < 6) {
            _passwordError.value = "密码至少6位"
            valid = false
        }
        return valid
    }

    fun login() {
        if (!validateInputs()) return

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
            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                _loginError.value = when (code) {
                    401 -> "用户名或密码错误，请重新输入"
                    403 -> "账号已被禁用，请联系管理员"
                    404 -> "账号不存在，请先注册"
                    429 -> "登录尝试过于频繁，请稍后再试"
                    500 -> "服务器异常，请稍后重试"
                    else -> "登录失败 ($code)，请稍后重试"
                }
            } catch (e: java.net.UnknownHostException) {
                _loginError.value = "无法连接到服务器，请检查网络"
            } catch (e: java.net.SocketTimeoutException) {
                _loginError.value = "连接超时，请检查网络后重试"
            } catch (e: Exception) {
                _loginError.value = "登录失败：${e.localizedMessage ?: "未知错误"}"
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
                            _loginError.value = "QQ 登录失败，服务器返回异常"
                        }
                    }
                } else {
                    _loginError.value = when (response.code()) {
                        401 -> "QQ 授权已过期，请重新授权"
                        403 -> "该 QQ 账号已被禁用"
                        else -> "QQ 登录失败，请稍后重试"
                    }
                }
            } catch (e: java.net.UnknownHostException) {
                _loginError.value = "无法连接到服务器，请检查网络"
            } catch (e: Exception) {
                _loginError.value = "QQ 登录失败：${e.localizedMessage ?: "未知错误"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
