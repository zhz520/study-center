package cn.zhzgo.study.ui.screens.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSegmentScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                        val bmp = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        withContext(Dispatchers.Main) {
                            originalBitmap = bmp
                            previewBitmap = bmp
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("智能抠背景", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (previewBitmap != null) {
                        TextButton(onClick = {
                            if (previewBitmap == null) return@TextButton
                            isProcessing = true
                            coroutineScope.launch(Dispatchers.IO) {
                                // Save as PNG to keep transparency
                                val success = saveBitmapToGalleryHelper(context, previewBitmap!!, 1, 100)
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    if (success) Toast.makeText(context, "已保存到相册 (透明 PNG)", Toast.LENGTH_SHORT).show()
                                    else Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) { Text("保存结果", fontWeight = FontWeight.Bold) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable(enabled = previewBitmap == null) { 
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                    },
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    Image(bitmap = previewBitmap!!.asImageBitmap(), contentDescription = "Preview", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    IconButton(
                        onClick = { previewBitmap = originalBitmap },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    ) { Text("还原", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Pick Image", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("点击选择图片", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (isProcessing) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }

            if (previewBitmap != null) {
                Button(
                    onClick = {
                        isProcessing = true
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val options = SubjectSegmenterOptions.Builder().enableForegroundBitmap().build()
                                val segmenter = SubjectSegmentation.getClient(options)
                                val image = InputImage.fromBitmap(originalBitmap!!, 0)
                                val result = segmenter.process(image).await()
                                val newBmp = result.foregroundBitmap
                                withContext(Dispatchers.Main) {
                                    if (newBmp != null) previewBitmap = newBmp
                                    else Toast.makeText(context, "抠图失败，请确认识别到主体", Toast.LENGTH_SHORT).show()
                                    isProcessing = false
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) { turnsOffProcessing(); Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(Icons.Filled.AutoFixHigh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始提取主体，移除背景", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
private fun turnsOffProcessing() {}
