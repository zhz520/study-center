package cn.zhzgo.study.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.Question
import cn.zhzgo.study.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

class QuestionViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val PAGE_SIZE = 99999
    }

    private val apiService = RetrofitClient.create(application)

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions = _questions.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    // Store answers by Question ID
    private val _userAnswers = MutableStateFlow<Map<Int, Any>>(emptyMap())
    val userAnswers = _userAnswers.asStateFlow()

    // Track which questions have been "Submitted"
    private val _submittedQuestions = MutableStateFlow<Set<Int>>(emptySet())
    val submittedQuestions = _submittedQuestions.asStateFlow()
    
    // AI Explanation
    private val _aiExplanation = MutableStateFlow<String?>(null)
    val aiExplanation = _aiExplanation.asStateFlow()
    
    private val _isLoadingAi = MutableStateFlow(false)
    val isLoadingAi = _isLoadingAi.asStateFlow()

    private val _aiRemaining = MutableStateFlow<Int?>(null)
    val aiRemaining = _aiRemaining.asStateFlow()

    // Favorites
    private val _favoriteQuestionIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteQuestionIds = _favoriteQuestionIds.asStateFlow()

    // Helper to parse options
    fun parseOptions(options: Any?): Map<String, String> {
        if (options == null) return emptyMap()
        return try {
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
                // Try to parse as Map first
                try {
                    val mapType = object : TypeToken<Map<String, String>>() {}.type
                    Gson().fromJson(options, mapType)
                } catch (e: Exception) {
                    // Try to parse as List of Objects
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
                            // Try as List of Strings
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

    // Loading More State
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()

    private var currentPage = 1
    private var isLastPage = false
    private var currentSubjectId = 0
    private var isFavoritesMode = false

    fun loadQuestions(subjectId: Int, isFavorites: Boolean = false) {
        currentSubjectId = subjectId
        isFavoritesMode = isFavorites
        currentPage = 1
        isLastPage = false
        _isLoadingMore.value = false
        
        viewModelScope.launch {
            try {
                val questionsDeferred = async { 
                    try {
                        if (isFavoritesMode) {
                            apiService.getFavoritePracticeQuestions()
                        } else {
                            apiService.getQuestions(subjectId, null, 1, PAGE_SIZE) 
                        }
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
                val progressDeferred = async { 
                    try { apiService.getProgress() } catch(e: Exception) { emptyList() }
                }
                
                val result = questionsDeferred.await()
                val progressList = progressDeferred.await()
                
                _questions.value = result
                if (result.size < PAGE_SIZE) isLastPage = true
                
                // Restore State
                val userAnsMap = mutableMapOf<Int, Any>()
                val subSet = mutableSetOf<Int>()
                progressList.forEach { p ->
                    // For favorites, progress might belong to various subject_ids so we match purely by question_id
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
                // Find the first unanswered question to resume progress
                var lastUnansweredIndex = 0
                for (i in result.indices) {
                    if (!subSet.contains(result[i].id)) {
                        lastUnansweredIndex = i
                        break
                    }
                }
                
                // If all questions in the first page are answered, we might want to load more
                // But for now, just jumping to the last unanswered index is sufficient.
                _currentIndex.value = lastUnansweredIndex
                _userAnswers.value = userAnsMap
                // First time load user favorites
                if (_favoriteQuestionIds.value.isEmpty()) {
                    loadFavorites()
                }

            } catch (e: Exception) {
                _questions.value = emptyList()
            }
        }
    }
    
    // Load Favorites
    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                val faves = apiService.getFavorites()
                _favoriteQuestionIds.value = faves.map { it.question_id }.toSet()
            } catch (e: Exception) {
                // fail silently
            }
        }
    }

    // Toggle Favorite
    fun toggleFavorite(questionId: Int) {
        val current = _favoriteQuestionIds.value
        val isFavorite = current.contains(questionId)
        
        // Optimistic UI update
        if (isFavorite) {
            _favoriteQuestionIds.value = current - questionId
        } else {
            _favoriteQuestionIds.value = current + questionId
        }
        
        viewModelScope.launch {
             try {
                 if (isFavorite) {
                     apiService.removeFavoriteByQuestionId(questionId)
                 } else {
                     apiService.addFavorite(cn.zhzgo.study.data.FavoriteAddRequest(questionId))
                 }
             } catch (e: Exception) {
                 // Revert on failure
                 val reverted = _favoriteQuestionIds.value
                 if (isFavorite) {
                     _favoriteQuestionIds.value = reverted + questionId
                 } else {
                     _favoriteQuestionIds.value = reverted - questionId
                 }
             }
        }
    }
    
    // Select Single Option
    fun selectSingleOption(questionId: Int, option: String) {
        if (isSubmitted(questionId)) return
        
        _userAnswers.update { current ->
            current + (questionId to option)
        }
        
        // Auto-Submit for Single Choice
        val question = _questions.value.find { it.id == questionId }
        if (question != null) {
            submitQuestion(question)
        }
    }
    
    // Select Judge Option
    fun selectJudgeOption(questionId: Int, option: String) {
        if (isSubmitted(questionId)) return
        
        _userAnswers.update { current ->
            current + (questionId to option)
        }
        
        // Auto-Submit for Judge
        val question = _questions.value.find { it.id == questionId }
        if (question != null) {
            submitQuestion(question)
        }
    }

    fun toggleMultiOption(questionId: Int, option: String) {
        if (isSubmitted(questionId)) return
        _userAnswers.update { current ->
            val existing = current[questionId] as? Set<String> ?: emptySet()
            val newSet = if (existing.contains(option)) existing - option else existing + option
            current + (questionId to newSet)
        }
    }

    fun enterFillBlank(questionId: Int, text: String) {
        if (isSubmitted(questionId)) return
        _userAnswers.update { current ->
            current + (questionId to text)
        }
    }

    fun submitCurrentQuestion() {
        val currentQ = _questions.value.getOrNull(_currentIndex.value) ?: return
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
        if (isSubmitted(question.id)) return // Don't submit twice
        
        _submittedQuestions.update { it + question.id }
        
        // Calculate correctness
        val userAnswerRaw = _userAnswers.value[question.id]
        val userAnswerStr = formatUserAnswer(userAnswerRaw)
        
        var normalizedUser = userAnswerStr
        if (question.type != "fill" && question.type != "text" && question.type != "sort") {
            normalizedUser = normalizedUser.replace(Regex("[^A-Za-z]"), "").uppercase().toCharArray().sorted().joinToString("")
        }
        val normalizedCorrect = normalizeAnswer(question.type, question.answer)

        // Simple check. Backend should ideally validate.
        val isCorrect = normalizedUser.equals(normalizedCorrect, ignoreCase = true)
        
        viewModelScope.launch {
            try {
                val request = cn.zhzgo.study.data.ProgressRequest(
                    question_id = question.id,
                    is_correct = isCorrect,
                    answer = userAnswerStr
                )
                apiService.syncProgress(request) 
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isSubmitted(questionId: Int): Boolean {
        return _submittedQuestions.value.contains(questionId)
    }
    
    private fun loadNextPage() {
        if (_isLoadingMore.value || isLastPage || isFavoritesMode) return
        _isLoadingMore.value = true
        
        viewModelScope.launch {
            try {
                val nextPage = currentPage + 1
                val result = apiService.getQuestions(currentSubjectId, null, nextPage, PAGE_SIZE)
                if (result.isNotEmpty()) {
                    _questions.update { current -> current + result }
                    currentPage = nextPage
                    if (result.size < PAGE_SIZE) isLastPage = true
                } else {
                    isLastPage = true
                }
            } catch (e: Exception) {
                // Fail silently for pagination
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun nextQuestion() {
        if (_currentIndex.value < _questions.value.size - 1) {
            _currentIndex.value += 1
            _aiExplanation.value = null // Reset AI on change
            
            // Preload next page if we are close to the end
            if (!isLastPage && _questions.value.size - _currentIndex.value < 10) {
                loadNextPage()
            }
        }
    }

    fun prevQuestion() {
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
            _aiExplanation.value = null
        }
    }
    
    fun jumpToQuestion(index: Int) {
        if (index in 0 until _questions.value.size) {
            _currentIndex.value = index
            _aiExplanation.value = null
        }
    }
    
    fun requestAiExplanation(question: Question) {
        if (_aiExplanation.value != null) return
        
        _isLoadingAi.value = true
        viewModelScope.launch {
            try {
                val userAnswerRaw = _userAnswers.value[question.id]
                val userAnswerStr = formatUserAnswer(userAnswerRaw)
                
                // Ensure options are serialized to JSON string if they are a Map or List
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
                if (response.containsKey("explanation")) {
                    _aiExplanation.value = response["explanation"] as String
                }
                if (response.containsKey("remaining")) {
                    val rem = response["remaining"]
                    if (rem is Number) _aiRemaining.value = rem.toInt()
                    else if (rem is String) _aiRemaining.value = rem.toIntOrNull()
                }
            } catch (e: Exception) {
                _aiExplanation.value = "AI 分析请求失败，请稍后重试。"
                e.printStackTrace()
            } finally {
                _isLoadingAi.value = false
            }
        }
    }

    fun resetProgress(subjectId: Int) {
        viewModelScope.launch {
            try {
                // Call the DELETE endpoint with subject_id query param
                apiService.resetProgress(subjectId.toString())
                
                // Clear state immediately to reflect UI
                _userAnswers.value = emptyMap<Int, Any>()
                _submittedQuestions.value = emptySet<Int>()
                _aiExplanation.value = null
                _currentIndex.value = 0
                
                // Add a tiny delay to ensure backend DB commits the deletion before we fetch again
                kotlinx.coroutines.delay(300)
                
                // Reload questions but force it to stay at 0
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
