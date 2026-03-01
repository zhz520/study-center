package cn.zhzgo.study.ui.screens.tools

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

suspend fun saveBitmapToGalleryHelper(context: Context, bitmap: Bitmap, formatIndex: Int, quality: Int): Boolean = withContext(Dispatchers.IO) {
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
    if (imageUri != null) {
        try {
            val outputStream: OutputStream? = resolver.openOutputStream(imageUri)
            val finalQuality = if (formatIndex == 1) 100 else quality
            outputStream?.use { out -> bitmap.compress(compressFormat, finalQuality, out) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            resolver.delete(imageUri, null, null)
            return@withContext false
        }
    }
    return@withContext false
}
