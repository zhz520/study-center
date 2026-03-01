package cn.zhzgo.study.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorScreen(onBack: () -> Unit) {
    var length by remember { mutableStateOf(16f) }
    var useUppercase by remember { mutableStateOf(true) }
    var useLowercase by remember { mutableStateOf(true) }
    var useNumbers by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }
    var generatedPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Generate initial password
    LaunchedEffect(Unit) {
        generatedPassword = generatePassword(length.toInt(), useUppercase, useLowercase, useNumbers, useSymbols)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("密码生成", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Display Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable {
                        if (generatedPassword.isNotEmpty()) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Copied Password", generatedPassword)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (generatedPassword.isEmpty()) "请选择配置后生成" else generatedPassword,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("点击复制", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Controls
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Length Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("密码长度", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text("${length.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    }
                    Slider(
                        value = length,
                        onValueChange = { 
                            length = it 
                            generatedPassword = generatePassword(length.toInt(), useUppercase, useLowercase, useNumbers, useSymbols)
                        },
                        valueRange = 6f..32f,
                        steps = 25
                    )
                }

                // Options
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        ToggleOptionRow("大写字母 (A-Z)", useUppercase) { 
                            useUppercase = it
                            generatedPassword = generatePassword(length.toInt(), useUppercase, useLowercase, useNumbers, useSymbols)
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ToggleOptionRow("小写字母 (a-z)", useLowercase) { 
                            useLowercase = it
                            generatedPassword = generatePassword(length.toInt(), useUppercase, useLowercase, useNumbers, useSymbols)
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ToggleOptionRow("数字 (0-9)", useNumbers) { 
                            useNumbers = it
                            generatedPassword = generatePassword(length.toInt(), useUppercase, useLowercase, useNumbers, useSymbols)
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ToggleOptionRow("特殊符号 (!@#)", useSymbols) { 
                            useSymbols = it
                            generatedPassword = generatePassword(length.toInt(), useUppercase, useLowercase, useNumbers, useSymbols)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    generatedPassword = generatePassword(length.toInt(), useUppercase, useLowercase, useNumbers, useSymbols)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("重新生成", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ToggleOptionRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

private fun generatePassword(
    length: Int,
    useUpper: Boolean,
    useLower: Boolean,
    useNumbers: Boolean,
    useSymbols: Boolean
): String {
    val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val lower = "abcdefghijklmnopqrstuvwxyz"
    val numbers = "0123456789"
    val symbols = "!@#$%^&*()_+~`-=[]{}|;:,.<>?"

    var charPool = ""
    if (useUpper) charPool += upper
    if (useLower) charPool += lower
    if (useNumbers) charPool += numbers
    if (useSymbols) charPool += symbols

    if (charPool.isEmpty()) return ""

    return (1..length)
        .map { Random.nextInt(0, charPool.length) }
        .map(charPool::get)
        .joinToString("")
}
