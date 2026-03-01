package cn.zhzgo.study.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevHttpTestScreen(onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var url by remember { mutableStateOf("https://httpbin.org/get") }
    var method by remember { mutableStateOf("GET") }
    val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")
    var expandedMethod by remember { mutableStateOf(false) }

    val headers = remember { mutableStateListOf(Pair("Content-Type", "application/json")) }
    
    var bodyFormat by remember { mutableStateOf("JSON") }
    val bodyFormats = listOf("JSON", "Text", "Form-Data")
    var requestBody by remember { mutableStateOf("") }

    var responseStatus by remember { mutableStateOf("") }
    var responseHeaders by remember { mutableStateOf("") }
    var responseBody by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    // Predefined Header Keys
    val commonHeaderKeys = listOf(
        "Content-Type", "Authorization", "User-Agent", "Accept", 
        "Cache-Control", "Cookie", "Referer", "Host"
    )
    
    // Predefined Values for specific keys
    val commonHeaderValues = mapOf(
        "Content-Type" to listOf("application/json", "application/x-www-form-urlencoded", "text/plain", "text/html", "multipart/form-data"),
        "Accept" to listOf("application/json", "*/*", "text/plain"),
        "Cache-Control" to listOf("no-cache", "max-age=0")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("URL 请求测试", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // URL and Method Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    OutlinedButton(
                        onClick = { expandedMethod = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text(method)
                    }
                    DropdownMenu(expanded = expandedMethod, onDismissRequest = { expandedMethod = false }) {
                        methods.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    method = m
                                    expandedMethod = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            // Headers Section
            Text("请求头 (Headers)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            headers.forEachIndexed { index, pair ->
                HeaderRow(
                    index = index,
                    initialKey = pair.first,
                    initialValue = pair.second,
                    commonKeys = commonHeaderKeys,
                    commonValuesMap = commonHeaderValues,
                    onUpdate = { k, v -> headers[index] = Pair(k, v) },
                    onDelete = { headers.removeAt(index) }
                )
            }
            TextButton(
                onClick = { headers.add(Pair("", "")) },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加请求头")
            }

            // Body Section (if not GET)
            if (method != "GET") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("请求体 (Body)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    bodyFormats.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = bodyFormats.size),
                            onClick = { 
                                bodyFormat = label
                                // Auto update Content-Type
                                val contentType = when(label) {
                                    "JSON" -> "application/json"
                                    "Form-Data" -> "application/x-www-form-urlencoded"
                                    else -> "text/plain"
                                }
                                val ctIndex = headers.indexOfFirst { it.first.equals("Content-Type", true) }
                                if (ctIndex != -1) {
                                    headers[ctIndex] = Pair("Content-Type", contentType)
                                } else {
                                    headers.add(0, Pair("Content-Type", contentType))
                                }
                            },
                            selected = bodyFormat == label,
                            label = { Text(label) }
                        )
                    }
                    }

                    OutlinedTextField(
                        value = requestBody,
                        onValueChange = { requestBody = it },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { 
                            val p = if (bodyFormat == "JSON") "{\"key\": \"value\"}" 
                                   else if (bodyFormat == "Form-Data") "key1=value1&key2=value2"
                                   else "Simple text body..."
                            Text(p)
                        },
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            }

            Button(
                onClick = {
                    isProcessing = true
                    responseStatus = "请求中..."
                    responseHeaders = ""
                    responseBody = ""
                    coroutineScope.launch {
                        performRequest(url, method, headers, requestBody) { status, h, body ->
                            responseStatus = status
                            responseHeaders = h
                            responseBody = body
                            isProcessing = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("发送请求", fontWeight = FontWeight.Bold)
                }
            }

            // Results Section
            if (responseStatus.isNotEmpty()) {
                Text("响应状态: $responseStatus", fontWeight = FontWeight.Bold, color = if (responseStatus.contains("200") || responseStatus.contains("OK")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("响应头 (Headers)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(responseHeaders, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("响应体 (Body)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(responseBody, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderRow(
    index: Int,
    initialKey: String,
    initialValue: String,
    commonKeys: List<String>,
    commonValuesMap: Map<String, List<String>>,
    onUpdate: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    var key by remember { mutableStateOf(initialKey) }
    var value by remember { mutableStateOf(initialValue) }
    var showKeyDropdown by remember { mutableStateOf(false) }
    var showValueDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Key selection
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = key,
                onValueChange = { 
                    key = it
                    onUpdate(it, value)
                    showKeyDropdown = true 
                },
                placeholder = { Text("Key") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            DropdownMenu(
                expanded = showKeyDropdown && key.isNotEmpty() && commonKeys.any { it.contains(key, true) && it != key },
                onDismissRequest = { showKeyDropdown = false }
            ) {
                commonKeys.filter { it.contains(key, true) }.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            key = suggestion
                            onUpdate(suggestion, value)
                            showKeyDropdown = false
                        }
                    )
                }
            }
        }

        // Value selection
        Box(modifier = Modifier.weight(1.5f)) {
            val suggestions = commonValuesMap[key] ?: emptyList()
            OutlinedTextField(
                value = value,
                onValueChange = { 
                    value = it
                    onUpdate(key, it)
                    showValueDropdown = true
                },
                placeholder = { Text("Value") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                trailingIcon = if (suggestions.isNotEmpty()) {
                    {
                        IconButton(onClick = { showValueDropdown = !showValueDropdown }) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                } else null
            )
            DropdownMenu(
                expanded = showValueDropdown && suggestions.isNotEmpty(),
                onDismissRequest = { showValueDropdown = false }
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            value = suggestion
                            onUpdate(key, suggestion)
                            showValueDropdown = false
                        }
                    )
                }
            }
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}

private suspend fun performRequest(
    url: String, 
    method: String, 
    headers: List<Pair<String, String>>, 
    body: String,
    onResult: (String, String, String) -> Unit
) {
    val client = OkHttpClient()
    val requestBuilder = Request.Builder().url(url)
    
    headers.forEach { if (it.first.isNotBlank()) requestBuilder.addHeader(it.first, it.second) }
    
    val contentType = headers.find { it.first.equals("Content-Type", true) }?.second ?: "application/json"
    
    val requestBody = if (method != "GET" && body.isNotBlank()) {
        body.toRequestBody(contentType.toMediaTypeOrNull())
    } else if (method != "GET") {
        "".toRequestBody(null)
    } else null

    when (method) {
        "POST" -> requestBuilder.post(requestBody!!)
        "PUT" -> requestBuilder.put(requestBody!!)
        "DELETE" -> requestBuilder.delete(requestBody)
        "PATCH" -> requestBuilder.patch(requestBody!!)
        else -> requestBuilder.get()
    }

    val request = requestBuilder.build()
    
    withContext(Dispatchers.IO) {
        try {
            client.newCall(request).execute().use { response ->
                val status = "${response.code} ${response.message}"
                val hStrings = response.headers.toMultimap().entries.joinToString("\n") { 
                    "${it.key}: ${it.value.joinToString(", ")}" 
                }
                val bString = response.body?.string() ?: "No Response Body"
                withContext(Dispatchers.Main) { onResult(status, hStrings, bString) }
            }
        } catch (e: IOException) {
            withContext(Dispatchers.Main) { onResult("Error", "Failed to connect", e.message ?: "Unknown Error") }
        }
    }
}
