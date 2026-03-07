package cn.zhzgo.study.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import cn.zhzgo.study.utils.QQLoginHelper
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.CrueltyFree
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import coil.compose.AsyncImage
import coil.request.ImageRequest

val AVAILABLE_AVATARS = listOf(
    "Felix", "Aneka", "Jack", "Luna", "Bella", "Max", "Oliver", "Chloe", "Leo", "Milo", "Charlie", "Simba"
).map { "https://api.dicebear.com/9.x/fun-emoji/png?seed=$it&size=120" }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val qqHelper = remember { QQLoginHelper(context) }
    
    val userName by viewModel.userName.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val avatarIcon by viewModel.avatarIcon.collectAsState()
    val nickname by viewModel.nickname.collectAsState()
    val qqOpenId by viewModel.qqOpenId.collectAsState()
    val qqAvatar by viewModel.qqAvatar.collectAsState()
    
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text("选择头像", fontWeight = FontWeight.Bold) },
            text = {
                val combinedAvatars = remember(qqAvatar) {
                    if (qqAvatar != null) listOf(qqAvatar!!) + AVAILABLE_AVATARS else AVAILABLE_AVATARS
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(combinedAvatars) { url ->
                        val isSelected = url == avatarIcon
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                                .clickable {
                                    viewModel.setAvatarIcon(url)
                                    showAvatarDialog = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(url)
                                    .crossfade(true)
                                    .placeholder(cn.zhzgo.study.R.drawable.ic_launcher_foreground)
                                    .error(cn.zhzgo.study.R.drawable.ic_launcher_foreground)
                                    .build(),
                                contentDescription = "Avatar Option",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarDialog = false }) { Text("关闭", color = MaterialTheme.colorScheme.onSurface) }
            }
        )
    }

    if (showChangePasswordDialog) {
        var oldPw by remember { mutableStateOf("") }
        var newPw by remember { mutableStateOf("") }
        var confirmPw by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("修改密码", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = oldPw,
                        onValueChange = { oldPw = it },
                        label = { Text("原密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPw,
                        onValueChange = { newPw = it },
                        label = { Text("新密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPw,
                        onValueChange = { confirmPw = it },
                        label = { Text("确认新密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPw != confirmPw) {
                        Toast.makeText(context, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    viewModel.changePassword(
                        oldPw, newPw,
                        onSuccess = {
                            Toast.makeText(context, "密码修改成功，请重新登录", Toast.LENGTH_SHORT).show()
                            showChangePasswordDialog = false
                            viewModel.logout { /* handled by main listener usually */ }
                        },
                        onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                    )
                }) {
                    Text("确认修改", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) { Text("取消", color = MaterialTheme.colorScheme.onSurface) }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("账户信息", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Avatar Card - Original Simple Style with Refined Avatar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .clickable { showAvatarDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentIcon = if (avatarIcon.startsWith("http")) avatarIcon else "https://api.dicebear.com/9.x/fun-emoji/png?seed=Felix&size=120"
                    
                    // Avatar with Subtle Border and Integration
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentIcon)
                                .crossfade(true)
                                .placeholder(cn.zhzgo.study.R.drawable.ic_launcher_foreground)
                                .error(cn.zhzgo.study.R.drawable.ic_launcher_foreground)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = userName.ifEmpty { "未登录" }, 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val roleLabel = when (userRole) {
                            "admin" -> "管理员"
                            "demo" -> "演示账号"
                            else -> "学生"
                        }
                        Text(
                            text = roleLabel, 
                            fontSize = 13.sp, 
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            item {
                SettingsSection(title = "安全操作") {
                    SettingsRow(
                        icon = Icons.Default.Lock,
                        label = "修改密码",
                        onClick = { showChangePasswordDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 52.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    if (!nickname.isNullOrEmpty()) {
                        SettingsRow(
                            icon = Icons.Default.Chat,
                            label = "QQ 昵称",
                            trailingContent = { Text(nickname!!, color = Color.Gray, fontSize = 14.sp) },
                            onClick = {}
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 52.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }
                    
                    val isBound = !qqOpenId.isNullOrEmpty()
                    SettingsRow(
                        icon = if (isBound) Icons.Default.LinkOff else Icons.Default.Link,
                        label = if (isBound) "解绑 QQ" else "绑定 QQ",
                        onClick = {
                            if (isBound) {
                                viewModel.unbindQQ(
                                    onSuccess = { Toast.makeText(context, "QQ 已解绑", Toast.LENGTH_SHORT).show() },
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                )
                            } else {
                                activity?.let { ctx ->
                                    qqHelper.login(ctx, object : cn.zhzgo.study.utils.QQLoginHelper.QQLoginListener {
                                    override fun onSuccess(openId: String, accessToken: String, nickname: String?, avatarUrl: String?) {
                                        viewModel.bindQQ(openId, nickname, avatarUrl,
                                            onSuccess = { Toast.makeText(context, "QQ 绑定成功", Toast.LENGTH_SHORT).show() },
                                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                        )
                                    }
                                        override fun onError(code: Int, message: String, detail: String?) {
                                            Toast.makeText(context, "QQ 授权失败: $message", Toast.LENGTH_SHORT).show()
                                        }
                                        override fun onCancel() {}
                                    })
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
