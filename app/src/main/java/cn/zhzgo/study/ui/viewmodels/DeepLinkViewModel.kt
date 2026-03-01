package cn.zhzgo.study.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 保存从分享链接进入时的待打开文章 ID，登录后跳转到对应文章页。
 */
class DeepLinkViewModel : ViewModel() {
    private val _pendingArticleId = MutableStateFlow<Int?>(null)
    val pendingArticleId: StateFlow<Int?> = _pendingArticleId.asStateFlow()

    fun setPendingArticleId(id: Int?) {
        _pendingArticleId.value = id
    }

    fun consumePendingArticleId(): Int? {
        val id = _pendingArticleId.value
        _pendingArticleId.value = null
        return id
    }
}
