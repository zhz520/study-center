package cn.zhzgo.study.ui.screens.tools

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCompressScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    var isProcessing by remember { mutableStateOf(false) }
    var quality by remember { mutableStateOf(85f) }
    var scalePercent by remember { mutableStateOf(100f) }
    val formatOptions = listOf("JPEG", "PNG", "WEBP")
    var selectedFormatIndex by remember { mutableStateOf(0) }

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
                title = { Text("图片压缩缩放", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (previewBitmap != null) {
                        TextButton(onClick = {
                            if (previewBitmap == null) return@TextButton
                            isProcessing = true
                            coroutineScope.launch(Dispatchers.IO) {
                                val success = saveBitmapToGalleryHelper(context, previewBitmap!!, selectedFormatIndex, quality.toInt())
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    if (success) Toast.makeText(context, "已保存到: Pictures/ZhzgoStudy", Toast.LENGTH_LONG).show()
                                    else Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Text("保存结果", fontWeight = FontWeight.Bold)
                        }
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
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("缩放图片", fontWeight = FontWeight.SemiBold)
                                Text("${scalePercent.toInt()}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(value = scalePercent, onValueChange = { scalePercent = it }, valueRange = 10f..100f, steps = 9)
                            Button(
                                onClick = {
                                    isProcessing = true
                                    coroutineScope.launch(Dispatchers.IO) {
                                        if (scalePercent < 100f) {
                                            val width = (originalBitmap!!.width * (scalePercent / 100f)).toInt().coerceAtLeast(1)
                                            val height = (originalBitmap!!.height * (scalePercent / 100f)).toInt().coerceAtLeast(1)
                                            val scaled = Bitmap.createScaledBitmap(originalBitmap!!, width, height, true)
                                            withContext(Dispatchers.Main) { previewBitmap = scaled; isProcessing = false }
                                        } else {
                                            withContext(Dispatchers.Main) { previewBitmap = originalBitmap; isProcessing = false }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("预览缩放效果") }
                        }
                    }
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("导出格式", fontWeight = FontWeight.SemiBold)
                            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.background).padding(4.dp)) {
                                formatOptions.forEachIndexed { index, format ->
                                    val isSelected = selectedFormatIndex == index
                                    Box(
                                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                            .clickable { selectedFormatIndex = index }.padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(format, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                             color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            if (selectedFormatIndex != 1) { // 1 is PNG
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("保存质量 (仅保存生效)", fontWeight = FontWeight.SemiBold)
                                    Text("${quality.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(value = quality, onValueChange = { quality = it }, valueRange = 10f..100f)
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun saveBitmapToGalleryHelper(context: Context, bitmap: Bitmap, formatIndex: Int, quality: Int): Boolean = withContext(Dispatchers.IO) {
    val compressFormat = when (formatIndex) {
        0 -> Bitmap.CompressFormat.JPEG
        1 -> Bitmap.CompressFormat.PNG
        2 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
        else -> Bitmap.CompressFormat.JPEG
    }
    val mimeType = when (formatIndex) {
        0 -> "image/jpeg"
        1 -> "image/png"
        2 -> "image/webp"
        else -> "image/jpeg"
    }
    val extension = when (formatIndex) {
        0 -> "jpg"
        1 -> "png"
        2 -> "webp"
        else -> "jpg"
    }
    val filename = "IMG_EDIT_${System.currentTimeMillis()}.$extension"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ZhzgoStudy")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }
    val resolver = context.contentResolver
    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    if (imageUri != null) {
        try {
            val outputStream: OutputStream? = resolver.openOutputStream(imageUri)
            val finalQuality = if (formatIndex == 1) 100 else quality
            outputStream?.use { out -> bitmap.compress(compressFormat, finalQuality, out) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            resolver.delete(imageUri, null, null)
            return@withContext false
        }
    }
    return@withContext false
}
