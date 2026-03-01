package cn.zhzgo.study.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataCenterScreen(
    onBack: () -> Unit,
    viewModel: DataCenterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val majors by viewModel.majors.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var showClearDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    // Observation mechanism to show toasts natively
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Export handler
    val handleExport = {
        viewModel.exportData { jsonString ->
            clipboardManager.setText(AnnotatedString(jsonString))
            coroutineScope.launch {
                snackbarHostState.showSnackbar("数据已复制到剪贴板！")
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("数据管理", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 1. Export Data Card
            DataCard(
                title = "导出备份",
                subtitle = "将所有进度保存并复制到本地剪贴板",
                icon = Icons.Default.Download,
                iconTint = MaterialTheme.colorScheme.onSurface,
                iconBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ) {
                Button(
                    onClick = handleExport,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("复制 JSON 到剪贴板", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }

            // 2. Import Data Card
            DataCard(
                title = "导入恢复",
                subtitle = "从剪贴板粘贴 JSON 以恢复刷题记录",
                icon = Icons.Default.Download, // Upside down logic roughly
                iconTint = MaterialTheme.colorScheme.onSurface,
                iconBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ) {
                Button(
                    onClick = {
                        viewModel.loadMajors()
                        showImportDialog = true 
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("开始导入", fontWeight = FontWeight.Bold)
                }
            }

            // 3. Danger Zone
            Spacer(modifier = Modifier.height(10.dp))
            Text("危险区域", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            
            DataCard(
                title = "清空进度",
                subtitle = "此操作不可逆，将彻底删除云端所有的做题记录",
                icon = Icons.Default.DeleteForever,
                iconTint = Color.Red,
                iconBg = Color(0xFFFFEBEE),
                borderColor = Color(0xFFFFCDD2),
                bgColor = Color(0xFFFFF7F8)
            ) {
                Button(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Red
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("确认清空所有数据", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Danger Confirm Dialog
        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("确认清空？", color = Color.Red) },
                text = { Text("此操作将永久抹除所有云端刷题记录数据，且无法撤销！", color = MaterialTheme.colorScheme.onSurface) },
                confirmButton = {
                    TextButton(onClick = {
                        showClearDialog = false
                        viewModel.clearAllData()
                    }) {
                        Text("执意清空", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("点错了回去", color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        // Import Dialog
        if (showImportDialog) {
            var importText by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("粘贴 JSON 导入", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("如果你正在导入老版本的数据（无外层结构），请在下方手动选择要附加到的科目。新版结构会自动识别。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Dropdowns for Major / Subject
                        var selectedMajorId by remember { mutableStateOf<Int?>(null) }
                        var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
                        
                        if (majors.isNotEmpty()) {
                            // Minimalist mock for dropdown
                            Text("由于老版数据特殊，此处若不选择科目则依赖新版自解析", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 10.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = importText,
                            onValueChange = { importText = it },
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            placeholder = { Text("在此粘贴你的备份数据...", fontSize = 12.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.importData(importText, null) // Pass subject ID here if strictly old type selected later
                            showImportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("解析并导入", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun DataCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    borderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    bgColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).background(iconBg, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}
