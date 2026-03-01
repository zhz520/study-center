package cn.zhzgo.study.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimestampConverterScreen(onBack: () -> Unit) {
    var timestampInput by remember { mutableStateOf(System.currentTimeMillis().toString()) }
    var dateOutput by remember { mutableStateOf("") }
    
    var dateInput by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())) }
    var timestampOutput by remember { mutableStateOf("") }
    
    var isSecondsMode by remember { mutableStateOf(false) }

    LaunchedEffect(timestampInput, isSecondsMode) {
        val tsStr = timestampInput.trim()
        val ts = tsStr.toLongOrNull()
        if (ts != null && ts > 0) {
            val millis = if (isSecondsMode) ts * 1000 else ts
            dateOutput = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(millis))
        } else {
            dateOutput = "无效的时间戳"
        }
    }
    
    LaunchedEffect(dateInput, isSecondsMode) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(dateInput)
            if (date != null) {
                var millis = date.time
                if (isSecondsMode) millis /= 1000
                timestampOutput = millis.toString()
            } else {
                timestampOutput = "格式错误"
            }
        } catch (e: Exception) {
            timestampOutput = "格式错误"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("时间戳转换", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { isSecondsMode = !isSecondsMode }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSecondsMode) "秒 (s)" else "毫秒 (ms)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Current Time Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "现在时间戳 (${if (isSecondsMode) "s" else "ms"})", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
                    
                    LaunchedEffect(Unit) {
                        while(true) {
                            kotlinx.coroutines.delay(100)
                            currentTime = System.currentTimeMillis()
                        }
                    }
                    val displayTime = if (isSecondsMode) currentTime / 1000 else currentTime
                    Text(
                        text = displayTime.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { timestampInput = displayTime.toString() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text("获取现在", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // Timestamp to Date
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("时间戳 ➔ 时间", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = timestampInput,
                        onValueChange = { timestampInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("输入时间戳") }
                    )
                    Text(
                        text = "= " + dateOutput, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Date to Timestamp
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("时间 ➔ 时间戳", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("yyyy-MM-dd HH:mm:ss") }
                    )
                    Text(
                        text = "= " + timestampOutput, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
