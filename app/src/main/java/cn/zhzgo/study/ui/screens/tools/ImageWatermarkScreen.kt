package cn.zhzgo.study.ui.screens.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.max
import cn.zhzgo.study.ui.screens.tools.saveBitmapToGalleryHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ImageWatermarkScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var mosaicBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    var isProcessing by remember { mutableStateOf(false) }
    var brushSize by remember { mutableStateOf(40f) }
    var displaySize by remember { mutableStateOf(IntSize.Zero) }
    
    // Stack for undo
    val history = remember { mutableStateListOf<Bitmap>() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                        val bmp = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        
                        // Create a mosaic scaled version for brush content
                        val scaleFactor = 0.05f 
                        val w = max(1, (bmp.width * scaleFactor).toInt())
                        val h = max(1, (bmp.height * scaleFactor).toInt())
                        val tiny = Bitmap.createScaledBitmap(bmp, w, h, false)
                        val mosaic = Bitmap.createScaledBitmap(tiny, bmp.width, bmp.height, false)
                        
                        withContext(Dispatchers.Main) {
                            history.clear()
                            val mutableBmp = bmp.copy(Bitmap.Config.ARGB_8888, true)
                            originalBitmap = bmp
                            previewBitmap = mutableBmp
                            mosaicBitmap = mosaic
                            history.add(mutableBmp.copy(Bitmap.Config.ARGB_8888, true))
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
                title = { Text("马赛克去水印", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (previewBitmap != null) {
                        IconButton(onClick = {
                            if (history.size > 1) {
                                history.removeLast()
                                previewBitmap = history.last().copy(Bitmap.Config.ARGB_8888, true)
                            }
                        }, enabled = history.size > 1) {
                            Icon(Icons.Filled.Undo, contentDescription = "Undo")
                        }
                        TextButton(onClick = {
                            if (previewBitmap == null) return@TextButton
                            isProcessing = true
                            coroutineScope.launch(Dispatchers.IO) {
                                val success = saveBitmapToGalleryHelper(context, previewBitmap!!, 0, 95)
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    if (success) Toast.makeText(context, "已保存到相册", Toast.LENGTH_SHORT).show()
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
                modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable(enabled = previewBitmap == null) { 
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                    }.onSizeChanged { displaySize = it },
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null && displaySize.width > 0 && displaySize.height > 0) {
                    // Calculate visual scaling
                    val imgW = previewBitmap!!.width.toFloat()
                    val imgH = previewBitmap!!.height.toFloat()
                    val boxW = displaySize.width.toFloat()
                    val boxH = displaySize.height.toFloat()
                    
                    val scaleX = boxW / imgW
                    val scaleY = boxH / imgH
                    val scale = minOf(scaleX, scaleY)
                    
                    val renderW = imgW * scale
                    val renderH = imgH * scale
                    val offsetX = (boxW - renderW) / 2f
                    val offsetY = (boxH - renderH) / 2f
                    
                    Image(
                        bitmap = previewBitmap!!.asImageBitmap(), 
                        contentDescription = "Preview", 
                        modifier = Modifier.fillMaxSize()
                            .pointerInteropFilter { event ->
                                val x = event.x
                                val y = event.y
                                
                                if (x >= offsetX && x <= offsetX + renderW && y >= offsetY && y <= offsetY + renderH) {
                                    val bitmapX = ((x - offsetX) / scale).toInt()
                                    val bitmapY = ((y - offsetY) / scale).toInt()
                                    
                                    if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                                        // Draw mosaic on preview bitmap
                                        coroutineScope.launch(Dispatchers.IO) {
                                            if (previewBitmap != null && mosaicBitmap != null) {
                                                val bmp = previewBitmap!!
                                                val canvas = Canvas(bmp)
                                                val paint = Paint().apply {
                                                    isAntiAlias = false
                                                    style = Paint.Style.FILL
                                                }
                                                // Create clipping path for brush
                                                val path = android.graphics.Path()
                                                path.addCircle(bitmapX.toFloat(), bitmapY.toFloat(), brushSize * (1f / scale), android.graphics.Path.Direction.CW)
                                                
                                                canvas.save()
                                                canvas.clipPath(path)
                                                canvas.drawBitmap(mosaicBitmap!!, 0f, 0f, paint)
                                                canvas.restore()
                                                
                                                // Trigger recomposition (hack)
                                                withContext(Dispatchers.Main) {
                                                    val temp = bmp.copy(Bitmap.Config.ARGB_8888, true)
                                                    previewBitmap = temp
                                                }
                                            }
                                        }
                                        true
                                    } else if (event.action == MotionEvent.ACTION_UP) {
                                        history.add(previewBitmap!!.copy(Bitmap.Config.ARGB_8888, true))
                                        true
                                    } else false
                                } else false
                            }, 
                        contentScale = ContentScale.Fit
                    )
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
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Brush, contentDescription = "Brush", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("画笔粗细", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("${brushSize.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(value = brushSize, onValueChange = { brushSize = it }, valueRange = 10f..150f)
                        Text("在上方图片上滑动以涂抹马赛克", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
