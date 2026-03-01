package cn.zhzgo.study.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import cn.zhzgo.study.data.UpdateData
import java.io.File

object UpdateManager {

    fun downloadApk(context: Context, updateData: UpdateData): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(updateData.download_url)
        
        val request = DownloadManager.Request(uri).apply {
            setTitle("Downloading Update")
            setDescription("Update downloading for version ${updateData.version_name}")
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "study_app_update_${updateData.version_name}.apk"
            )
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        return downloadManager.enqueue(request)
    }
}
