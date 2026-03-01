package cn.zhzgo.study.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.Major
import cn.zhzgo.study.data.Subject
import cn.zhzgo.study.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DataCenterState(
    val isLoading: Boolean = false,
    val toastMessage: String? = null
)

class DataCenterViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.create(application)
    private val gson = Gson()

    private val _uiState = MutableStateFlow(DataCenterState())
    val uiState = _uiState.asStateFlow()

    private val _majors = MutableStateFlow<List<Major>>(emptyList())
    val majors = _majors.asStateFlow()

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects = _subjects.asStateFlow()

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun loadMajors() {
        viewModelScope.launch {
            try {
                val majorsList = apiService.getMajors()
                _majors.value = majorsList
            } catch (e: Exception) {
                // Ignore silent
            }
        }
    }

    fun exportData(onDataExtracted: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val progressList = apiService.getProgress()
                val jsonStr = gson.toJson(progressList)
                onDataExtracted(jsonStr)
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "导出失败: ${e.message}") }
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                apiService.resetProgress("all")
                _uiState.update { it.copy(toastMessage = "所有进度已成功清空！") }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "清空失败: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun importData(jsonString: String, targetSubjectId: Int?) {
        viewModelScope.launch {
            if (jsonString.isBlank()) {
                _uiState.update { it.copy(toastMessage = "请输入 JSON 数据") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Roughly parse it to a generic map or list to detect structure
                val listType = object : TypeToken<List<cn.zhzgo.study.data.ProgressResponse>>() {}.type
                
                try {
                    val progressList: List<cn.zhzgo.study.data.ProgressResponse> = gson.fromJson(jsonString, listType)
                    if (progressList.isNotEmpty() && progressList[0].question_id != 0) {
                        // Standard Modern Data Import (array of Progress objects)
                        var successCount = 0
                        for (item in progressList) {
                            if (item.question_id != 0) {
                                val req = cn.zhzgo.study.data.ProgressRequest(
                                    question_id = item.question_id,
                                    is_correct = item.is_correct == 1, // Assume tinyint maps 1 to true
                                    answer = item.answer
                                )
                                apiService.syncProgress(req)
                                successCount++
                            }
                        }
                        _uiState.update { it.copy(toastMessage = "成功导入 $successCount 条记录！") }
                        return@launch
                    }
                } catch (e: Exception) {
                    // Not a standard modern list. It's an old legacy JSON dictionary object.
                }

                // If executing here, it's either an old legacy JSON object {} or nested data.
                if (targetSubjectId == null) {
                    _uiState.update { it.copy(toastMessage = "检测到老版数据结构，Android 暂不支持跨科目盲导，请先选择目标科目。") }
                    return@launch
                }

                // We try legacy logic
                val mapType = object : TypeToken<Map<String, Any>>() {}.type
                val mapData: Map<String, Any> = gson.fromJson(jsonString, mapType)

                // If wrapper exists
                val finalMap = if (mapData.containsKey("version") && mapData.containsKey("data")) {
                    mapData["data"] as? Map<String, Any> ?: mapData
                } else {
                    mapData
                }

                val rawAnswers = if (finalMap.containsKey("answers")) {
                    finalMap["answers"] as? Map<String, Any> ?: finalMap
                } else {
                    finalMap
                }

                val items = mutableListOf<Map<String, Any>>()
                val hasZero = rawAnswers.containsKey("0")
                val offset = if (hasZero) 1 else 0

                rawAnswers.forEach { (key, value) ->
                    val kInt = key.toIntOrNull()
                    if (kInt != null && value is Map<*, *>) {
                        val ans = value["choice"] as? String 
                            ?: value["userAnswer"] as? String 
                            ?: value["answer"] as? String
                        
                        if (ans != null) {
                            val isCorrect = value["isCorrect"] as? Boolean ?: false
                            items.add(mapOf(
                                "original_id" to kInt + offset,
                                "is_correct" to isCorrect,
                                "answer" to ans
                            ))
                        }
                    }
                }

                if (items.isNotEmpty()) {
                    val payload = mapOf(
                        "subject_id" to targetSubjectId,
                        "items" to items
                    )
                    apiService.syncBatch(payload)
                    _uiState.update { it.copy(toastMessage = "导入成功！恢复了 ${items.size} 条记录到所选科目") }
                } else {
                    _uiState.update { it.copy(toastMessage = "未发现有效记录，请检查JSON格式") }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "数据解析错误: ${e.message}") }
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
