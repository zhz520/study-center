package cn.zhzgo.study.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.Major
import cn.zhzgo.study.data.Subject
import cn.zhzgo.study.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = RetrofitClient.create(application)

    private val _majors = MutableStateFlow<List<Major>>(emptyList())
    val majors = _majors.asStateFlow()

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects = _subjects.asStateFlow()
    
    // UI State
    private val _selectedMajor = MutableStateFlow<Major?>(null)
    val selectedMajor = _selectedMajor.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadMajors()
    }

    private fun loadMajors() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _majors.value = apiService.getMajors()
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectMajor(major: Major) {
        _selectedMajor.value = major
        loadSubjects(major.id)
    }

    fun clearSelectedMajor() {
        _selectedMajor.value = null
        _subjects.value = emptyList()
    }

    private fun loadSubjects(majorId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _subjects.value = apiService.getSubjects(majorId)
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
