package cn.zhzgo.study.data

// App Update Models
data class UpdateResponse(
    val hasUpdate: Boolean,
    val updateType: String?, // "apk" or "patch"
    val data: UpdateData?
)

data class UpdateData(
    val id: Int,
    val version_name: String?,
    val version_code: Int?,
    val target_version_code: Int?,
    val patch_version: Int?,
    val download_url: String,
    val update_log: String?,
    val is_force: Int?,
    val md5: String?
)

data class AppVersionHistoryResponse(
    val data: List<AppVersionHistoryItem>
)

data class AppVersionHistoryItem(
    val version_code: Int,
    val version_name: String,
    val update_log: String?,
    val created_at: String?
)
