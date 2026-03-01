package cn.zhzgo.study.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isRegistered by viewModel.isRegistered.collectAsState()

    LaunchedEffect(isRegistered) {
        if (isRegistered) {
            onRegisterSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "创建账号",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "加入我们，开启学习之旅",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(48.dp))

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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AppTextField(
                    value = confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = "确认密码",
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
                    text = if (isLoading) "注册中..." else "立即注册",
                    onClick = viewModel::register,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onBack) {
                    Text(
                        text = "已有账号？去登录",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
