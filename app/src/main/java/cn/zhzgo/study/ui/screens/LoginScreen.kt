package cn.zhzgo.study.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.zhzgo.study.ui.components.AppTextField
import cn.zhzgo.study.ui.components.PrimaryButton

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToBindQQ: (String) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    val qqHelper = remember { cn.zhzgo.study.utils.QQLoginHelper(context) }
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.loginError.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    val needBindQQ by viewModel.needBindQQ.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(needBindQQ) {
        if (needBindQQ != null) {
            onNavigateToBindQQ(needBindQQ!!)
            viewModel.resetBindQQ()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header
                Text(
                    text = "欢迎回来",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "登录您的账号",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(48.dp))

                // Form
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
                    text = if (isLoading) "登录中..." else "登录",
                    onClick = viewModel::login,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "没有账号？去注册",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = " 或 ",
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        activity?.let {
                            qqHelper.login(it, object : cn.zhzgo.study.utils.QQLoginHelper.QQLoginListener {
                                override fun onSuccess(openId: String, accessToken: String, nickname: String?, avatarUrl: String?) {
                                    viewModel.qqLogin(openId, accessToken)
                                }

                                override fun onError(code: Int, message: String, detail: String?) {
                                    // Handle UI error display if necessary via ViewModel
                                }

                                override fun onCancel() {}
                            })
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("使用 QQ 快捷登录", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
