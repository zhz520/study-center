package cn.zhzgo.study.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.zhzgo.study.data.Countdown
import cn.zhzgo.study.data.DailyContentResponse
import cn.zhzgo.study.data.UserStats
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush as ComposeBrush

@Composable
fun HomeScreen(
    onNavigateToSubject: (Int, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToData: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWebView: (String, String) -> Unit
) {
    val viewModel: HomeViewModel = viewModel()
    val dailyContent by viewModel.dailyContent.collectAsState()
    val countdowns by viewModel.countdowns.collectAsState()
    val externalLinks by viewModel.externalLinks.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showLogoutDialog by remember { mutableStateOf(false) }

    val greeting = remember { cn.zhzgo.study.utils.GreetingUtils.getGreeting() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { _ ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Header & User Info
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                        Text(
                            text = "$greeting $userName",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        val quote = dailyContent?.quote
                        if (!quote.isNullOrBlank()) {
                            Text(
                                text = quote,
                                fontSize = 13.sp,
                                color = Color.Gray,
                                lineHeight = 18.sp,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        } else {
                            Text(
                                text = "今天也是充满希望的一天",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            if (isLoading) {
                // Skeleton Loading
                item { ShimmerStatsRow() }
                item { ShimmerBlock(height = 120) }
                item { ShimmerBlock(height = 90) }
                item { ShimmerBlock(height = 100) }
            } else {

            // 2. User Stats
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HomeStatItem(value = "${userStats.active_days}", label = "活跃天数")
                    HomeStatItem(value = "${userStats.total_questions}", label = "刷题总数")
                    HomeStatItem(value = "${userStats.accuracy}%", label = "正确率")
                }
            }

            // 3. Daily Content (Native minimalist style)
            item {
                if (dailyContent != null) {
                    val contentItem = dailyContent
                    
                    val (typeLabel, contentText, sourceText) = when {
                        !contentItem?.proverb?.en.isNullOrBlank() -> Triple("每日谚语", contentItem!!.proverb!!.en ?: "", contentItem.proverb.zh ?: "")
                        !contentItem?.poetry?.line.isNullOrBlank() -> Triple("每日诗词", contentItem!!.poetry!!.line ?: "", contentItem.poetry.src ?: "")
                        !contentItem?.quote.isNullOrBlank() -> Triple("每日语录", contentItem!!.quote!!, "")
                        else -> Triple("每日一句", "每一天都是新开始", "")
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(typeLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = contentText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )
                        if (sourceText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = sourceText,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            // 4. Countdowns
            item {
                if (countdowns.isNotEmpty()) {
                    Column {
                        Text("倒计时", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(countdowns) { countdown ->
                                CountdownCard(countdown)
                            }
                        }
                    }
                }
            }

            // 5. External Links
            item {
                if (externalLinks.isNotEmpty()) {
                    Column {
                        Text("常用资源", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(12.dp))
                        val context = androidx.compose.ui.platform.LocalContext.current
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(externalLinks) { link ->
                                Box(
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(70.dp)
                                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            val finalUrl = if (!link.url.startsWith("http")) "https://${link.url}" else link.url
                                            onNavigateToWebView(finalUrl, link.name)
                                        }
                                        .padding(12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ListAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = link.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(text = link.url.replace("https://", "").replace("http://", ""), fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Tools Grid (Settings, Resources, etc.)
            item {
                Text("常用功能", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolCard(
                            icon = Icons.Default.BarChart,
                            label = "学情分析",
                            modifier = Modifier.weight(1f),
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            iconBgColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            onClick = onNavigateToStats
                        )
                        ToolCard(
                            icon = Icons.Default.Storage,
                            label = "数据中心",
                            modifier = Modifier.weight(1f),
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            iconBgColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            onClick = onNavigateToData
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolCard(
                            icon = Icons.Default.EmojiEvents,
                            label = "排行榜",
                            modifier = Modifier.weight(1f),
                            iconColor = MaterialTheme.colorScheme.onSurface,
                            iconBgColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            onClick = onNavigateToLeaderboard
                        )
                        if (userRole == "admin" || userRole == "demo") {
                            ToolCard(
                                icon = Icons.Default.AdminPanelSettings,
                                label = "后台管理",
                                modifier = Modifier.weight(1f),
                                iconColor = MaterialTheme.colorScheme.onPrimary,
                                iconBgColor = MaterialTheme.colorScheme.primary,
                                onClick = onNavigateToAdmin
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
            } // end else (isLoading)
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("退出登录") },
                text = { Text("确定要退出登录吗？") },
                confirmButton = {
                    TextButton(onClick = { 
                        showLogoutDialog = false 
                        viewModel.logout()
                        onNavigateToLogin()
                    }) {
                        Text("退出", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun HomeStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun CountdownCard(countdown: Countdown) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(90.dp)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = countdown.title ?: "考试",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${getDaysLeft(countdown.target_date)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(text = " 天", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
            }
        }
    }
}

@Composable
private fun ToolCard(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    iconColor: Color = MaterialTheme.colorScheme.onSurface,
    iconBgColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun getDaysLeft(dateStr: String?): Long {
    if (dateStr.isNullOrBlank()) return 0
    return try {
        // Handle "MM-DD" or "YYYY-MM-DD"
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        var target: LocalDate
        if (dateStr.length == 5 && dateStr.contains("-")) {
            // It's MM-DD format
            target = LocalDate.parse("${now.year}-$dateStr", formatter)
            // If the event has already passed this year, roll over to next year
            if (target.isBefore(now)) {
                target = target.plusYears(1)
            }
        } else {
            val safeDateStr = if (dateStr.length >= 10) dateStr.substring(0, 10) else dateStr
            target = LocalDate.parse(safeDateStr, formatter)
            if (target.isBefore(now)) {
                 // Try rollover if it makes sense, though exact year dates shouldn't normally rollover
                 // We will rollover for everything if it passed to match Vue behavior if it's recurring.
                 target = target.withYear(now.year)
                 if (target.isBefore(now)) target = target.plusYears(1)
            }
        }
        
        val diff = ChronoUnit.DAYS.between(now, target)
        if (diff < 0) 0 else diff
    } catch (e: Exception) {
        0
    }
}

// ─── Shimmer Loading Components ──────────────────────────────────────────────

@Composable
private fun shimmerBrush(): ComposeBrush {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.1f),
        Color.LightGray.copy(alpha = 0.3f)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    return ComposeBrush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim + 200f, 0f)
    )
}

@Composable
private fun ShimmerBlock(height: Int) {
    val brush = shimmerBrush()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
    )
}

@Composable
private fun ShimmerStatsRow() {
    val brush = shimmerBrush()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        repeat(3) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
        }
    }
}
