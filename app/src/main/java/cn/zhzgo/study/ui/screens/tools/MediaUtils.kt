package cn.zhzgo.study.ui.screens.tools

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream

object MediaUtils {

    /**
     * Copy a file from internal cache to public media storage (Downloads or Music) using MediaStore.
     * This ensures compatibility with Android 11+ Scoped Storage.
     */
    suspend fun saveFileToPublicStorage(
        context: Context,
        tempFile: File,
        destFileName: String,
        mimeType: String,
        relativePath: String = Environment.DIRECTORY_DOWNLOADS + "/ZhzgoStudy"
    ): Boolean = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, destFileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val collection = if (mimeType.startsWith("audio/")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        } else if (mimeType.startsWith("video/")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            else MediaStore.Downloads.EXTERNAL_CONTENT_URI
        }

        var uri: Uri? = null
        try {
            uri = resolver.insert(collection, contentValues)
            if (uri == null) return@withContext false

            resolver.openOutputStream(uri)?.use { outStream ->
                FileInputStream(tempFile).use { inStream ->
                    inStream.copyTo(outStream)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            uri?.let { resolver.delete(it, null, null) }
            return@withContext false
        }
    }
}
