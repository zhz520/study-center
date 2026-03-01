package cn.zhzgo.study.data

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
@Immutable
data class LoginRequest(
    val username: String,
    val password: String
)

@Immutable
data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val user: User
)

@Immutable
data class RefreshRequest(
    val refreshToken: String
)

@Immutable
data class RefreshResponse(
    val token: String,
    val user: User
)

@Immutable
data class LogoutRequest(
    val refreshToken: String
)

@Immutable
data class RegisterResponse(
    val message: String?
)

@Immutable
data class QQLoginRequest(
    val openid: String,
    val access_token: String
)

@Immutable
data class QQLoginResponse(
    val need_bind: Boolean?,
    val message: String?,
    val token: String?,
    val refreshToken: String?,
    val user: User?
)

@Immutable
data class QQBindRequest(
    val openid: String,
    val username: String? = null,
    val password: String? = null,
    val nickname: String? = null,
    val avatar_icon: String? = null
)

@Immutable
data class User(
    val id: Int,
    val username: String,
    val role: String,
    val avatar_icon: String? = null,
    val nickname: String? = null,
    val qq_openid: String? = null
)

@Immutable
data class Major(
    val id: Int,
    val name: String,
    val description: String?
)

@Immutable
data class Subject(
    val id: Int,
    val name: String,
    val major_id: Int,
    val icon: String?
)

@Immutable
data class Question(
    val id: Int,
    val subject_name: String?,
    val type: String, // "single", "multiple", "judge", "fill", "material", "sort"
    val content: String,
    val options: Any?, // Can be String (JSON), List, or Map. Handled in ViewModel.
    val answer: String?,
    val analysis: String?,
    val difficulty: Int?,
    val tags: Any?,
    val category: String?
)

@Immutable
data class DailyContentResponse(
    val quote: String?,
    val proverb: DualLanguageContent?,
    val poetry: PoetryContent?
)

@Immutable
data class DualLanguageContent(
    val en: String?,
    val zh: String?
)

@Immutable
data class PoetryContent(
    val line: String?,
    val src: String?
)

@Immutable
data class Countdown(
    val id: Int?,
    val title: String?,
    val target_date: String?
)

@Immutable
data class ExternalLink(
    val id: Int,
    val name: String,
    val url: String
)

@Immutable
data class HomeDataResponse(
    val countdowns: List<Countdown>,
    val externalLinks: List<ExternalLink>
)

@Immutable
data class UserStats(
    val active_days: Int,
    val total_questions: Int,
    val accuracy: Int
)

@Immutable
data class RecommendationResponse(
    val recommendations: List<Question>
)

@Immutable
data class ProgressRequest(
    val question_id: Int,
    val is_correct: Boolean,
    val answer: String
)

@Immutable
data class ProgressResponse(
    val question_id: Int,
    val is_correct: Int,
    val answer: String,
    val subject_id: Int
)

// Detailed Stats Models
@Immutable
data class SubjectStat(
    val subject_id: Int,
    val subject_name: String,
    val total: Int,
    val correct: Int,
    val accuracy: Int
)

@Immutable
data class TypeStat(
    val type: String,
    val total: Int,
    val correct: Int,
    val accuracy: Int
)

@Immutable
data class WeekDay(
    val day: String,
    val count: Int
)

@Immutable
data class DetailedStatsResponse(
    val subjectStats: List<SubjectStat>,
    val typeStats: List<TypeStat>,
    val weekHeatmap: List<WeekDay>,
    val streak: Int
)

@Immutable
data class Article(
    val id: Int,
    val title: String,
    val summary: String? = null,
    val content: String? = null,
    val cover_url: String? = null,
    val coverUrl: String? = null, // backwards compat alias
    val category: String? = null,
    val is_published: Int? = 1,
    val is_pinned: Int? = 0,
    val view_count: Int? = 0,
    val author_id: Int? = null,
    val author_name: String? = null,
    val author_avatar: String? = null,
    val content_markdown: String? = null,
    val content_format: String? = null,
    val publish_time: String? = null,
    val publishTime: String? = null // backwards compat alias
) {
    fun getCover(): String? = cover_url ?: coverUrl
    fun getTime(): String? = publish_time ?: publishTime
}

@Immutable
data class ResourceCategory(
    val id: Int,
    val name: String,
    val iconRes: Int // Can be mapped to local icons or URLs
)

@Immutable
data class FavoriteQuestion(
    val id: Int,
    val question_id: Int,
    val title: String,
    val type: String,
    val favorite_time: String
)

@Immutable
data class FavoriteAddRequest(
    val question_id: Int
)

@Immutable
data class GenericMessageResponse(
    val message: String
)

@Immutable
data class UploadResponse(
    val url: String,
    val filename: String,
    val size: Long
)

@Immutable
data class Comment(
    val id: Int,
    val article_id: Int,
    val user_id: Int,
    val content: String,
    val created_at: String,
    val username: String,
    val avatar_icon: String? = null,
    val article_title: String? = null // For Dynamics
)

@Immutable
data class UserProfileData(
    val id: Int,
    val username: String,
    val avatar_icon: String? = null,
    val role: String,
    val join_date: String? = null
)

@Immutable
data class UserProfileResponse(
    val user: UserProfileData,
    val articles: List<Article>,
    val dynamics: List<Comment>
)

@Immutable
data class CommentRequest(
    val content: String
)
