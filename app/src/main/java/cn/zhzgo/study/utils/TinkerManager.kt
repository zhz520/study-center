package cn.zhzgo.study.utils

import android.content.Context
import android.util.Log
import cn.zhzgo.study.data.UpdateData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Manages hotfix patch downloading and application.
 *
 * Note: Tinker has been removed due to incompatibility with AGP 9.0.
 * The patch download logic is preserved for future re-integration
 * with a compatible hotfix framework.
 */
object TinkerManager {

    private const val TAG = "TinkerManager"

    suspend fun downloadAndApplyPatch(context: Context, updateData: UpdateData) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting patch download: ${updateData.download_url}")
                val url = URL(updateData.download_url)
                val connection = url.openConnection()
                connection.connect()

                val patchDir = File(context.cacheDir, "tinker_patches")
                if (!patchDir.exists()) {
                    patchDir.mkdirs()
                }

                val patchFile = File(patchDir, "patch_${updateData.patch_version}.apk")

                val input = connection.getInputStream()
                val output = FileOutputStream(patchFile)
                val buffer = ByteArray(4096)
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }

                output.close()
                input.close()

                Log.d(TAG, "Patch downloaded to ${patchFile.absolutePath}")
                // TODO: Re-integrate with a hotfix framework compatible with AGP 9.0
                Log.w(TAG, "Tinker has been removed due to AGP 9.0 incompatibility. Patch downloaded but not applied.")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to download or apply patch", e)
            }
        }
    }
}
