package cn.zhzgo.study.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import cn.zhzgo.study.data.Article
import cn.zhzgo.study.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ContributionViewModel : ViewModel() {
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _uploadUrl = MutableStateFlow<String?>(null)
    val uploadUrl: StateFlow<String?> = _uploadUrl.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    fun submitArticle(context: Context, title: String, content: String, category: String, coverUrl: String?) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            try {
                val api = RetrofitClient.create(context)
                val article = mapOf(
                    "title" to title,
                    "content" to content,
                    "content_markdown" to content,
                    "content_format" to "markdown",
                    "category" to category,
                    "cover_url" to coverUrl
                )
                val response = api.submitArticle(article)
                _successMessage.value = response.message
            } catch (e: Exception) {
                _error.value = e.message ?: "投稿失败，请重试"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun uploadFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isUploading.value = true
            _error.value = null
            try {
                val file = getFileFromUri(context, uri) ?: throw Exception("无法处理该文件")
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
                
                val api = RetrofitClient.create(context)
                val response = api.uploadImage(body)
                
                _uploadUrl.value = response.url
                // Clean up temp file
                file.delete()
            } catch (e: Exception) {
                _error.value = "上传失败: ${e.message}"
            } finally {
                _isUploading.value = false
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        val fileExtension = getFileExtension(context, uri)
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.$fileExtension")
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileExtension(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "jpg"
    }

    fun resetUploadUrl() {
        _uploadUrl.value = null
    }

    fun clearMessages() {
        _successMessage.value = null
        _error.value = null
    }
}
