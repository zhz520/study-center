package cn.zhzgo.study.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.zhzgo.study.data.QQBindRequest
import cn.zhzgo.study.data.UserPreferences
import cn.zhzgo.study.network.RetrofitClient
import cn.zhzgo.study.ui.components.AppTextField
import cn.zhzgo.study.ui.components.PrimaryButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BindQQViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.create(application)
    private val userPreferences = UserPreferences(application)

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun onUsernameChange(newValue: String) { _username.value = newValue }
    fun onPasswordChange(newValue: String) { _password.value = newValue }

    fun bindExisting(openId: String, onSuccess: () -> Unit) {
        if (_username.value.isBlank() || _password.value.isBlank()) {
            _error.value = "用户名和密码不能为空"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.bindQq(QQBindRequest(openId, _username.value, _password.value))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.token != null) {
                        userPreferences.saveAuthToken(body.token)
                        userPreferences.saveRefreshToken(body.refreshToken ?: "")
                        userPreferences.saveUserName(body.user?.username ?: "User")
                        userPreferences.saveUserRole(body.user?.role ?: "user")
                        onSuccess()
                    } else {
                        _error.value = "绑定失败: 无效的响应"
                    }
                } else {
                    _error.value = "绑定失败: 用户名或密码错误"
                }
            } catch (e: Exception) {
                _error.value = "绑定失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BindQQScreen(
    openId: String,
    onBindSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: BindQQViewModel = viewModel()
) {
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("绑定已有账号") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "QQ 授权成功",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请绑定您的已有账号以完成联合登录",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            AppTextField(
                value = username,
                onValueChange = viewModel::onUsernameChange,
                label = "用户名",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                label = "密码",
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = Icons.Default.Lock
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error!!, 
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = if (isLoading) "绑定中..." else "绑定并登录",
                onClick = { viewModel.bindExisting(openId, onBindSuccess) },
                enabled = !isLoading
            )
        }
    }
}
