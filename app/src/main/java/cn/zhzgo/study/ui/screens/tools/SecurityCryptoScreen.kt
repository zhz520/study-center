package cn.zhzgo.study.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import androidx.compose.ui.graphics.Color
import java.util.Base64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityCryptoScreen(onBack: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var aesKey by remember { mutableStateOf("1234567890123456") }
    var aesIv by remember { mutableStateOf("1234567890123456") }
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val hashTabs = listOf("MD5", "SHA-1", "SHA-256")
    val cryptoTabs = listOf("哈希计算", "AES 加密")
    var selectedMainTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("安全与加解密", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                divider = {}
            ) {
                cryptoTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedMainTab == index,
                        onClick = { selectedMainTab = index },
                        text = { Text(title) }
                    )
                }
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("输入文本 / 密文") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            if (selectedMainTab == 0) {
                // Hash
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    hashTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                Button(
                    onClick = {
                        outputText = try {
                            val alg = hashTabs[selectedTab]
                            val md = MessageDigest.getInstance(alg)
                            val bytes = md.digest(inputText.toByteArray())
                            bytes.joinToString("") { "%02x".format(it) }
                        } catch (e: Exception) {
                            "计算失败: ${e.message}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("立即计算哈希")
                }
            } else {
                // AES
                OutlinedTextField(
                    value = aesKey,
                    onValueChange = { aesKey = it },
                    label = { Text("AES Key (16/24/32位)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = aesIv,
                    onValueChange = { aesIv = it },
                    label = { Text("AES IV (16位)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            outputText = try {
                                aesAction(inputText, aesKey, aesIv, Cipher.ENCRYPT_MODE)
                            } catch (e: Exception) {
                                "加密失败: ${e.message}"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("加密 (Base64)")
                    }
                    Button(
                        onClick = {
                            outputText = try {
                                aesAction(inputText, aesKey, aesIv, Cipher.DECRYPT_MODE)
                            } catch (e: Exception) {
                                "解密失败: ${e.message}"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("解密")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("执行结果", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { 
                            clipboardManager.setText(AnnotatedString(outputText))
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(20.dp))
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = outputText.ifEmpty { "等候处理..." },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun aesAction(data: String, key: String, iv: String, mode: Int): String {
    val keySpec = SecretKeySpec(key.toByteArray(), "AES")
    val ivSpec = IvParameterSpec(iv.toByteArray())
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(mode, keySpec, ivSpec)
    
    return if (mode == Cipher.ENCRYPT_MODE) {
        Base64.getEncoder().encodeToString(cipher.doFinal(data.toByteArray()))
    } else {
        String(cipher.doFinal(Base64.getDecoder().decode(data)))
    }
}

