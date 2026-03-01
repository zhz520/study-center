package cn.zhzgo.study.ui.screens.tools

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ImageCompressUiState(
    val originalBitmap: Bitmap? = null,
    val previewBitmap: Bitmap? = null,
    val isProcessing: Boolean = false,
    val quality: Float = 85f,
    val scalePercent: Float = 100f,
    val selectedFormatIndex: Int = 0
)

class ImageCompressViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ImageCompressUiState())
    val uiState = _uiState.asStateFlow()

    fun loadBitmap(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isProcessing = true) }
            try {
                val resolver = context.contentResolver
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

                // Safe decoding to prevent OOM
                options.inSampleSize = calculateInSampleSize(options, 2048, 2048)
                options.inJustDecodeBounds = false

                val bmp = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(originalBitmap = bmp, previewBitmap = bmp, scalePercent = 100f, isProcessing = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isProcessing = false) }
                }
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun updateScalePercent(scale: Float) {
        _uiState.update { it.copy(scalePercent = scale) }
    }

    fun applyScale() {
        val state = _uiState.value
        val original = state.originalBitmap ?: return
        
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isProcessing = true) }
            val scale = state.scalePercent
            if (scale < 100f) {
                val width = (original.width * (scale / 100f)).toInt().coerceAtLeast(1)
                val height = (original.height * (scale / 100f)).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(original, width, height, true)
                withContext(Dispatchers.Main) { 
                    _uiState.update { it.copy(previewBitmap = scaled, isProcessing = false) }
                }
            } else {
                withContext(Dispatchers.Main) { 
                    _uiState.update { it.copy(previewBitmap = original, isProcessing = false) }
                }
            }
        }
    }

    fun restoreOriginal() {
        _uiState.update { it.copy(previewBitmap = it.originalBitmap, scalePercent = 100f) }
    }

    fun updateQuality(q: Float) {
        _uiState.update { it.copy(quality = q) }
    }

    fun updateFormatIndex(index: Int) {
        _uiState.update { it.copy(selectedFormatIndex = index) }
    }

    fun saveImage(context: Context, onSuccess: () -> Unit, onError: () -> Unit) {
        val state = _uiState.value
        val bitmap = state.previewBitmap ?: return
        
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isProcessing = true) }
            val formatIndex = state.selectedFormatIndex
            val quality = state.quality.toInt()
            
            val compressFormat = when (formatIndex) {
                0 -> Bitmap.CompressFormat.JPEG
                1 -> Bitmap.CompressFormat.PNG
                2 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }
            val mimeType = when (formatIndex) {
                0 -> "image/jpeg"
                1 -> "image/png"
                2 -> "image/webp"
                else -> "image/jpeg"
            }
            val extension = when (formatIndex) {
                0 -> "jpg"
                1 -> "png"
                2 -> "webp"
                else -> "jpg"
            }
            val filename = "IMG_EDIT_${System.currentTimeMillis()}.$extension"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ZhzgoStudy")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            val resolver = context.contentResolver
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            
            var success = false
            if (imageUri != null) {
                try {
                    resolver.openOutputStream(imageUri)?.use { out -> 
                        val finalQuality = if (formatIndex == 1) 100 else quality
                        bitmap.compress(compressFormat, finalQuality, out) 
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(imageUri, contentValues, null, null)
                    }
                    success = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    resolver.delete(imageUri, null, null)
                }
            }
            
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isProcessing = false) }
                if (success) onSuccess() else onError()
            }
        }
    }
}
