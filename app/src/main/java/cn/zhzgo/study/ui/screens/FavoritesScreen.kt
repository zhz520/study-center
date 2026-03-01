package cn.zhzgo.study.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.zhzgo.study.data.FavoriteQuestion
import cn.zhzgo.study.ui.viewmodels.FavoritesViewModel

// Type label & color helpers
private fun typeLabel(type: String?): String = when (type) {
    "single" -> "单选题"
    "multiple" -> "多选题"
    "judge" -> "判断题"
    "fill" -> "填空题"
    "text" -> "简答题"
    "sort" -> "排序题"
    else -> type ?: "题目"
}

private fun typeColor(type: String?): Color = when (type) {
    "single" -> Color(0xFF4CAF50)
    "multiple" -> Color(0xFF2196F3)
    "judge" -> Color(0xFFFF9800)
    "fill" -> Color(0xFF9C27B0)
    "text" -> Color(0xFFE91E63)
    "sort" -> Color(0xFF00BCD4)
    else -> Color(0xFF607D8B)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateToPracticeMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: FavoritesViewModel = viewModel()

    val allFavorites by viewModel.favorites.collectAsState()
    val filteredFavorites by viewModel.filteredFavorites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val typeStats by viewModel.typeStats.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<FavoriteQuestion?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchFavorites(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "题目收藏",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            if (allFavorites.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToPracticeMode,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始练习", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.fetchFavorites(context) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && allFavorites.isEmpty()) {
                cn.zhzgo.study.ui.components.SkeletonList(count = 5)
            } else if (!isLoading && allFavorites.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "暂无收藏的题目",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "在做题时点击 ☆ 即可收藏",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 96.dp // space for FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stats Header Card
                    item(key = "stats_header") {
                        StatsHeaderCard(
                            totalCount = allFavorites.size,
                            typeStats = typeStats
                        )
                    }

                    // Filter Chips
                    item(key = "filter_chips") {
                        FilterChipsRow(
                            typeStats = typeStats,
                            selectedType = selectedType,
                            onSelectType = { viewModel.selectType(it) }
                        )
                    }

                    // Question list
                    items(
                        items = filteredFavorites,
                        key = { it.id }
                    ) { item ->
                        FavoriteItemCard(
                            question = item,
                            onClick = onNavigateToPracticeMode,
                            onRemove = { showDeleteDialog = item },
                            modifier = Modifier.animateItem(
                                fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                fadeOutSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                        )
                    }

                    // Empty filter state
                    if (filteredFavorites.isEmpty() && allFavorites.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "该类型暂无收藏",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { targetQuestion ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = {
                Icon(
                    Icons.Filled.BookmarkRemove,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text("取消收藏") },
            text = { Text("确定要取消收藏这道题目吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeFavorite(context, targetQuestion.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("取消收藏")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }) {
                    Text("保留")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Stats Header Card ──────────────────────────────────────────────────────────

@Composable
private fun StatsHeaderCard(
    totalCount: Int,
    typeStats: Map<String, Int>
) {
    val primary = MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            primary,
                            primary.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$totalCount",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "道题已收藏",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (typeStats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        typeStats.entries.take(4).forEach { (type, count) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$count",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = typeLabel(type),
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Filter Chips ────────────────────────────────────────────────────────────────

@Composable
private fun FilterChipsRow(
    typeStats: Map<String, Int>,
    selectedType: String?,
    onSelectType: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        // "All" chip
        item {
            FilterChip(
                selected = selectedType == null,
                onClick = { onSelectType(null) },
                label = { Text("全部") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    enabled = true,
                    selected = selectedType == null
                )
            )
        }

        val types = listOf("single", "multiple", "judge", "fill", "text", "sort")
        items(types.filter { typeStats.containsKey(it) }) { type ->
            val count = typeStats[type] ?: 0
            FilterChip(
                selected = selectedType == type,
                onClick = { onSelectType(if (selectedType == type) null else type) },
                label = { Text("${typeLabel(type)} $count") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = typeColor(type).copy(alpha = 0.15f),
                    selectedLabelColor = typeColor(type)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (selectedType == type) typeColor(type).copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    enabled = true,
                    selected = selectedType == type
                )
            )
        }
    }
}

// ─── Favorite Item Card ──────────────────────────────────────────────────────────

@Composable
private fun FavoriteItemCard(
    question: FavoriteQuestion,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = typeColor(question.type)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator dot
            Box(
                modifier = Modifier
                    .size(4.dp, 36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = color.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = typeLabel(question.type),
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = question.title ?: "未知题目",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = question.favorite_time ?: "未知时间",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Filled.BookmarkRemove,
                    contentDescription = "取消收藏",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
