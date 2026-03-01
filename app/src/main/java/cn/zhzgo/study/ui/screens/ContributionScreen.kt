package cn.zhzgo.study.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.zhzgo.study.data.Article
import cn.zhzgo.study.ui.viewmodels.ContributionViewModel
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ContributionViewModel = viewModel()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val error by viewModel.error.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf(TextFieldValue("")) }
    var category by remember { mutableStateOf("投递资料") }
    var coverUrl by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Edit, 1: Preview

    val uploadUrl by viewModel.uploadUrl.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadFile(context, it) }
    }

    LaunchedEffect(uploadUrl) {
        uploadUrl?.let { url ->
            val insertion = "![图片]($url)"
            val newText = content.text.substring(0, content.selection.start) + 
                          insertion + 
                          content.text.substring(content.selection.end)
            content = TextFieldValue(
                text = newText,
                selection = TextRange(content.selection.start + insertion.length)
            )
            viewModel.resetUploadUrl()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            onBack()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    val isDark = isSystemInDarkTheme()
    val surfaceColor = if (isDark) Color(0xFF121212) else Color.White
    val onSurfaceColor = if (isDark) Color.White else Color.Black

    Scaffold(
        containerColor = surfaceColor,
        topBar = {
            TopAppBar(
                title = { Text("用户投稿", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    navigationIconContentColor = onSurfaceColor,
                    titleContentColor = onSurfaceColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "分享你的知识",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = onSurfaceColor
            )
            Text(
                text = "投稿通过审核后，将显示在资源中心，供所有用户学习。",
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceColor.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            ContributionTextField(
                value = title,
                onValueChange = { title = it },
                label = "文章标题",
                placeholder = "给文章起一个吸引人的标题",
                icon = Icons.Default.Title
            )

            Spacer(modifier = Modifier.height(20.dp))

            ContributionTextField(
                value = category,
                onValueChange = { category = it },
                label = "文章分类",
                placeholder = "如：学习资料、备考心得",
                icon = Icons.Default.Label
            )

            Spacer(modifier = Modifier.height(20.dp))

            ContributionTextField(
                value = coverUrl,
                onValueChange = { coverUrl = it },
                label = "封面图片链接 (可选)",
                placeholder = "请输入公开的图片 URL",
                icon = Icons.Default.Link
            )

            Spacer(modifier = Modifier.height(20.dp))

            Spacer(modifier = Modifier.height(30.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("编辑内容", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("实时预览", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                MarkdownToolbar(
                    onAction = { action ->
                        val start = content.selection.start
                        val end = content.selection.end
                        val selectedText = content.text.substring(start, end)
                        
                        val (wrapped, offset) = when (action) {
                            "bold" -> "**$selectedText**" to 2
                            "italic" -> "_${selectedText}_" to 1
                            "link" -> "[$selectedText](url)" to 1
                            "h1" -> "# $selectedText" to 2
                            "h2" -> "## $selectedText" to 3
                            "quote" -> "> $selectedText" to 2
                            else -> selectedText to 0
                        }
                        
                        val newText = content.text.substring(0, start) + wrapped + content.text.substring(end)
                        content = TextFieldValue(
                            text = newText,
                            selection = if (selectedText.isEmpty()) TextRange(start + offset) else TextRange(start + wrapped.length)
                        )
                    },
                    onImageClick = { imagePicker.launch("image/*") },
                    isUploading = isUploading
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp),
                    placeholder = { Text("在这里输入详细内容...", fontSize = 14.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.1f)
                    )
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = onSurfaceColor.copy(alpha = if (isDark) 0.05f else 0.02f)
                    ),
                    border = BorderStroke(1.dp, onSurfaceColor.copy(alpha = 0.1f))
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        PreviewMarkdown(content.text)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && content.text.isNotBlank()) {
                        viewModel.submitArticle(context, title, content.text, category, coverUrl.ifBlank { null })
                    } else {
                        Toast.makeText(context, "请完善标题和内容", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = onSurfaceColor,
                    contentColor = surfaceColor
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = surfaceColor)
                } else {
                    Text("提交投稿", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MarkdownToolbar(
    onAction: (String) -> Unit,
    onImageClick: () -> Unit,
    isUploading: Boolean
) {
    val isDark = isSystemInDarkTheme()
    val tintColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
    
    LazyRow(
        modifier = Modifier.padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { ToolbarIcon(Icons.Default.FormatBold, "加粗", tintColor) { onAction("bold") } }
        item { ToolbarIcon(Icons.Default.FormatItalic, "斜体", tintColor) { onAction("italic") } }
        item { ToolbarIcon(Icons.Default.FormatQuote, "引用", tintColor) { onAction("quote") } }
        item { ToolbarIcon(Icons.Default.FormatSize, "标题1", tintColor) { onAction("h1") } }
        item { ToolbarIcon(Icons.Default.Link, "链接", tintColor) { onAction("link") } }
        item { 
            Box {
                ToolbarIcon(Icons.Default.Image, "插入图片", tintColor) { onImageClick() }
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp).align(Alignment.TopEnd),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ToolbarIcon(icon: ImageVector, description: String, tint: Color, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .border(1.dp, tint.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun PreviewMarkdown(content: String) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) "#e2e8f0" else "#1e293b"
    val bgColor = if (isDark) "#1e1e1e" else "#ffffff"

    // Simple markdown to HTML for local preview if no real engine available
    // For a real app, use a library like Markwon or a properly configured WebView
    val html = """
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { 
                    font-family: -apple-system, sans-serif; 
                    line-height: 1.6; 
                    color: $textColor; 
                    background-color: transparent;
                    margin: 0; padding: 0;
                }
                img { max-width: 100%; border-radius: 8.dp; }
                blockquote { border-left: 4px solid #cbd5e1; padding-left: 16px; color: #64748b; margin: 16px 0; }
                pre { background: #f1f5f9; padding: 12px; border-radius: 8px; overflow-x: auto; }
                code { font-family: monospace; background: rgba(0,0,0,0.05); padding: 2px 4px; border-radius: 4px; }
            </style>
            <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
        </head>
        <body>
            <div id="content"></div>
            <script>
                document.getElementById('content').innerHTML = marked.parse(`${content.replace("`", "\\`").replace("$", "\\$")}`);
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                setBackgroundColor(0)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        },
        modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp)
    )
}

@Composable
fun ContributionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            leadingIcon = { 
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(18.dp)
                ) 
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
            )
        )
    }
}
