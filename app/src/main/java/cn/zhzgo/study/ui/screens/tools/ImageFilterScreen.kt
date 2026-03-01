package cn.zhzgo.study.ui.screens.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
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
import cn.zhzgo.study.ui.screens.tools.saveBitmapToGalleryHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageFilterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    var isProcessing by remember { mutableStateOf(false) }
    var activeFilterIndex by remember { mutableStateOf(0) }

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
                            activeFilterIndex = 0
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    )

    val filters = listOf(
        "原图" to null,
        "黑白" to floatArrayOf(
            0.33f, 0.59f, 0.11f, 0f, 0f,
            0.33f, 0.59f, 0.11f, 0f, 0f,
            0.33f, 0.59f, 0.11f, 0f, 0f,
            0f,    0f,    0f,    1f, 0f
        ),
        "反相" to floatArrayOf(
            -1f,  0f,  0f, 0f, 255f,
             0f, -1f,  0f, 0f, 255f,
             0f,  0f, -1f, 0f, 255f,
             0f,  0f,  0f, 1f,   0f
        ),
        "怀旧" to floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f,     0f,     0f,     1f, 0f
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("一键滤镜", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    if (previewBitmap != null) {
                        TextButton(onClick = {
                            if (previewBitmap == null) return@TextButton
                            isProcessing = true
                            coroutineScope.launch(Dispatchers.IO) {
                                val success = saveBitmapToGalleryHelper(context, previewBitmap!!, 0, 100)
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    if (success) Toast.makeText(context, "已保存到相册", Toast.LENGTH_SHORT).show()
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
                modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable(enabled = previewBitmap == null) { 
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                    },
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    Image(bitmap = previewBitmap!!.asImageBitmap(), contentDescription = "Preview", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    IconButton(
                        onClick = { 
                            previewBitmap = originalBitmap 
                            activeFilterIndex = 0
                        },
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
                LazyRow(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(filters) { index, filterPair ->
                        val isSelected = activeFilterIndex == index
                        Box(
                            modifier = Modifier.fillMaxHeight().aspectRatio(1f).clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    activeFilterIndex = index
                                    isProcessing = true
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val array = filterPair.second
                                        if (array == null) {
                                            withContext(Dispatchers.Main) { previewBitmap = originalBitmap; isProcessing = false }
                                        } else {
                                            val bmp = Bitmap.createBitmap(originalBitmap!!.width, originalBitmap!!.height, Bitmap.Config.ARGB_8888)
                                            val canvas = Canvas(bmp)
                                            val paint = Paint()
                                            val colorMatrix = ColorMatrix(array)
                                            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
                                            canvas.drawBitmap(originalBitmap!!, 0f, 0f, paint)
                                            withContext(Dispatchers.Main) { previewBitmap = bmp; isProcessing = false }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filterPair.first, 
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
