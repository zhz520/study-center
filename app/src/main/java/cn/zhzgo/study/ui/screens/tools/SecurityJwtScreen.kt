package cn.zhzgo.study.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Base64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityJwtScreen(onBack: () -> Unit) {
    var jwtToken by remember { mutableStateOf("") }
    var decodedHeader by remember { mutableStateOf("") }
    var decodedPayload by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("JWT 解析专家", fontWeight = FontWeight.Bold) },
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
            Text(
                "输入 JWT 令牌",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            OutlinedTextField(
                value = jwtToken,
                onValueChange = { 
                    jwtToken = it
                    decodeJwt(it, { h, p -> 
                        decodedHeader = h
                        decodedPayload = p
                        errorMsg = null
                    }, { err ->
                        errorMsg = err
                    })
                },
                placeholder = { Text("eyJhbGciOiJIUzI1NiI...") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )

            if (errorMsg != null) {
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            if (decodedHeader.isNotEmpty() || decodedPayload.isNotEmpty()) {
                JwtPartCard("HEADER: 算法与令牌类型", decodedHeader, Color(0xFFFB8C00))
                JwtPartCard("PAYLOAD: 数据负载", decodedPayload, Color(0xFF1E88E5))
                
                Text(
                    "注意：此工具仅进行 Base64 解等解析，不验证签名有效性。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun JwtPartCard(title: String, content: String, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(4.dp, 16.dp).background(accentColor))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    content,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
            }
        }
    }
}

private fun decodeJwt(token: String, onSuccess: (String, String) -> Unit, onError: (String) -> Unit) {
    if (token.isBlank()) return
    val parts = token.trim().split(".")
    if (parts.size < 2) {
        onError("无效的 JWT 格式（通常需要由 . 分隔的部分）")
        return
    }
    
    try {
        val decoder = Base64.getUrlDecoder()
        val header = String(decoder.decode(parts[0]))
        val payload = String(decoder.decode(parts[1]))
        onSuccess(header, payload)
    } catch (e: Exception) {
        onError("Base64 解码失败: ${e.message}")
    }
}
