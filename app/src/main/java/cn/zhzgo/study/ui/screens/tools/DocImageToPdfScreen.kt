package cn.zhzgo.study.ui.screens.tools

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocImageToPdfScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }

    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                isProcessing = true
                coroutineScope.launch(Dispatchers.IO) {
                    val bitmaps = mutableListOf<Bitmap>()
                    for (uri in uris) {
                        try {
                            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                            val bmp = BitmapFactory.decodeStream(inputStream)
                            inputStream?.close()
                            if (bmp != null) bitmaps.add(bmp)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    withContext(Dispatchers.Main) {
                        selectedImages = bitmaps
                        isProcessing = false
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("图片转 PDF", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (selectedImages.isNotEmpty()) {
                        TextButton(onClick = {
                            if (selectedImages.isEmpty()) return@TextButton
                            isProcessing = true
                            coroutineScope.launch(Dispatchers.IO) {
                                val success = saveImagesAsPdf(context, selectedImages)
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    if (success) Toast.makeText(context, "已保存到: Downloads/ZhzgoStudy", Toast.LENGTH_LONG).show()
                                    else Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Text("生成并保存", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { 
                        multiPhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedImages.isEmpty()) "选择多张图片" else "重新选择图片", fontWeight = FontWeight.SemiBold)
                }

                if (selectedImages.isNotEmpty()) {
                    Text("共选择 ${selectedImages.size} 张图片 (按以下顺序生成单页)", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(selectedImages) { index, bitmap ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) { Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold) }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Image(
                                        bitmap = bitmap.asImageBitmap(), 
                                        contentDescription = "Image $index", 
                                        modifier = Modifier.height(80.dp).weight(1f), 
                                        contentScale = ContentScale.Fit,
                                        alignment = Alignment.CenterStart
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                            Text("将多张图合并为 PDF 文档", color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            if (isProcessing) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

suspend fun saveImagesAsPdf(context: Context, bitmaps: List<Bitmap>): Boolean = withContext(Dispatchers.IO) {
    if (bitmaps.isEmpty()) return@withContext false
    
    val pdfDocument = PdfDocument()
    
    try {
        for ((index, bitmap) in bitmaps.withIndex()) {
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            
            val canvas: Canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            
            pdfDocument.finishPage(page)
        }
        
        val filename = "DOC_IMGTOPDF_${System.currentTimeMillis()}.pdf"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ZhzgoStudy")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                val outputStream: OutputStream? = resolver.openOutputStream(uri)
                outputStream?.use { out -> pdfDocument.writeTo(out) }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                return@withContext true
            }
        } else {
            // Deprecated fallback for older APIs but required
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(dir, filename)
            val outputStream = java.io.FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            return@withContext true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext false
    } finally {
        pdfDocument.close()
    }
    
    return@withContext false
}
