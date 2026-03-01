package cn.zhzgo.study.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.zhzgo.study.data.ToolEntity
import cn.zhzgo.study.data.UserPreferences
import cn.zhzgo.study.ui.viewmodels.ToolsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsListScreen(
    onNavigateToTool: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ToolsViewModel = viewModel()
    val allTools by viewModel.allTools.collectAsState()
    val userPreferences = remember { UserPreferences(context) }
    
    val currentViewModeStr by userPreferences.toolsViewMode.collectAsState(initial = "grid")
    val dashboardToolsStr by userPreferences.dashboardTools.collectAsState(initial = "base_converter,calculator,bmi_calculator,date_calculator")
    
    val isGridView = currentViewModeStr == "grid"
    val dashboardToolsList = dashboardToolsStr.split(",").filter { it.isNotBlank() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("全部小工具", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                userPreferences.setToolsViewMode(if (isGridView) "list" else "grid")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = "Toggle View"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        val toolsByCategory = allTools.groupBy { it.category }

        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                toolsByCategory.forEach { (category, tools) ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = getCategoryName(category),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp, top = if (category == toolsByCategory.keys.first()) 0.dp else 16.dp)
                        )
                    }
                    items(tools) { tool ->
                    val isDashboardTool = dashboardToolsList.contains(tool.id)
                    ToolCard(
                        tool = tool,
                        isDashboardTool = isDashboardTool,
                        onClick = {
                            viewModel.incrementUsage(tool.id)
                            onNavigateToTool(tool.route)
                        },
                        onToggleDashboard = {
                            coroutineScope.launch {
                                val newList = dashboardToolsList.toMutableList()
                                if (isDashboardTool) {
                                    newList.remove(tool.id)
                                    userPreferences.setDashboardTools(newList.joinToString(","))
                                } else {
                                    newList.add(tool.id)
                                    userPreferences.setDashboardTools(newList.joinToString(","))
                                }
                            }
                        }
                    )
                }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                toolsByCategory.forEach { (category, tools) ->
                    item {
                        Text(
                            text = getCategoryName(category),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp, top = if (category == toolsByCategory.keys.first()) 0.dp else 16.dp)
                        )
                    }
                    items(tools) { tool ->
                    val isDashboardTool = dashboardToolsList.contains(tool.id)
                    ToolListCard(
                        tool = tool,
                        isDashboardTool = isDashboardTool,
                        onClick = {
                            viewModel.incrementUsage(tool.id)
                            onNavigateToTool(tool.route)
                        },
                        onToggleDashboard = {
                            coroutineScope.launch {
                                val newList = dashboardToolsList.toMutableList()
                                if (isDashboardTool) {
                                    newList.remove(tool.id)
                                    userPreferences.setDashboardTools(newList.joinToString(","))
                                } else {
                                    newList.add(tool.id)
                                    userPreferences.setDashboardTools(newList.joinToString(","))
                                }
                            }
                        }
                    )
                }
                }
            }
        }
    }
}

@Composable
fun ToolCard(
    tool: ToolEntity,
    isDashboardTool: Boolean,
    onClick: () -> Unit,
    onToggleDashboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val icon = getIconForTool(tool.iconResName)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp)) // iOS squircle
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tool.name,
                modifier = Modifier.size(32.dp).align(Alignment.Center),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Pin Icon for dashboard tool
            IconButton(
                onClick = onToggleDashboard,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .offset(x = 4.dp, y = (-4).dp)
            ) {
                Icon(
                    imageVector = if (isDashboardTool) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = "Toggle Dashboard Pin",
                    tint = if (isDashboardTool) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = tool.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ToolListCard(
    tool: ToolEntity,
    isDashboardTool: Boolean,
    onClick: () -> Unit,
    onToggleDashboard: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = getIconForTool(tool.iconResName)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tool.name,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = tool.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onToggleDashboard) {
                Icon(
                    imageVector = if (isDashboardTool) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = "Toggle Dashboard Pin",
                    tint = if (isDashboardTool) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

fun getIconForTool(iconName: String): ImageVector {
    return when (iconName) {
        "icon_base_converter" -> Icons.Filled.Build
        "icon_calculator" -> Icons.Filled.Calculate
        "icon_bmi" -> Icons.Filled.MonitorWeight
        "icon_date" -> Icons.Filled.DateRange
        "icon_random" -> Icons.Filled.Shuffle
        "icon_color" -> Icons.Filled.ColorLens
        "icon_password" -> Icons.Filled.VpnKey
        "icon_text" -> Icons.Filled.TextFields
        "icon_timer" -> Icons.Filled.Timer
        "icon_swap" -> Icons.Filled.SwapHoriz
        "icon_clock" -> Icons.Filled.Schedule
        "icon_family" -> Icons.Filled.FamilyRestroom
        "icon_image" -> Icons.Filled.Image
        "icon_auto_fix_high" -> Icons.Filled.AutoFixHigh
        "icon_brush" -> Icons.Filled.Brush
        "icon_picture_as_pdf" -> Icons.Filled.PictureAsPdf
        "icon_collections" -> Icons.Filled.Collections
        "icon_integration_instructions" -> Icons.Filled.IntegrationInstructions
        "icon_music_note" -> Icons.Filled.MusicNote
        "icon_video_file" -> Icons.Filled.VideoFile
        "icon_graphic_eq" -> Icons.Filled.GraphicEq
        "icon_pin" -> Icons.Filled.Pin
        "icon_security" -> Icons.Filled.Security
        "icon_key" -> Icons.Filled.Key
        "icon_code" -> Icons.Filled.Code
        "icon_http" -> Icons.Filled.Http
        else -> Icons.Filled.Build
    }
}


@Composable
fun getCategoryName(category: String): String {
    return when (category) {
        "calc" -> "计算工具"
        "health", "life" -> "生活与健康"
        "time" -> "时间与日期"
        "general" -> "通用工具"
        "dev" -> "开发工具"
        "security" -> "安全与密码"
        "text" -> "文本工具"
        "image" -> "图像工具"
        "document" -> "文档处理"
        "media" -> "影音工具"
        else -> "其他工具"
    }
}
