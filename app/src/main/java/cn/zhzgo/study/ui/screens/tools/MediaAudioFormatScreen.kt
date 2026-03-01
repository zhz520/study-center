package cn.zhzgo.study.ui.screens.tools

import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaAudioFormatScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    
    val formats = listOf("MP3", "WAV", "AAC", "FLAC")
    var selectedFormatIndex by remember { mutableIntStateOf(0) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> selectedAudioUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("音频格式转换", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    onClick = { audioPickerLauncher.launch("audio/*") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Audiotrack,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedAudioUri == null) "点击选择音频文件 (录音/音乐)" else "已选择音频\n${selectedAudioUri?.path?.substringAfterLast("/")}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                if (selectedAudioUri != null) {
                    Text("选择目标格式", fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        formats.forEachIndexed { index, format ->
                            val isSelected = index == selectedFormatIndex
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clickable { selectedFormatIndex = index },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = format,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isSelected) {
                                            Icon(
                                                Icons.Filled.CheckCircle, 
                                                contentDescription = null, 
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            isProcessing = true
                            coroutineScope.launch(Dispatchers.IO) {
                                val inputPath = FFmpegKitConfig.getSafParameterForRead(context, selectedAudioUri)
                                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                val appDir = File(downloadsDir, "ZhzgoStudy")
                                if (!appDir.exists()) appDir.mkdirs()
                                
                                val formatExt = formats[selectedFormatIndex].lowercase()
                                val filename = "AUDIO_CONVERTED_${System.currentTimeMillis()}.$formatExt"
                                val outputFile = File(appDir, filename)
                                
                                // Determine audio codec based on choice
                                val codecParam = when(formatExt) {
                                    "mp3" -> "-c:a libmp3lame -q:a 2"
                                    "wav" -> "-c:a pcm_s16le"
                                    "aac" -> "-c:a aac -b:a 192k"
                                    "flac" -> "-c:a flac"
                                    else -> "-c:a copy"
                                }
                                
                                val cmd = "-i \"$inputPath\" $codecParam \"${outputFile.absolutePath}\""
                                
                                val session = FFmpegKit.execute(cmd)
                                val returnCode = session.returnCode
                                
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    if (ReturnCode.isSuccess(returnCode)) {
                                        Toast.makeText(context, "转换成功：已保存至 Downloads/ZhzgoStudy", Toast.LENGTH_LONG).show()
                                    } else {
                                        Log.e("FFmpegKit", "Convert Audio Failed: ${session.allLogsAsString}")
                                        Toast.makeText(context, "编码失败，该格式可能受损或不支持此转换模式", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("立即转换", fontSize = MaterialTheme.typography.titleMedium.fontSize, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("转换中，请勿关闭页面...", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
