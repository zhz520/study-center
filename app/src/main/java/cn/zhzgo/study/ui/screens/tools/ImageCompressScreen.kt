package cn.zhzgo.study.ui.screens.tools

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
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCompressScreen(
    onBack: () -> Unit,
    viewModel: ImageCompressViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val formatOptions = listOf("JPEG", "PNG", "WEBP")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.loadBitmap(context, it) }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("图片压缩缩放", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (uiState.previewBitmap != null) {
                        TextButton(onClick = {
                            viewModel.saveImage(
                                context = context,
                                onSuccess = { Toast.makeText(context, "已保存到: Pictures/ZhzgoStudy", Toast.LENGTH_LONG).show() },
                                onError = { Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show() }
                            )
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
                    .clickable(enabled = uiState.previewBitmap == null && !uiState.isProcessing) { 
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                    },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.previewBitmap != null) {
                    Image(
                        bitmap = uiState.previewBitmap!!.asImageBitmap(), 
                        contentDescription = "Preview", 
                        modifier = Modifier.fillMaxSize(), 
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = { viewModel.restoreOriginal() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    ) { Text("还原", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Pick Image", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("点击选择图片", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (uiState.isProcessing) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)), 
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (uiState.previewBitmap != null) {
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), 
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("缩放图片", fontWeight = FontWeight.SemiBold)
                                Text("${uiState.scalePercent.toInt()}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = uiState.scalePercent, 
                                onValueChange = { viewModel.updateScalePercent(it) }, 
                                onValueChangeFinished = { viewModel.applyScale() },
                                valueRange = 10f..100f, 
                                steps = 9
                            )
                            Button(
                                onClick = { viewModel.applyScale() },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("预览缩放效果") }
                        }
                    }
                    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("导出格式", fontWeight = FontWeight.SemiBold)
                            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.background).padding(4.dp)) {
                                formatOptions.forEachIndexed { index, format ->
                                    val isSelected = uiState.selectedFormatIndex == index
                                    Box(
                                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                            .clickable { viewModel.updateFormatIndex(index) }.padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(format, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                             color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            if (uiState.selectedFormatIndex != 1) { // 1 is PNG
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("保存质量 (仅保存生效)", fontWeight = FontWeight.SemiBold)
                                    Text("${uiState.quality.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = uiState.quality, 
                                    onValueChange = { viewModel.updateQuality(it) }, 
                                    valueRange = 10f..100f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
