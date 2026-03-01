package cn.zhzgo.study.ui.screens.tools

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FileOpen
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocPdfToImageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var pdfPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                pdfUri = it
                isProcessing = true
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val pfd: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(it, "r")
                        if (pfd != null) {
                            val renderer = PdfRenderer(pfd)
                            val bitmaps = mutableListOf<Bitmap>()
                            for (i in 0 until renderer.pageCount) {
                                val page = renderer.openPage(i)
                                // Only render a small thumbnail for the UI preview to avoid OOM
                                val scale = 400f / page.width
                                val width = 400
                                val height = (page.height * scale).toInt()
                                
                                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                bitmap.eraseColor(android.graphics.Color.WHITE)
                                
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                bitmaps.add(bitmap)
                                page.close()
                            }
                            renderer.close()
                            pfd.close()
                            
                            withContext(Dispatchers.Main) {
                                pdfPages = bitmaps
                                isProcessing = false
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "解析 PDF 失败 (文件可能过大)", Toast.LENGTH_SHORT).show()
                            isProcessing = false
                        }
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF 转图片", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (pdfPages.isNotEmpty()) {
                        TextButton(onClick = {
                            if (pdfPages.isEmpty()) return@TextButton
                            isProcessing = true
                            coroutineScope.launch(Dispatchers.IO) {
                                var successCount = 0
                                try {
                                    val pfd: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(pdfUri!!, "r")
                                    if (pfd != null) {
                                        val renderer = PdfRenderer(pfd)
                                        for (i in 0 until renderer.pageCount) {
                                            val page = renderer.openPage(i)
                                            val scale = java.lang.Math.min(300f / 72f, 4096f / java.lang.Math.max(page.width, page.height))
                                            val width = (page.width * scale).toInt()
                                            val height = (page.height * scale).toInt()
                                            
                                            val hqBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                            hqBitmap.eraseColor(android.graphics.Color.WHITE)
                                            page.render(hqBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                            page.close()
                                            
                                            if (saveBitmapToGalleryHelper(context, hqBitmap, 1, 100)) {
                                                successCount++
                                            }
                                            hqBitmap.recycle()
                                        }
                                        renderer.close()
                                        pfd.close()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    Toast.makeText(context, "成功保存 $successCount 张到 Pictures/ZhzgoStudy", Toast.LENGTH_LONG).show()
                                }
                            }
                        }) {
                            Text("保存所有页", fontWeight = FontWeight.Bold)
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
                    onClick = { pdfPickerLauncher.launch("application/pdf") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.FileOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (pdfUri == null) "选择 PDF 文档" else "重新选择 PDF", fontWeight = FontWeight.SemiBold)
                }

                if (pdfPages.isNotEmpty()) {
                    Text("共成功提取 ${pdfPages.size} 页，单页点击可单独保存", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(pdfPages) { index, bitmap ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("第 ${index + 1} 页", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        IconButton(onClick = {
                                            isProcessing = true
                                            coroutineScope.launch(Dispatchers.IO) {
                                                var success = false
                                                try {
                                                    val pfd = context.contentResolver.openFileDescriptor(pdfUri!!, "r")
                                                    if (pfd != null) {
                                                        val renderer = PdfRenderer(pfd)
                                                        val page = renderer.openPage(index)
                                                        val scale = java.lang.Math.min(300f / 72f, 4096f / java.lang.Math.max(page.width, page.height))
                                                        val width = (page.width * scale).toInt()
                                                        val height = (page.height * scale).toInt()
                                                        val hqBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                                        hqBitmap.eraseColor(android.graphics.Color.WHITE)
                                                        page.render(hqBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                                        page.close()
                                                        
                                                        success = saveBitmapToGalleryHelper(context, hqBitmap, 1, 100)
                                                        hqBitmap.recycle()
                                                        renderer.close()
                                                        pfd.close()
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                                withContext(Dispatchers.Main) {
                                                    isProcessing = false
                                                    if (success) Toast.makeText(context, "第 ${index + 1} 页已保存到 Pictures/ZhzgoStudy", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Filled.Collections, contentDescription = "Save this page")
                                        }
                                    }
                                    
                                    Image(
                                        bitmap = bitmap.asImageBitmap(), 
                                        contentDescription = "PDF Page $index", 
                                        modifier = Modifier.height(300.dp).fillMaxWidth(), 
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Filled.Collections, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                            Text("将 PDF 的每一页转化为高清照片", color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            if (isProcessing) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在拼命处理 PDF...", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
