package cn.zhzgo.study.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import cn.zhzgo.study.ui.components.*

import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.ui.text.withStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(
    subjectId: Int,
    subjectName: String,
    isFavorites: Boolean = false,
    onBack: () -> Unit,
    viewModel: QuestionViewModel = viewModel()
) {
    LaunchedEffect(subjectId, isFavorites) {
        viewModel.loadQuestions(subjectId, isFavorites)
    }

    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val submittedQuestions by viewModel.submittedQuestions.collectAsState()
    val aiExplanation by viewModel.aiExplanation.collectAsState()
    val isLoadingAi by viewModel.isLoadingAi.collectAsState()
    val aiRemaining by viewModel.aiRemaining.collectAsState()
    val favoriteQuestionIds by viewModel.favoriteQuestionIds.collectAsState()
    
    val currentQuestion = questions.getOrNull(currentIndex)
    // Fix reactivity: observe submittedQuestions directly
    val isSubmitted = currentQuestion?.let { submittedQuestions.contains(it.id) } ?: false
    val hasAnswer = currentQuestion?.let { 
        val answer = userAnswers[it.id]
        when (answer) {
            is String -> answer.isNotEmpty()
            is Collection<*> -> answer.isNotEmpty()
            else -> false
        }
    } ?: false
    
    var showAnswerSheet by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = subjectName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                     if (currentQuestion != null) {
                         val isFavorite = favoriteQuestionIds.contains(currentQuestion.id)
                         IconButton(onClick = { viewModel.toggleFavorite(currentQuestion.id) }) {
                             Icon(
                                 imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                 contentDescription = "收藏",
                                 tint = if (isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onBackground
                             )
                         }
                     }
                     if (!isFavorites) {
                         IconButton(onClick = { showResetConfirmDialog = true }) {
                            Icon(Icons.Default.Refresh, contentDescription = "重置进度", tint = MaterialTheme.colorScheme.onBackground)
                        }
                     }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        bottomBar = {
            if (questions.isNotEmpty()) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Answer Sheet Toggle
                        IconButton(onClick = { showAnswerSheet = true }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.GridView, contentDescription = "答题卡", tint = MaterialTheme.colorScheme.onSurface)
                                Text("答题卡", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        // Submit / Next Actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = viewModel::prevQuestion, 
                                enabled = currentIndex > 0,
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                            ) {
                                Text("上一题")
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            if (!isSubmitted) {
                                Button(
                                    onClick = viewModel::submitCurrentQuestion,
                                    enabled = hasAnswer, // Only enable if user selected an option
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.onSurface,
                                        contentColor = MaterialTheme.colorScheme.surface,
                                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        disabledContentColor = Color.Gray
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("提交")
                                }
                            } else {
                                Button(
                                    onClick = viewModel::nextQuestion,
                                    enabled = currentIndex < questions.size - 1,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.onSurface,
                                        contentColor = MaterialTheme.colorScheme.surface,
                                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        disabledContentColor = Color.Gray
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("下一题")
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (currentQuestion == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                if (questions.isEmpty()) CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
                else Text("暂无题目", color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Progress Bar (Minimalist)
                LinearProgressIndicator(
                    progress = { (currentIndex + 1) / questions.size.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Question Type Label
                Box(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when(currentQuestion.type) {
                            "single" -> "单选题"
                            "multiple" -> "多选题"
                            "judge" -> "判断题"
                            "fill" -> "填空题"
                            else -> "未知题型"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Question Content
                QuestionContent(question = currentQuestion)

                // Options based on type
                val options = viewModel.parseOptions(currentQuestion.options)
                
                when (currentQuestion.type) {
                    "single" -> {
                        SingleChoiceQuestion(
                            options = options,
                            selectedOption = userAnswers[currentQuestion.id] as? String,
                            onOptionSelected = { viewModel.selectSingleOption(currentQuestion.id, it) },
                            showAnswer = isSubmitted,
                            correctAnswer = viewModel.normalizeAnswer(currentQuestion.type, currentQuestion.answer)
                        )
                    }
                    "multiple" -> {
                        @Suppress("UNCHECKED_CAST")
                        val selectedSet = userAnswers[currentQuestion.id] as? Set<String> ?: emptySet()
                        MultiChoiceQuestion(
                            options = options,
                            selectedOptions = selectedSet,
                            onOptionToggle = { viewModel.toggleMultiOption(currentQuestion.id, it) },
                            showAnswer = isSubmitted,
                            correctAnswer = viewModel.normalizeAnswer(currentQuestion.type, currentQuestion.answer)
                        )
                    }
                    "judge" -> {
                        JudgeQuestion(
                            selectedOption = userAnswers[currentQuestion.id] as? String,
                            onOptionSelected = { viewModel.selectJudgeOption(currentQuestion.id, it) },
                            showAnswer = isSubmitted,
                            correctAnswer = viewModel.normalizeAnswer(currentQuestion.type, currentQuestion.answer)
                        )
                    }
                    "fill" -> {
                        FillBlankQuestion(
                            answerText = userAnswers[currentQuestion.id] as? String ?: "",
                            onAnswerChange = { viewModel.enterFillBlank(currentQuestion.id, it) },
                            showAnswer = isSubmitted,
                            correctAnswer = currentQuestion.answer
                        )
                    }
                    else -> {
                        Text("不支持的题型: ${currentQuestion.type}", color = Color.Red)
                    }
                }

                // Analysis Section
                if (isSubmitted) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // AI Tutor Button
                    if (aiExplanation == null) {
                         Button(
                             onClick = { viewModel.requestAiExplanation(currentQuestion) },
                             modifier = Modifier.fillMaxWidth().height(50.dp),
                             colors = ButtonDefaults.buttonColors(
                                 containerColor = MaterialTheme.colorScheme.surface,
                                 contentColor = MaterialTheme.colorScheme.onSurface
                             ),
                             border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                             shape = RoundedCornerShape(8.dp),
                             enabled = !isLoadingAi
                         ) {
                             if (isLoadingAi) {
                                 CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSurface)
                                 Spacer(modifier = Modifier.width(8.dp))
                                 Text("AI 分析中...", fontSize = 14.sp)
                             } else {
                                 Text("✨ AI 讲解", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                             }
                         }
                    }

                    // Standard Analysis
                    if (currentQuestion.analysis != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                             Text("解析", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                             Spacer(modifier = Modifier.height(8.dp))
                             MarkdownText(text = currentQuestion.analysis)
                        }
                    }
                    
                    // AI Explanation Result
                    if (aiExplanation != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                             Text("✨ AI 深度解析", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                             Spacer(modifier = Modifier.height(12.dp))
                             MarkdownText(text = aiExplanation!!)
                             
                             if (aiRemaining != null) {
                                 Spacer(modifier = Modifier.height(16.dp))
                                 Text(
                                     text = "今日剩余 AI 讲解次数: $aiRemaining 次", 
                                     fontSize = 12.sp, 
                                     color = Color.Gray,
                                     modifier = Modifier.align(Alignment.End)
                                 )
                             }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(30.dp))
                
                // Pagination Loading Indicator
                val isLoadingMore by viewModel.isLoadingMore.collectAsState()
                if (isLoadingMore) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onBackground)
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp)) // Bottom padding
            }
        }
        
        if (showAnswerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAnswerSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AnswerSheetDrawer(
                    questions = questions,
                    currentIndex = currentIndex,
                    answers = userAnswers,
                    onQuestionClick = { 
                        viewModel.jumpToQuestion(it)
                        showAnswerSheet = false
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showResetConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showResetConfirmDialog = false },
                title = { Text("重置进度") },
                text = { Text("确定要清空该科目的所有刷题记录吗？此操作不可恢复。") },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.resetProgress(subjectId)
                        android.widget.Toast.makeText(context, "进度已重置", android.widget.Toast.LENGTH_SHORT).show()
                        showResetConfirmDialog = false
                    }) {
                        Text("确定", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirmDialog = false }) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}
