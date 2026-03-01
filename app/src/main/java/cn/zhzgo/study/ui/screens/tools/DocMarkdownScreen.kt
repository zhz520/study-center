package cn.zhzgo.study.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TextView
import io.noties.markwon.Markwon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocMarkdownScreen(onBack: () -> Unit) {
    var rawMarkdown by remember { 
        mutableStateOf("# Hello Markdown\n\nWelcome to **Jetpack Compose** Markdown viewer.\n\n- Support lists\n- Support links\n- Support code blocks") 
    }
    var isPreviewMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isPreviewMode) "Markdown 预览" else "Markdown 编辑", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { isPreviewMode = !isPreviewMode }) {
                        Icon(
                            if (isPreviewMode) Icons.Filled.Edit else Icons.Filled.Visibility, 
                            contentDescription = "Toggle Mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (isPreviewMode) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val markwon = remember { Markwon.create(context) }
                    
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            TextView(ctx).apply {
                                setTextColor(android.graphics.Color.BLACK)
                                textSize = 16f
                            }
                        },
                        update = { view ->
                            markwon.setMarkdown(view, rawMarkdown)
                        }
                    )
                }
            } else {
                OutlinedTextField(
                    value = rawMarkdown,
                    onValueChange = { rawMarkdown = it },
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    placeholder = { Text("输入 Markdown 文本...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}
