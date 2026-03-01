package cn.zhzgo.study.ui.screens

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.zhzgo.study.data.*
import cn.zhzgo.study.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── ViewModel ───────────────────────────────────────────────────────────────
class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val api = RetrofitClient.create(application)

    private val _stats = MutableStateFlow<UserStats?>(null)
    val stats = _stats.asStateFlow()

    private val _detailed = MutableStateFlow<DetailedStatsResponse?>(null)
    val detailed = _detailed.asStateFlow()

    private val _aiAnalysis = MutableStateFlow<String?>(null)
    val aiAnalysis = _aiAnalysis.asStateFlow()

    private val _isLoadingAi = MutableStateFlow(false)
    val isLoadingAi = _isLoadingAi.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            try {
                _stats.value = api.getUserStats()
            } catch (_: Exception) {}
            try {
                _detailed.value = api.getDetailedStats()
            } catch (_: Exception) {}
        }
    }

    fun requestAiAnalysis() {
        val s = _stats.value ?: return
        val d = _detailed.value ?: return
        _isLoadingAi.value = true
        _aiAnalysis.value = null

        viewModelScope.launch {
            try {
                // Build structured data for the dedicated analytics endpoint
                val subjectStatsList = d.subjectStats.map { sub ->
                    mapOf(
                        "subject_name" to sub.subject_name,
                        "total" to sub.total,
                        "correct" to sub.correct,
                        "accuracy" to sub.accuracy
                    )
                }
                val typeStatsList = d.typeStats.map { ts ->
                    mapOf(
                        "type" to ts.type,
                        "total" to ts.total,
                        "correct" to ts.correct,
                        "accuracy" to ts.accuracy
                    )
                }

                val result = api.getAiLearningAnalysis(
                    mapOf(
                        "totalQuestions" to s.total_questions,
                        "accuracy" to s.accuracy,
                        "activeDays" to s.active_days,
                        "streak" to d.streak,
                        "subjectStats" to subjectStatsList,
                        "typeStats" to typeStatsList
                    )
                )

                val text = result["analysis"] as? String ?: "AI 分析暂不可用"
                _aiAnalysis.value = text
            } catch (e: Exception) {
                _aiAnalysis.value = "AI 分析请求失败: ${e.message}"
            } finally {
                _isLoadingAi.value = false
            }
        }

    }

    private fun typeLabel(t: String) = when (t) {
        "single" -> "单选题"
        "multiple" -> "多选题"
        "judge" -> "判断题"
        "fill" -> "填空题"
        "material" -> "材料题"
        "sort" -> "排序题"
        else -> t
    }
}

// ─── Screen ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onBack: () -> Unit) {
    val vm: StatsViewModel = viewModel()
    val stats by vm.stats.collectAsState()
    val detailed by vm.detailed.collectAsState()
    val aiAnalysis by vm.aiAnalysis.collectAsState()
    val isLoadingAi by vm.isLoadingAi.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("学情分析", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // ── Overview Metrics ──────────────────────────────────────────
            item {
                stats?.let { s ->
                    val d = detailed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OverviewCard(
                            modifier = Modifier.weight(1f),
                            value = "${s.accuracy}%",
                            label = "整体正确率",
                            color = accuracyColor(s.accuracy)
                        )
                        OverviewCard(
                            modifier = Modifier.weight(1f),
                            value = "${s.total_questions}",
                            label = "刷题总数",
                            color = Color(0xFF3B82F6)
                        )
                        OverviewCard(
                            modifier = Modifier.weight(1f),
                            value = "${d?.streak ?: 0}",
                            label = "连续打卡",
                            color = Color(0xFFF59E0B),
                            icon = "🔥"
                        )
                    }
                } ?: Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(32.dp))
                }
            }

            // ── Donut Chart ───────────────────────────────────────────────
            stats?.let { s ->
                item {
                    StatsCard(title = "正确率概览") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                DonutChart(
                                    percentage = s.accuracy / 100f,
                                    radius = 80.dp,
                                    color = accuracyColor(s.accuracy),
                                    strokeWidth = 18.dp
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${s.accuracy}%", fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                    Text("正确率", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            LegendItem(color = accuracyColor(s.accuracy), label = "正确")
                            LegendItem(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), label = "错误")
                        }
                    }
                }
            }

            // ── Weekly Heatmap ────────────────────────────────────────────
            detailed?.let { d ->
                if (d.weekHeatmap.isNotEmpty()) {
                    item {
                        StatsCard(title = "近7天活跃度") {
                            WeekHeatmap(data = d.weekHeatmap)
                        }
                    }
                }

                // ── Per-Subject Bar Chart ─────────────────────────────────
                if (d.subjectStats.isNotEmpty()) {
                    item {
                        StatsCard(title = "各科目正确率") {
                            Spacer(Modifier.height(8.dp))
                            d.subjectStats.forEach { sub ->
                                SubjectBar(stat = sub)
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }

                // ── Question Type Breakdown ───────────────────────────────
                if (d.typeStats.isNotEmpty()) {
                    item {
                        StatsCard(title = "题型掌握度") {
                            Spacer(Modifier.height(8.dp))
                            d.typeStats.forEach { ts ->
                                TypeBar(stat = ts)
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }

                // ── Streak Achievement Badge ──────────────────────────────
                if (d.streak >= 3) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(36.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("连续打卡 ${d.streak} 天！", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("坚持就是胜利，继续保持这个好习惯！", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }

            // ── AI Analysis ───────────────────────────────────────────────
            item {
                StatsCard(title = "AI 学情分析") {
                    Spacer(Modifier.height(8.dp))
                    if (aiAnalysis != null) {
                        Text(
                            text = aiAnalysis!!,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    Button(
                        onClick = { vm.requestAiAnalysis() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoadingAi
                    ) {
                        if (isLoadingAi) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("DeepSeek 分析中…")
                        } else {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (aiAnalysis == null) "生成 AI 学情分析" else "重新生成", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
        } // end Box
    }
}


// ─── Components ──────────────────────────────────────────────────────────────

@Composable
private fun StatsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            content()
        }
    }
}

@Composable
private fun OverviewCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color,
    icon: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Text(icon, fontSize = 18.sp)
            } else {
                Spacer(Modifier.height(2.dp))
            }
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SubjectBar(stat: SubjectStat) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stat.subject_name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${stat.accuracy}%  (${stat.correct}/${stat.total})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (stat.accuracy / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = accuracyColor(stat.accuracy),
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun TypeBar(stat: TypeStat) {
    val label = when (stat.type) {
        "single" -> "单选题"
        "multiple" -> "多选题"
        "judge" -> "判断题"
        "fill" -> "填空题"
        "material" -> "材料题"
        "sort" -> "排序题"
        else -> stat.type
    }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text("${stat.accuracy}%  ·  ${stat.total}题", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (stat.accuracy / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF6366F1),
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun WeekHeatmap(data: List<WeekDay>) {
    val maxCount = (data.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
    // Show last 7 calendar days even if no data
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Build a map of day -> count
        val map = data.associate { it.day.takeLast(5) to it.count } // MM-dd
        val days = (6 downTo 0).map { offset ->
            val d = java.time.LocalDate.now().minusDays(offset.toLong())
            val key = "%02d-%02d".format(d.monthValue, d.dayOfMonth)
            val dayLabel = "%02d/%02d".format(d.monthValue, d.dayOfMonth)
            val count = map[key] ?: 0
            Pair(dayLabel, count)
        }
        days.forEach { (label, count) ->
            val alpha = if (count == 0) 0.08f else (count.toFloat() / maxCount * 0.85f + 0.15f)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = Color(0xFF10B981).copy(alpha = alpha),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (count > 0) {
                        Text(
                            "$count",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alpha > 0.5f) MaterialTheme.colorScheme.surface else Color(0xFF10B981)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(label.takeLast(5), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

private fun accuracyColor(acc: Int) = when {
    acc >= 80 -> Color(0xFF10B981)
    acc >= 60 -> Color(0xFFF59E0B)
    else -> Color(0xFFEF4444)
}

@Composable
fun DonutChart(
    percentage: Float,
    radius: androidx.compose.ui.unit.Dp,
    color: Color,
    strokeWidth: androidx.compose.ui.unit.Dp
) {
    Canvas(modifier = Modifier.size(radius * 2)) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        drawCircle(color = Color(0xFFF1F5F9).copy(alpha = 0.2f), style = stroke)
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360 * percentage.coerceIn(0f, 1f),
            useCenter = false,
            style = stroke
        )
    }
}

