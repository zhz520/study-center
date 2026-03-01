package cn.zhzgo.study.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Base64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityCodecScreen(onBack: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Base64", "URL", "Unicode", "摩斯")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编码转换", fontWeight = FontWeight.Bold) },
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
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(bottom = 8.dp),
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("原始文本") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        outputText = try {
                            when (selectedTab) {
                                0 -> Base64.getEncoder().encodeToString(inputText.toByteArray())
                                1 -> URLEncoder.encode(inputText, "UTF-8")
                                2 -> toUnicode(inputText)
                                3 -> MorseCodec.encode(inputText)
                                else -> ""
                            }
                        } catch (e: Exception) {
                            "编码错误: ${e.message}"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("编码")
                }
                Button(
                    onClick = {
                        outputText = try {
                            when (selectedTab) {
                                0 -> String(Base64.getDecoder().decode(inputText))
                                1 -> URLDecoder.decode(inputText, "UTF-8")
                                2 -> fromUnicode(inputText)
                                3 -> MorseCodec.decode(inputText)
                                else -> ""
                            }
                        } catch (e: Exception) {
                            "解码错误: ${e.message}"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("解码")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("结果输出", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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

private fun toUnicode(text: String): String {
    return text.map { "\\u%04x".format(it.toInt()) }.joinToString("")
}

private fun fromUnicode(text: String): String {
    val regex = Regex("\\\\u([0-9a-fA-F]{4})")
    return regex.replace(text) {
        it.groupValues[1].toInt(16).toChar().toString()
    }
}

object MorseCodec {
    private val map = mapOf(
        'a' to ".-", 'b' to "-...", 'c' to "-.-.", 'd' to "-..", 'e' to ".", 'f' to "..-.", 'g' to "--.", 'h' to "....",
        'i' to "..", 'j' to ".---", 'k' to "-.-", 'l' to ".-..", 'm' to "--", 'n' to "-.", 'o' to "---", 'p' to ".--.",
        'q' to "--.-", 'r' to ".-.", 's' to "...", 't' to "-", 'u' to "..-", 'v' to "...-", 'w' to ".--", 'x' to "-..-",
        'y' to "-.--", 'z' to "--..", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-", '5' to ".....",
        '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.", '0' to "-----", ' ' to "/"
    )
    private val revMap = map.entries.associateBy({ it.value }, { it.key })

    fun encode(text: String): String {
        return text.lowercase().map { map[it] ?: "?" }.joinToString(" ")
    }

    fun decode(text: String): String {
        return text.split(" ").map { revMap[it] ?: '?' }.joinToString("")
    }
}
