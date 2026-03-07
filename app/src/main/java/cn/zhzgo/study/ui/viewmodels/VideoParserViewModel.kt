package cn.zhzgo.study.ui.viewmodels

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.VideoParseRequest
import cn.zhzgo.study.data.VideoParseResult
import cn.zhzgo.study.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VideoParserViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.create(application)

    private val _inputUrl = MutableStateFlow("")
    val inputUrl: StateFlow<String> = _inputUrl

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _result = MutableStateFlow<VideoParseResult?>(null)
    val result: StateFlow<VideoParseResult?> = _result

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun updateUrl(url: String) {
        _inputUrl.value = url
        _error.value = null
    }

    fun parseVideo() {
        val url = _inputUrl.value.trim()
        if (url.isEmpty()) {
            _error.value = "请粘贴短视频分享链接"
            return
        }

        _isLoading.value = true
        _error.value = null
        _result.value = null

        viewModelScope.launch {
            try {
                val response = apiService.parseVideo(VideoParseRequest(url))
                if (response.success && response.data != null) {
                    _result.value = response.data
                } else {
                    _error.value = response.error ?: "解析失败，请检查链接是否正确"
                }
            } catch (e: Exception) {
                _error.value = "网络请求失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveToGallery(url: String, defaultFilename: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            
            // Add headers to prevent 403 errors
            val userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"
            request.addRequestHeader("User-Agent", userAgent)
            
            // Extract extension more robustly from URL path
            val path = Uri.parse(url).path ?: ""
            var extension = path.substringAfterLast('.', "").lowercase()
            
            // If path doesn't yield a clear extension, try MimeTypeMap or fallback
            if (extension.isEmpty() || extension.length > 5) {
                extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(url)
            }
            
            // Special handling for Kuaishou/common patterns
            if (extension.isEmpty()) {
                if (url.contains("webp")) extension = "webp"
                else if (url.contains("jpg") || url.contains("jpeg")) extension = "jpg"
                else if (url.contains("png")) extension = "png"
            }

            val mimeType = if (!extension.isNullOrEmpty()) {
                android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            } else null

            // Final filename determination
            val finalFilename = if (extension.isNullOrEmpty()) {
                if (url.contains(".mp4") || defaultFilename.contains("video")) {
                    if (defaultFilename.endsWith(".mp4")) defaultFilename else "$defaultFilename.mp4"
                } else {
                    if (defaultFilename.endsWith(".jpg")) defaultFilename else "$defaultFilename.jpg"
                }
            } else {
                if (defaultFilename.contains('.')) defaultFilename else "$defaultFilename.$extension"
            }

            request.setTitle("正在下载内容")
                .setDescription(finalFilename)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            // Determine destination based on type
            val destinationDir: String
            val folderName: String
            
            if (finalFilename.lowercase().endsWith(".mp4") || (mimeType != null && mimeType.startsWith("video/"))) {
                destinationDir = Environment.DIRECTORY_MOVIES
                folderName = "电影/Movies"
                request.setMimeType(mimeType ?: "video/mp4")
            } else {
                destinationDir = Environment.DIRECTORY_PICTURES
                folderName = "图片/Pictures"
                request.setMimeType(mimeType ?: "image/jpeg")
            }

            request.setDestinationInExternalPublicDir(destinationDir, finalFilename)

            val downloadManager = getApplication<Application>().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            
            // Provide clear feedback on where the file is being saved
            Toast.makeText(getApplication(), "开始下载，将保存至：$folderName/$finalFilename", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(getApplication(), "下载失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearResult() {
        _result.value = null
        _error.value = null
        _inputUrl.value = ""
    }
}
