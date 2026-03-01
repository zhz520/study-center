package cn.zhzgo.study.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.zhzgo.study.data.Article
import cn.zhzgo.study.data.Comment
import cn.zhzgo.study.data.CommentRequest
import cn.zhzgo.study.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArticleDetailViewModel : ViewModel() {

    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isCommentsLoading = MutableStateFlow(false)
    val isCommentsLoading: StateFlow<Boolean> = _isCommentsLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchArticleDetail(context: Context, articleId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val api = RetrofitClient.create(context)
                val fetchedArticle = api.getArticleDetail(articleId)
                _article.value = fetchedArticle
                fetchComments(context, articleId)
            } catch (e: Exception) {
                _error.value = e.message ?: "加载文章失败"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchComments(context: Context, articleId: Int) {
        viewModelScope.launch {
            _isCommentsLoading.value = true
            try {
                val api = RetrofitClient.create(context)
                _comments.value = api.getComments(articleId)
            } catch (_: Exception) {
                // Ignore silent errors for comments
            } finally {
                _isCommentsLoading.value = false
            }
        }
    }

    fun postComment(context: Context, articleId: Int, content: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.create(context)
                val response = api.postComment(articleId, CommentRequest(content))
                fetchComments(context, articleId)
                onSuccess(response.message)
            } catch (e: Exception) {
                // Should probably show a toast or error state for posting
            }
        }
    }

    fun deleteComment(context: Context, commentId: Int, articleId: Int) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.create(context)
                api.deleteComment(commentId)
                fetchComments(context, articleId)
            } catch (_: Exception) {}
        }
    }
}
