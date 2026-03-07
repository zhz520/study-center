package cn.zhzgo.study.ui.screens.tools

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlin.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import cn.zhzgo.study.data.VideoParseResult
import cn.zhzgo.study.ui.viewmodels.VideoParserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoParserScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val viewModel: VideoParserViewModel = viewModel()

    val inputUrl by viewModel.inputUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val result by viewModel.result.collectAsState()
    val error by viewModel.error.collectAsState()

    // Explicit colors to avoid any theme-inherited pink
    val backgroundColor = Color.White
    val surfaceColor = Color(0xFFF7F7F7)
    val accentColor = Color.Black
    val textColor = Color(0xFF222222)
    val secondaryTextColor = Color(0xFF666666)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("视频解析", fontWeight = FontWeight.Bold, color = accentColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = accentColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Supported Platforms Banner ---
            item(key = "platforms") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("抖音", "快手", "小红书").forEach { name ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.05f)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelMedium,
                                color = secondaryTextColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // --- Input Area ---
            item(key = "input") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = surfaceColor
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "粘贴分享链接",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Text field
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = backgroundColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (inputUrl.isEmpty()) {
                                        Text(
                                            "复制并粘贴链接到这里...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = secondaryTextColor.copy(alpha = 0.5f)
                                        )
                                    }
                                    BasicTextField(
                                        value = inputUrl,
                                        onValueChange = { viewModel.updateUrl(it) },
                                        textStyle = TextStyle(
                                            color = textColor,
                                            fontSize = 14.sp
                                        ),
                                        cursorBrush = SolidColor(accentColor),
                                        maxLines = 4,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (inputUrl.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.updateUrl("") },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Clear,
                                            contentDescription = "清除",
                                            modifier = Modifier.size(16.dp),
                                            tint = secondaryTextColor.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Paste button
                            OutlinedButton(
                                onClick = {
                                    val clip = clipboardManager.getText()?.text ?: ""
                                    if (clip.isNotBlank()) {
                                        viewModel.updateUrl(clip)
                                    } else {
                                        Toast.makeText(context, "剪贴板为空", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = SolidColor(accentColor.copy(alpha = 0.1f))
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Filled.ContentPaste,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = textColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("粘贴", color = textColor)
                            }

                            // Parse button
                            Button(
                                onClick = { viewModel.parseVideo() },
                                enabled = !isLoading && inputUrl.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = backgroundColor,
                                    disabledContainerColor = accentColor.copy(alpha = 0.12f),
                                    disabledContentColor = textColor.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = backgroundColor,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Filled.FlashOn, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("解析")
                                }
                            }
                        }
                    }
                }
            }

            // --- Error ---
            if (error != null) {
                item(key = "error") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEBEE), // Subtle red for error, but still monochrome-adjacent
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Error, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Red.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // --- Result ---
            if (result != null) {
                item(key = "result") {
                    ResultCard(
                        result = result!!,
                        viewModel = viewModel,
                        accentColor = accentColor,
                        surfaceColor = surfaceColor,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor
                    )
                }
            }

            // --- Instructions ---
            item(key = "tips") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "使用流程",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val steps = listOf(
                        "1. 在分享平台点击「复制链接」",
                        "2. 在上方粘贴链接并点击「解析」",
                        "3. 解析成功后可直接查看和保存"
                    )
                    steps.forEach { step ->
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryTextColor,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    result: VideoParseResult,
    viewModel: VideoParserViewModel,
    accentColor: Color,
    surfaceColor: Color,
    textColor: Color,
    secondaryTextColor: Color
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Type & Platform
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (result.type == "video") "视频" else "图集",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = result.title ?: "未知内容",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Media display
            if (result.type == "video") {
                result.videoUrl?.let { videoUrl ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black)
                    ) {
                        VideoPlayer(videoUrl = videoUrl)
                    }
                }
            } else if (result.type == "image" && !result.images.isNullOrEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(result.images) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(160.dp)
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = 0.05f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Secondary actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Copy link button
                    Button(
                        onClick = {
                            val urlToCopy = if (result.type == "video") result.videoUrl else result.images?.firstOrNull()
                            urlToCopy?.let {
                                clipboardManager.setText(AnnotatedString(it))
                                Toast.makeText(context, "链接已复制", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor.copy(alpha = 0.05f),
                            contentColor = textColor
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("复制链接", fontSize = 13.sp)
                    }

                    // Open browser button
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(if (result.type == "video") result.videoUrl else result.images?.firstOrNull()))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor.copy(alpha = 0.05f),
                            contentColor = textColor
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("外部打开", fontSize = 13.sp)
                    }
                }

                // Primary Save button
                Button(
                    onClick = {
                        if (result.type == "video") {
                            result.videoUrl?.let { viewModel.saveToGallery(it, "video_${System.currentTimeMillis()}") }
                        } else {
                            result.images?.forEachIndexed { index, url ->
                                viewModel.saveToGallery(url, "image_${System.currentTimeMillis()}_$index")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (result.type == "video") "保存视频到相册" else "保存全部图片到相册", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
