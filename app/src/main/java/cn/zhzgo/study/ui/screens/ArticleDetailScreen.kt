package cn.zhzgo.study.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.zhzgo.study.data.Article
import cn.zhzgo.study.data.Comment
import cn.zhzgo.study.ui.viewmodels.ArticleDetailViewModel
import coil.compose.AsyncImage
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ArticleDetailScreen(
    articleId: Int,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit = {},
    onOpenLink: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val viewModel: ArticleDetailViewModel = viewModel()
    val article by viewModel.article.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var commentText by remember { mutableStateOf("") }
    
    // Fetch data on start
    LaunchedEffect(articleId) {
        viewModel.fetchArticleDetail(context, articleId)
    }

    // Font size level: 0=Small, 1=Medium, 2=Large
    var fontSizeLevel by remember { mutableStateOf(1) }
    val bodyFontSizePx = when (fontSizeLevel) { 0 -> 14; 1 -> 16; else -> 18 }

    val isDark = isSystemInDarkTheme()
    val surfaceColor = if (isDark) Color(0xFF121212) else Color.White
    val inputBgColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val inputBorderColor = if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE)
    val onSurfaceColor = if (isDark) Color.White else Color.Black

    Scaffold(
        containerColor = surfaceColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        article?.title ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (article != null) {
                        IconButton(onClick = { fontSizeLevel = (fontSizeLevel + 1) % 3 }) {
                            Icon(Icons.Filled.TextFields, contentDescription = "字体大小", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            val a = article!!
                            val shareText = "${a.title}\n\n${a.summary ?: ""}\n\nhttps://study.zhzgo.cn/article/${a.id}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "分享文章"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "分享")
                        }
                        IconButton(onClick = {
                            val link = "https://study.zhzgo.cn/article/${article!!.id}"
                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(
                                ClipData.newPlainText("文章链接", link)
                            )
                            android.widget.Toast.makeText(context, "链接已复制", android.widget.Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "复制链接")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    navigationIconContentColor = onSurfaceColor,
                    titleContentColor = onSurfaceColor,
                    actionIconContentColor = if (isDark) Color.LightGray else Color.DarkGray
                )
            )
        },
        bottomBar = {
            if (article != null) {
                // High-End B&W Floating Input
                Surface(
                    color = surfaceColor.copy(alpha = 0.95f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .height(52.dp)
                            .clip(CircleShape)
                            .background(inputBgColor)
                            .border(
                                width = 1.dp,
                                color = inputBorderColor,
                                shape = CircleShape
                            )
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            TextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { 
                                    Text(
                                        "说点什么吧...", 
                                        fontSize = 14.sp, 
                                        color = (if (isDark) Color.Gray else Color.LightGray)
                                    ) 
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = onSurfaceColor
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = onSurfaceColor),
                                singleLine = true
                            )
                            
                            IconButton(
                                onClick = {
                                    if (commentText.isNotBlank()) {
                                        viewModel.postComment(context, articleId, commentText) { msg ->
                                            commentText = ""
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = commentText.isNotBlank(),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (commentText.isNotBlank()) onSurfaceColor 
                                        else inputBorderColor
                                    ),
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = if (commentText.isNotBlank()) surfaceColor 
                                                  else onSurfaceColor.copy(alpha = 0.2f)
                                )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send, 
                                    contentDescription = "发送",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🤔", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error!!, 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.fetchArticleDetail(context, articleId) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("重试获取")
                        }
                    }
                }
            }
            article != null -> {
                val a = article!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 24.dp) 
                ) {
                    // 1. Cover Image with Gradient Overlay
                    item {
                        val cover = a.getCover()
                        if (!cover.isNullOrEmpty()) {
                            AsyncImage(
                                model = cover,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                            )
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // 2. Article Header
                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                a.category?.let { cat ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = a.getTime()?.take(10) ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = a.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = onSurfaceColor,
                                lineHeight = 38.sp,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            // Author Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { a.author_id?.let { onUserClick(it.toString()) } },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = a.author_avatar ?: "https://api.dicebear.com/9.x/fun-emoji/png?seed=${a.author_name ?: "Unknown"}",
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = a.author_name ?: "学习助手",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = onSurfaceColor
                                    )
                                    Text(
                                        text = "发布于资源中心",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = onSurfaceColor.copy(alpha = 0.4f)
                                    )
                                }
                            }
                            a.summary?.let {
                                val outlineColor = MaterialTheme.colorScheme.outlineVariant
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 26.sp,
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .drawBehind {
                                            drawRoundRect(
                                                color = outlineColor,
                                                size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height),
                                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                            )
                                        }
                                        .padding(start = 12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                thickness = 0.5.dp
                            )
                        }
                    }

                    // 3. Article Content (WebView)
                    item {
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            ArticleContentWeb(a, bodyFontSizePx, onOpenLink)
                        }
                    }

                    // 4. Comments Section Header
                    item {
                        Column(modifier = Modifier.padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "评论交流",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                )
                                if (comments.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = comments.size.toString(),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Comments List
                    if (comments.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("💡", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "虚位以待，首评有奖~", 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            CommentItem(
                                comment = comment,
                                onUserClick = onUserClick,
                                onDelete = { viewModel.deleteComment(context, comment.id, articleId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleContentWeb(article: Article, fontSize: Int, onOpenLink: (String, String) -> Unit) {
    val rawContent = article.content ?: ""
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val textColor = if (isDark) "#e2e8f0" else "#1e293b"

    val bodyContent = if (rawContent.contains("<") && rawContent.contains(">")) {
        rawContent
    } else {
        "<p>${rawContent.replace("\n", "<br>")}</p>"
    }

    val html = """
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { 
                    font-size: ${fontSize}px; 
                    line-height: 1.85; 
                    color: $textColor; 
                    background: transparent; 
                    margin: 0; 
                    padding: 0;
                    font-family: -apple-system, system-ui, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                }
                body * {
                    color: inherit !important;
                    background-color: transparent !important;
                }
                img { max-width: 100%; height: auto; border-radius: 16.dp; margin: 16px 0; box-shadow: 0 4px 12px rgba(0,0,0,0.05); background-color: initial !important; }
                p { margin-bottom: 1.2em; text-align: justify; }
                a { color: #4f46e5; text-decoration: none; font-weight: 600; }
                blockquote { border-left: 4px solid #e2e8f0; padding-left: 16px; margin: 20px 0; color: #64748b; font-style: italic; }
            </style>
        </head>
        <body>$bodyContent</body>
        </html>
    """.trimIndent()

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(v: WebView?, u: String?): Boolean {
                        onOpenLink(u ?: "", article.title)
                        return true
                    }
                }
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.javaScriptEnabled = true
            }
        },
        update = { it.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)
    )
}

@Composable
fun CommentItem(comment: Comment, onUserClick: (String) -> Unit = {}, onDelete: () -> Unit) {
    val displayDate = remember(comment.created_at) {
        try {
            val dt = ZonedDateTime.parse(comment.created_at)
            dt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
        } catch (_: Exception) {
            comment.created_at.take(16).replace("T", " ")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = comment.avatar_icon ?: "https://api.dicebear.com/9.x/fun-emoji/png?seed=${comment.username}",
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                .clickable { onUserClick(comment.user_id.toString()) }
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.username,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
            ) {
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}
