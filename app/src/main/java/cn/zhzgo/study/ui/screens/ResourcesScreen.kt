package cn.zhzgo.study.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import cn.zhzgo.study.data.Article
import cn.zhzgo.study.ui.viewmodels.ResourcesViewModel
import cn.zhzgo.study.R

private val categoryColors = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800),
    Color(0xFF9C27B0), Color(0xFFE91E63), Color(0xFF00BCD4),
    Color(0xFF607D8B), Color(0xFF795548)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    onNavigateToArticle: (String) -> Unit = {},
    onNavigateToCategory: (Int) -> Unit = {},
    onNavigateToContribution: () -> Unit = {},
    onNavigateToTool: (String) -> Unit = {},
    onNavigateToAllTools: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ResourcesViewModel = viewModel()

    val articles by viewModel.filteredArticles.collectAsState()
    val allArticles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val articleCategories by viewModel.articleCategories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val toolsViewModel: cn.zhzgo.study.ui.viewmodels.ToolsViewModel = viewModel()
    val userPreferences = remember { cn.zhzgo.study.data.UserPreferences(context) }
    val dashboardToolsIdsStr by userPreferences.dashboardTools.collectAsState(initial = "base_converter,calculator,bmi_calculator,date_calculator")
    val dashboardToolsIds = remember(dashboardToolsIdsStr) { dashboardToolsIdsStr.split(",").filter { it.isNotBlank() } }
    val dashboardToolsFlow = remember(dashboardToolsIds) { toolsViewModel.getToolsByIds(dashboardToolsIds) }
    val dashboardTools by dashboardToolsFlow.collectAsState(initial = emptyList())
    // Sort tools to match user's custom order
    val sortedDashboardTools = remember(dashboardToolsIds, dashboardTools) {
        dashboardToolsIds.mapNotNull { id -> dashboardTools.find { it.id == id } }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchData(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "资源中心",
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
            ExtendedFloatingActionButton(
                onClick = onNavigateToContribution,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("投稿") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.fetchData(context) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!isLoading && allArticles.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📚", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "暂无资源文章",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "管理员可在后台添加文章",
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
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Search Bar
                    item(key = "search") {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.updateSearch(it) }
                        )
                    }

                    // --- ChatGPT iOS Style Tools Entry ---
                    if (searchQuery.isBlank() && selectedCategory == null) {
                        item(key = "tools_entry") {
                            ToolsSection(
                                tools = sortedDashboardTools,
                                onToolClick = {
                                    toolsViewModel.incrementUsage(it.id)
                                    onNavigateToTool(it.route)
                                },
                                onMoreClick = onNavigateToAllTools
                            )
                        }
                    }
                    // -------------------------------------

                    // Category Filter Chips
                    if (articleCategories.isNotEmpty()) {
                        item(key = "categories") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedCategory == null,
                                        onClick = { viewModel.selectCategory(null) },
                                        label = { Text("全部") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            enabled = true,
                                            selected = selectedCategory == null
                                        )
                                    )
                                }
                                items(articleCategories) { category ->
                                    val idx = articleCategories.indexOf(category)
                                    val color = categoryColors[idx % categoryColors.size]
                                    FilterChip(
                                        selected = selectedCategory == category,
                                        onClick = {
                                            viewModel.selectCategory(
                                                if (selectedCategory == category) null else category
                                            )
                                        },
                                        label = { Text(category) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = color.copy(alpha = 0.15f),
                                            selectedLabelColor = color
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = if (selectedCategory == category) color.copy(alpha = 0.5f)
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            enabled = true,
                                            selected = selectedCategory == category
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Featured: first article with large card
                    if (articles.isNotEmpty() && searchQuery.isBlank() && selectedCategory == null) {
                        item(key = "featured") {
                            FeaturedArticleCard(
                                article = articles.first(),
                                onClick = {
                                    onNavigateToArticle(articles.first().id.toString())
                                }
                            )
                        }

                        // Rest of articles
                        items(
                            items = articles.drop(1),
                            key = { it.id }
                        ) { article ->
                            ArticleListCard(
                                article = article,
                                onClick = {
                                    onNavigateToArticle(article.id.toString())
                                },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    fadeOutSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
                            )
                        }
                    } else {
                        // All articles in list mode when searching/filtering
                        items(
                            items = articles,
                            key = { it.id }
                        ) { article ->
                            ArticleListCard(
                                article = article,
                                onClick = {
                                    onNavigateToArticle(article.id.toString())
                                },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    fadeOutSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
                            )
                        }

                        if (articles.isEmpty() && allArticles.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "没有找到匹配的文章",
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
    }
}

// ─── Search Bar ──────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        "搜索文章...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─── Featured Article Card ───────────────────────────────────────────────────

@Composable
private fun FeaturedArticleCard(
    article: Article,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Large Cover
            val cover = article.getCover()
            if (!cover.isNullOrEmpty()) {
                AsyncImage(
                    model = cover,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Pinned badge + Category chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (article.is_pinned == 1) {
                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "📌 置顶",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    article.category?.let { cat ->
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                article.summary?.let { summary ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.getTime() ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    article.view_count?.let { count ->
                        if (count > 0) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                Icons.Filled.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Article List Card ───────────────────────────────────────────────────────

@Composable
private fun ArticleListCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Text content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                // Pinned + Category tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (article.is_pinned == 1) {
                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "📌 置顶",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    article.category?.let { cat ->
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                article.summary?.let { summary ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.getTime() ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        fontSize = 11.sp
                    )
                    article.view_count?.let { count ->
                        if (count > 0) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                Icons.Filled.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Cover image
            val cover = article.getCover()
            if (!cover.isNullOrEmpty()) {
                AsyncImage(
                    model = cover,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}

// ─── Tools Section (Premium iOS ChatGPT Style) ───────────────────────────────

@Composable
private fun ToolsSection(
    tools: List<cn.zhzgo.study.data.ToolEntity>,
    onToolClick: (cn.zhzgo.study.data.ToolEntity) -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "快捷工具",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "查看全部",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onMoreClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val displayTools = tools.take(4)
            displayTools.forEach { tool ->
                PremiumToolItem(
                    tool = tool,
                    onClick = { onToolClick(tool) },
                    modifier = Modifier.weight(1f)
                )
            }
            // Fill empty spaces if less than 4 tools
            repeat(4 - displayTools.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PremiumToolItem(
    tool: cn.zhzgo.study.data.ToolEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val icon = cn.zhzgo.study.ui.screens.getIconForTool(tool.iconResName)
        
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp)) // Apple-like squircle effect simulation
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tool.name,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = tool.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            maxLines = 1,
            textDecoration = null,
            overflow = TextOverflow.Ellipsis
        )
    }
}
