package cn.zhzgo.study.ui.screens.tools

import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Compress
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
fun MediaVideoCompressScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var compressionLevel by remember { mutableFloatStateOf(28f) } // CRF: 0-51, lower is better quality. Typical 23-30.

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> selectedVideoUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("视频瘦身/压缩", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
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
                    onClick = { videoPickerLauncher.launch("video/*") },
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
                            Icons.Filled.Compress,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedVideoUri == null) "点击选择视频文件" else "视频已选择\n${selectedVideoUri?.path?.substringAfterLast("/")}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                if (selectedVideoUri != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("压缩强度 (CRF: ${compressionLevel.toInt()})", fontWeight = FontWeight.Medium)
                            Text("值越大体积越小，画质损失越多 (推荐: 28)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = compressionLevel,
                                onValueChange = { compressionLevel = it },
                                valueRange = 18f..40f,
                                steps = 22,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            isProcessing = true
                            coroutineScope.launch(Dispatchers.IO) {
                                val inputPath = FFmpegKitConfig.getSafParameterForRead(context, selectedVideoUri)
                                val tempOutputFile = File(context.cacheDir, "temp_compressed_${System.currentTimeMillis()}.mp4")
                                
                                // H264 encode with varying CRF for size reduction, fast preset
                                val crf = compressionLevel.toInt()
                                val cmd = "-i \"$inputPath\" -vcodec libx264 -crf $crf -preset fast -c:a copy \"${tempOutputFile.absolutePath}\""
                                
                                val session = FFmpegKit.execute(cmd)
                                val returnCode = session.returnCode
                                
                                if (ReturnCode.isSuccess(returnCode)) {
                                    val filename = "VIDEO_COMPRESSED_${System.currentTimeMillis()}.mp4"
                                    val saved = MediaUtils.saveFileToPublicStorage(
                                        context,
                                        tempOutputFile,
                                        filename,
                                        "video/mp4",
                                        Environment.DIRECTORY_MOVIES + "/ZhzgoStudy"
                                    )
                                    tempOutputFile.delete()
                                    
                                    withContext(Dispatchers.Main) {
                                        isProcessing = false
                                        if (saved) {
                                            Toast.makeText(context, "压缩成功：已保存至 电影/ZhzgoStudy", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "压缩成功但保存失败", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    tempOutputFile.delete()
                                    withContext(Dispatchers.Main) {
                                        isProcessing = false
                                        Log.e("FFmpegKit", "Compress Video Failed: ${session.allLogsAsString}")
                                        Toast.makeText(context, "压缩失败，请重试", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("开始压缩", fontSize = MaterialTheme.typography.titleMedium.fontSize, fontWeight = FontWeight.Bold)
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
                            Text("视频重编码中，这可能需要一会儿...", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
