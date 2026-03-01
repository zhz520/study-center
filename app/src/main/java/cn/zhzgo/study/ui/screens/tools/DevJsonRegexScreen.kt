package cn.zhzgo.study.ui.screens.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevJsonRegexScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("JSON 处理", "正则测试")
    
    val clipboardManager = LocalClipboardManager.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("开发者助手", fontWeight = FontWeight.Bold) },
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
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
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
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 0) {
                    JsonToolSection(clipboardManager)
                } else {
                    RegexToolSection(clipboardManager)
                }
            }
        }
    }
}

@Composable
fun JsonToolSection(clipboardManager: androidx.compose.ui.platform.ClipboardManager) {
    var inputJson by remember { mutableStateOf("") }
    var outputJson by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    OutlinedTextField(
        value = inputJson,
        onValueChange = { inputJson = it },
        label = { Text("JSON 字符串") },
        modifier = Modifier.fillMaxWidth().height(180.dp),
        shape = RoundedCornerShape(12.dp),
        placeholder = { Text("{\"a\":1, \"b\":\"hello\"}") },
        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {
                try {
                    val trimmed = inputJson.trim()
                    if (trimmed.startsWith("[")) {
                        outputJson = JSONArray(trimmed).toString(4)
                    } else {
                        outputJson = JSONObject(trimmed).toString(4)
                    }
                    errorMsg = null
                } catch (e: Exception) {
                    errorMsg = "格式化失败: ${e.message}"
                }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("美化 JSON")
        }
        Button(
            onClick = {
                outputJson = inputJson.replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                errorMsg = null
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("去转义")
        }
    }

    if (errorMsg != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }

    if (outputJson.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("结果输出", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(outputJson)) }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(20.dp))
                    }
                }
                Text(outputJson, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
            }
        }
    }
}

@Composable
fun RegexToolSection(clipboardManager: androidx.compose.ui.platform.ClipboardManager) {
    var regexPattern by remember { mutableStateOf("") }
    var testText by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<String>("") }
    
    val commonRegexList = listOf(
        "手机号" to "^1[3-9]\\d{9}$",
        "邮箱" to "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        "IP" to "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$",
        "中文" to "[\\u4e00-\\u9fa5]+"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("常用正则库", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            commonRegexList.forEach { (name, pattern) ->
                SuggestionChip(
                    onClick = { regexPattern = pattern },
                    label = { Text(name, style = MaterialTheme.typography.bodySmall) }
                )
            }
        }

        OutlinedTextField(
            value = regexPattern,
            onValueChange = { regexPattern = it },
            label = { Text("正则表达式模式") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
        )

        OutlinedTextField(
            value = testText,
            onValueChange = { testText = it },
            label = { Text("验证文本") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                try {
                    val regex = Regex(regexPattern)
                    val matches = regex.findAll(testText)
                    val matchCount = matches.count()
                    if (matchCount > 0) {
                        results = "找到 $matchCount 个匹配项:\n" + 
                                matches.joinToString("\n") { 
                                    "• \"${it.value}\" (索引: ${it.range})"
                                }
                    } else {
                        results = "未找到匹配项。"
                    }
                } catch (e: Exception) {
                    results = "正则语法错误: ${e.message}"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("开始匹配验证")
        }

        if (results.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(results, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
