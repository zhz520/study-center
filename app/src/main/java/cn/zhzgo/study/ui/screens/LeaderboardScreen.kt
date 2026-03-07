package cn.zhzgo.study.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.zhzgo.study.data.LeaderboardUser
import cn.zhzgo.study.ui.viewmodels.LeaderboardViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val viewModel: LeaderboardViewModel = viewModel()
    val totalRanking by viewModel.totalQuestionsRanking.collectAsState()
    val accuracyRanking by viewModel.accuracyRanking.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val subjects by viewModel.subjects.collectAsState()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadSubjects(context)
        viewModel.loadRankings(context)
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showSubjectMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tabs = listOf("刷题数量", "正确率")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("排行榜")
                        Spacer(modifier = Modifier.width(12.dp))
                        // Subject Selector
                        Surface(
                            onClick = { showSubjectMenu = true },
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), 
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subjects.find { it.first == selectedSubjectId }?.second ?: "全部科目",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 120.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    } 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            val currentList = if (selectedTabIndex == 0) totalRanking else accuracyRanking
            val isAccuracy = selectedTabIndex == 1
            if (!isLoading && error == null && currentList.isNotEmpty() && !currentUser.isNullOrBlank()) {
                val myUser = currentList.find { it.username == currentUser }
                val myRank = if (myUser != null) currentList.indexOf(myUser) + 1 else -1
                if (myUser != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            thickness = 0.5.dp
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LeaderboardListItem(
                                user = myUser,
                                rank = myRank,
                                isAccuracy = isAccuracy,
                                isCurrentUser = true
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ─── Segmented Tab ──────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    val bgColor by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        animationSpec = tween(200),
                        label = "tabBg"
                    )
                    Surface(
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = bgColor,
                        shadowElevation = if (isSelected) 1.dp else 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = title,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // ─── Content ──────────────────────
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.loadRankings(context) },
                modifier = Modifier.fillMaxSize()
            ) {

                if (isLoading) {
                    // Skeleton loading
                    LeaderboardSkeleton()
                } else if (error != null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.loadRankings(context) }) {
                            Text("重试")
                        }
                    }
                } else {
                    val currentList = if (selectedTabIndex == 0) totalRanking else accuracyRanking
                    val isAccuracy = selectedTabIndex == 1

                    if (currentList.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            // Top 3 podium
                            if (currentList.size >= 3) {
                                item {
                                    PodiumSection(
                                        top3 = currentList.take(3),
                                        isAccuracy = isAccuracy
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }

                            // Remaining list
                            val startIndex = if (currentList.size >= 3) 3 else 0
                            val remaining = currentList.drop(startIndex)
                            itemsIndexed(remaining) { index, user ->
                                val rank = startIndex + index + 1
                                LeaderboardListItem(user = user, rank = rank, isAccuracy = isAccuracy, index = index)
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    } else {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "暂无排行数据",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSubjectMenu) {
        ModalBottomSheet(
            onDismissRequest = { showSubjectMenu = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(
                    text = "选择科目",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp, top = 4.dp).align(Alignment.CenterHorizontally)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false).padding(bottom = 24.dp)
                ) {
                    items(subjects.size) { index ->
                        val (id, name) = subjects[index]
                        val isSelected = id == selectedSubjectId
                        
                        // Enforcing monochrome architecture rules
                        val bgColor = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f) else Color.Transparent
                        
                        Surface(
                            onClick = {
                                viewModel.selectSubject(id, context)
                                showSubjectMenu = false
                            },
                            color = bgColor,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "已选择",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
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

// ─── Top 3 Podium ────────────────────────────────────────────────────────────
@Composable
private fun PodiumSection(top3: List<LeaderboardUser>, isAccuracy: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        PodiumItem(user = top3[1], rank = 2, isAccuracy = isAccuracy, barHeight = 72)
        PodiumItem(user = top3[0], rank = 1, isAccuracy = isAccuracy, barHeight = 100)
        PodiumItem(user = top3[2], rank = 3, isAccuracy = isAccuracy, barHeight = 56)
    }
}

@Composable
private fun PodiumItem(user: LeaderboardUser, rank: Int, isAccuracy: Boolean, barHeight: Int) {
    val avatarSize = if (rank == 1) 56.dp else 46.dp

    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animated = true }
    
    val animatedHeight by animateDpAsState(
        targetValue = if (animated) barHeight.dp else 0.dp,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "barHeight"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        // Rank label
        Text(
            text = rank.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Avatar
        Box(contentAlignment = Alignment.TopCenter) {
            AsyncImage(
                model = user.avatar_icon
                    ?: "https://api.dicebear.com/9.x/fun-emoji/png?seed=${user.username}&size=120",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .border(
                        width = if (rank == 1) 2.dp else 1.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (rank == 1) 0.2f else 0.1f),
                        shape = CircleShape
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            if (rank == 1) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .size(26.dp)
                        .offset(x = 18.dp, y = (-6).dp)
                        .rotate(15f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Crown",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Name
        Text(
            text = user.nickname ?: user.username,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        // Score
        Text(
            text = if (isAccuracy) "${user.score}%" else "${user.score} 题",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Bar
        val gradientBrush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
            )
        )
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(animatedHeight)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(gradientBrush)
        )
    }
}

// ─── List Item (rank 4+) ─────────────────────────────────────────────────────
@Composable
private fun LeaderboardListItem(user: LeaderboardUser, rank: Int, isAccuracy: Boolean, isCurrentUser: Boolean = false, index: Int = 0) {
    val bgColor = if (isCurrentUser) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f) else Color.Transparent
    
    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animated = true }
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (animated) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = minOf(10, index) * 30),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isCurrentUser) 1f else animatedAlpha)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(vertical = 12.dp, horizontal = if (isCurrentUser) 20.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = rank.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(12.dp))
            // Avatar
            AsyncImage(
                model = user.avatar_icon
                    ?: "https://api.dicebear.com/9.x/fun-emoji/png?seed=${user.username}&size=120",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(12.dp))
            // Name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.nickname ?: user.username,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Score
            Text(
                text = if (isAccuracy) "${user.score}%" else "${user.score} 题",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        if (!isCurrentUser) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                thickness = 0.5.dp
            )
        }
    }
}

// ─── Skeleton Loading ────────────────────────────────────────────────────────
@Composable
private fun leaderboardShimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.05f),
        Color.LightGray.copy(alpha = 0.2f)
    )
    val transition = rememberInfiniteTransition(label = "lbShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lbShimmerTranslate"
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim + 200f, 0f)
    )
}

@Composable
private fun LeaderboardSkeleton() {
    val brush = leaderboardShimmerBrush()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Podium skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            PodiumSkeleton(brush, avatarSize = 46, barHeight = 72)
            PodiumSkeleton(brush, avatarSize = 56, barHeight = 100)
            PodiumSkeleton(brush, avatarSize = 46, barHeight = 56)
        }

        Spacer(modifier = Modifier.height(28.dp))

        // List skeleton rows
        repeat(6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.width(16.dp))
                // Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Name
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.width(20.dp))
                // Score
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
private fun PodiumSkeleton(brush: Brush, avatarSize: Int, barHeight: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        // Rank label placeholder
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(6.dp))
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(avatarSize.dp)
                .clip(CircleShape)
                .background(brush)
        )
        Spacer(modifier = Modifier.height(6.dp))
        // Name placeholder
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Score placeholder
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Bar placeholder
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(barHeight.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(brush)
        )
    }
}
