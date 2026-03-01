package cn.zhzgo.study.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            installApk(context, downloadId)
        }
    }

    private fun installApk(context: Context, downloadId: Long) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (statusIndex >= 0 && cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                if (uriIndex >= 0) {
                    val uriString = cursor.getString(uriIndex)
                    if (uriString != null) {
                        val downloadedUri = Uri.parse(uriString)
                        val path = downloadedUri.path ?: return
                        val file = File(path)
                        
                        // We might need to resolve the actual file path from content:// uri in newer Android versions
                        // But since we set setDestinationInExternalPublicDir, getting the file directly is sometimes tricky.
                        // A safer way is to use DownloadManager's getUriForDownloadedFile
                        val installUri = downloadManager.getUriForDownloadedFile(downloadId) ?: return

                        val installIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(installUri, "application/vnd.android.package-archive")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        try {
                            context.startActivity(installIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "安装失败: ${e.message}", Toast.LENGTH_LONG).show()
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        cursor.close()
    }
}
