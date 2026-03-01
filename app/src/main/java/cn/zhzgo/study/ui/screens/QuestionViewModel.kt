package cn.zhzgo.study.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.Question
import cn.zhzgo.study.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

data class QuestionUiState(
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val userAnswers: Map<Int, Any> = emptyMap(),
    val submittedQuestions: Set<Int> = emptySet(),
    val aiExplanation: String? = null,
    val isLoadingAi: Boolean = false,
    val aiRemaining: Int? = null,
    val favoriteQuestionIds: Set<Int> = emptySet(),
    val isLoadingMore: Boolean = false,
    val isLastPage: Boolean = false,
    val parsedOptionsMap: Map<Int, Map<String, String>> = emptyMap()
)

class QuestionViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val PAGE_SIZE = 99999
    }

    private val apiService = RetrofitClient.create(application)

    private val _uiState = MutableStateFlow(QuestionUiState())
    val uiState = _uiState.asStateFlow()

    private var currentPage = 1
    private var currentSubjectId = 0
    private var isFavoritesMode = false

    private suspend fun parseOptionsAsync(options: Any?): Map<String, String> = withContext(Dispatchers.Default) {
        if (options == null) return@withContext emptyMap()
        try {
            if (options is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                options as Map<String, String>
            } else if (options is List<*>) {
                val map = mutableMapOf<String, String>()
                options.forEachIndexed { index, item ->
                    if (item is Map<*, *>) {
                        val content = item["content"]?.toString() ?: item["text"]?.toString() ?: item.toString()
                        val label = item["label"]?.toString() ?: (Char('A'.code + index)).toString()
                        map[label] = content
                    } else {
                        map[(Char('A'.code + index)).toString()] = item.toString()
                    }
                }
                map
            } else if (options is String) {
                try {
                    val mapType = object : TypeToken<Map<String, String>>() {}.type
                    Gson().fromJson<Map<String, String>>(options, mapType)
                } catch (e: Exception) {
                    try {
                        val listObjType = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val list: List<Map<String, Any>> = Gson().fromJson(options, listObjType)
                        val map = mutableMapOf<String, String>()
                        list.forEachIndexed { index, item ->
                            val content = item["content"]?.toString() ?: item["text"]?.toString() ?: item.toString()
                            val label = item["label"]?.toString() ?: (Char('A'.code + index)).toString()
                            map[label] = content
                        }
                        map
                    } catch (e2: Exception) {
                        try {
                            val listType = object : TypeToken<List<String>>() {}.type
                            val list: List<String> = Gson().fromJson(options, listType)
                            val map = mutableMapOf<String, String>()
                            list.forEachIndexed { index, item ->
                                map[(Char('A'.code + index)).toString()] = item
                            }
                            map
                        } catch (e3: Exception) {
                            emptyMap()
                        }
                    }
                }
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private suspend fun buildParsedOptionsMap(questions: List<Question>): Map<Int, Map<String, String>> = withContext(Dispatchers.Default) {
        val map = mutableMapOf<Int, Map<String, String>>()
        for (q in questions) {
            if (q.type == "single" || q.type == "multiple") {
                map[q.id] = parseOptionsAsync(q.options)
            }
        }
        map
    }

    fun loadQuestions(subjectId: Int, isFavorites: Boolean = false) {
        currentSubjectId = subjectId
        isFavoritesMode = isFavorites
        currentPage = 1
        
        _uiState.update { it.copy(isLoadingMore = false, isLastPage = false, currentIndex = 0, questions = emptyList()) }
        
        viewModelScope.launch {
            try {
                val questionsDeferred = async { 
                    try {
                        if (isFavoritesMode) apiService.getFavoritePracticeQuestions()
                        else apiService.getQuestions(subjectId, null, 1, PAGE_SIZE) 
                    } catch (e: Exception) { emptyList() }
                }
                val progressDeferred = async { 
                    try { apiService.getProgress() } catch(e: Exception) { emptyList() }
                }
                
                val result = questionsDeferred.await()
                val progressList = progressDeferred.await()
                val parsedMap = buildParsedOptionsMap(result)
                
                val userAnsMap = mutableMapOf<Int, Any>()
                val subSet = mutableSetOf<Int>()
                
                progressList.forEach { p ->
                    val matchCriteria = if (isFavoritesMode) true else p.subject_id == subjectId
                    if (matchCriteria) {
                        val q = result.find { it.id == p.question_id }
                        if (q != null) {
                            subSet.add(p.question_id)
                            if (q.type == "multiple") {
                                userAnsMap[p.question_id] = p.answer.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                            } else {
                                userAnsMap[p.question_id] = p.answer
                            }
                        }
                    }
                }
                
                var lastUnansweredIndex = 0
                for (i in result.indices) {
                    if (!subSet.contains(result[i].id)) {
                        lastUnansweredIndex = i
                        break
                    }
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        questions = result,
                        isLastPage = result.size < PAGE_SIZE,
                        currentIndex = lastUnansweredIndex,
                        userAnswers = userAnsMap,
                        submittedQuestions = subSet,
                        parsedOptionsMap = parsedMap
                    )
                }
                
                if (_uiState.value.favoriteQuestionIds.isEmpty()) loadFavorites()
            } catch (e: Exception) {
                _uiState.update { it.copy(questions = emptyList()) }
            }
        }
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                val faves = apiService.getFavorites()
                _uiState.update { it.copy(favoriteQuestionIds = faves.map { f -> f.question_id }.toSet()) }
            } catch (e: Exception) { }
        }
    }

    fun toggleFavorite(questionId: Int) {
        val current = _uiState.value.favoriteQuestionIds
        val isFavorite = current.contains(questionId)
        
        _uiState.update { it.copy(favoriteQuestionIds = if (isFavorite) current - questionId else current + questionId) }
        
        viewModelScope.launch {
             try {
                 if (isFavorite) {
                     apiService.removeFavoriteByQuestionId(questionId)
                 } else {
                     apiService.addFavorite(cn.zhzgo.study.data.FavoriteAddRequest(questionId))
                 }
             } catch (e: Exception) {
                 _uiState.update { it.copy(favoriteQuestionIds = current) }
             }
        }
    }
    
    fun selectSingleOption(questionId: Int, option: String) {
        if (isSubmitted(questionId)) return
        _uiState.update { it.copy(userAnswers = it.userAnswers + (questionId to option)) }
        val question = _uiState.value.questions.find { it.id == questionId }
        question?.let { submitQuestion(it) }
    }
    
    fun selectJudgeOption(questionId: Int, option: String) {
        if (isSubmitted(questionId)) return
        _uiState.update { it.copy(userAnswers = it.userAnswers + (questionId to option)) }
        val question = _uiState.value.questions.find { it.id == questionId }
        question?.let { submitQuestion(it) }
    }

    fun toggleMultiOption(questionId: Int, option: String) {
        if (isSubmitted(questionId)) return
        val currentAnswers = _uiState.value.userAnswers
        val existing = currentAnswers[questionId] as? Set<String> ?: emptySet()
        val newSet = if (existing.contains(option)) existing - option else existing + option
        _uiState.update { it.copy(userAnswers = currentAnswers + (questionId to newSet)) }
    }

    fun enterFillBlank(questionId: Int, text: String) {
        if (isSubmitted(questionId)) return
        _uiState.update { it.copy(userAnswers = it.userAnswers + (questionId to text)) }
    }

    fun submitCurrentQuestion() {
        val state = _uiState.value
        val currentQ = state.questions.getOrNull(state.currentIndex) ?: return
        submitQuestion(currentQ)
    }
    
    fun normalizeAnswer(questionType: String, answer: String?): String {
        var cleanAnswer = answer ?: ""
        try {
            if (cleanAnswer.startsWith("[") && cleanAnswer.endsWith("]")) {
                val listType = object : TypeToken<List<String>>() {}.type
                val list: List<String> = Gson().fromJson(cleanAnswer, listType)
                cleanAnswer = list.joinToString("")
            }
        } catch (e: Exception) {}

        if (questionType == "judge") {
            val upper = cleanAnswer.uppercase()
            if (cleanAnswer == "正确" || cleanAnswer == "对" || cleanAnswer == "√" || upper == "A" || upper == "TRUE") return "A"
            if (cleanAnswer == "错误" || cleanAnswer == "错" || cleanAnswer == "×" || upper == "B" || upper == "FALSE") return "B"
        }

        if (questionType != "fill" && questionType != "text" && questionType != "sort") {
            cleanAnswer = cleanAnswer.replace(Regex("[^A-Za-z]"), "").uppercase().toCharArray().sorted().joinToString("")
        }
        return cleanAnswer
    }

    private fun submitQuestion(question: Question) {
        if (isSubmitted(question.id)) return 
        
        _uiState.update { it.copy(submittedQuestions = it.submittedQuestions + question.id) }
        
        val userAnswerRaw = _uiState.value.userAnswers[question.id]
        val userAnswerStr = formatUserAnswer(userAnswerRaw)
        
        var normalizedUser = userAnswerStr
        if (question.type != "fill" && question.type != "text" && question.type != "sort") {
            normalizedUser = normalizedUser.replace(Regex("[^A-Za-z]"), "").uppercase().toCharArray().sorted().joinToString("")
        }
        val normalizedCorrect = normalizeAnswer(question.type, question.answer)
        val isCorrect = normalizedUser.equals(normalizedCorrect, ignoreCase = true)
        
        viewModelScope.launch {
            try {
                val request = cn.zhzgo.study.data.ProgressRequest(question.id, isCorrect, userAnswerStr)
                apiService.syncProgress(request) 
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isSubmitted(questionId: Int): Boolean {
        return _uiState.value.submittedQuestions.contains(questionId)
    }
    
    private fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoadingMore || state.isLastPage || isFavoritesMode) return
        _uiState.update { it.copy(isLoadingMore = true) }
        
        viewModelScope.launch {
            try {
                val nextPage = currentPage + 1
                val result = apiService.getQuestions(currentSubjectId, null, nextPage, PAGE_SIZE)
                if (result.isNotEmpty()) {
                    val newParsedMap = buildParsedOptionsMap(result)
                    _uiState.update { it.copy(
                        questions = it.questions + result,
                        parsedOptionsMap = it.parsedOptionsMap + newParsedMap,
                        isLastPage = result.size < PAGE_SIZE
                    ) }
                    currentPage = nextPage
                } else {
                    _uiState.update { it.copy(isLastPage = true) }
                }
            } catch (e: Exception) {
            } finally {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.currentIndex < state.questions.size - 1) {
            _uiState.update { it.copy(currentIndex = it.currentIndex + 1, aiExplanation = null) }
            if (!state.isLastPage && state.questions.size - state.currentIndex < 10) {
                loadNextPage()
            }
        }
    }

    fun prevQuestion() {
        if (_uiState.value.currentIndex > 0) {
            _uiState.update { it.copy(currentIndex = it.currentIndex - 1, aiExplanation = null) }
        }
    }
    
    fun jumpToQuestion(index: Int) {
        if (index in 0 until _uiState.value.questions.size) {
            _uiState.update { it.copy(currentIndex = index, aiExplanation = null) }
        }
    }
    
    fun requestAiExplanation(question: Question) {
        if (_uiState.value.aiExplanation != null) return
        
        _uiState.update { it.copy(isLoadingAi = true) }
        viewModelScope.launch {
            try {
                val userAnswerRaw = _uiState.value.userAnswers[question.id]
                val userAnswerStr = formatUserAnswer(userAnswerRaw)
                
                val optionsJson = if (question.options != null) {
                    if (question.options is String) question.options
                    else Gson().toJson(question.options)
                } else ""

                val request = mapOf(
                    "question" to question.content,
                    "options" to optionsJson,
                    "userAnswer" to userAnswerStr,
                    "correctAnswer" to normalizeAnswer(question.type, question.answer),
                    "type" to question.type
                )
                val response = apiService.getAiExplanation(request)
                
                _uiState.update { currentState -> 
                    var newState = currentState
                    if (response.containsKey("explanation")) {
                        newState = newState.copy(aiExplanation = response["explanation"] as String)
                    }
                    if (response.containsKey("remaining")) {
                        val rem = response["remaining"]
                        val remInt = if (rem is Number) rem.toInt() else if (rem is String) rem.toIntOrNull() else null
                        newState = newState.copy(aiRemaining = remInt)
                    }
                    newState
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(aiExplanation = "AI 分析请求失败，请稍后重试。") }
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoadingAi = false) }
            }
        }
    }

    fun resetProgress(subjectId: Int) {
        viewModelScope.launch {
            try {
                apiService.resetProgress(subjectId.toString())
                _uiState.update { QuestionUiState() }
                kotlinx.coroutines.delay(300)
                loadQuestions(subjectId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun formatUserAnswer(answer: Any?): String {
        return when (answer) {
            is String -> answer
            is Set<*> -> (answer as Set<String>).sorted().joinToString(",")
            else -> ""
        }
    }
}
