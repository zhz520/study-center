package cn.zhzgo.study.data

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val user: User
)

data class RefreshRequest(
    val refreshToken: String
)

data class RefreshResponse(
    val token: String,
    val user: User
)

data class LogoutRequest(
    val refreshToken: String
)

data class RegisterResponse(
    val message: String?
)

data class QQLoginRequest(
    val openid: String,
    val access_token: String
)

data class QQLoginResponse(
    val need_bind: Boolean?,
    val message: String?,
    val token: String?,
    val refreshToken: String?,
    val user: User?
)

data class QQBindRequest(
    val openid: String,
    val username: String? = null,
    val password: String? = null,
    val nickname: String? = null,
    val avatar_icon: String? = null
)

data class User(
    val id: Int,
    val username: String,
    val role: String,
    val avatar_icon: String? = null,
    val nickname: String? = null,
    val qq_openid: String? = null
)

data class Major(
    val id: Int,
    val name: String,
    val description: String?
)

data class Subject(
    val id: Int,
    val name: String,
    val major_id: Int,
    val icon: String?
)

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

data class DailyContentResponse(
    val quote: String?,
    val proverb: DualLanguageContent?,
    val poetry: PoetryContent?
)

data class DualLanguageContent(
    val en: String?,
    val zh: String?
)

data class PoetryContent(
    val line: String?,
    val src: String?
)

data class Countdown(
    val id: Int?,
    val title: String?,
    val target_date: String?
)

data class ExternalLink(
    val id: Int,
    val name: String,
    val url: String
)

data class HomeDataResponse(
    val countdowns: List<Countdown>,
    val externalLinks: List<ExternalLink>
)

data class UserStats(
    val active_days: Int,
    val total_questions: Int,
    val accuracy: Int
)

data class RecommendationResponse(
    val recommendations: List<Question>
)

data class ProgressRequest(
    val question_id: Int,
    val is_correct: Boolean,
    val answer: String
)

data class ProgressResponse(
    val question_id: Int,
    val is_correct: Int,
    val answer: String,
    val subject_id: Int
)

// Detailed Stats Models
data class SubjectStat(
    val subject_id: Int,
    val subject_name: String,
    val total: Int,
    val correct: Int,
    val accuracy: Int
)

data class TypeStat(
    val type: String,
    val total: Int,
    val correct: Int,
    val accuracy: Int
)

data class WeekDay(
    val day: String,
    val count: Int
)

data class DetailedStatsResponse(
    val subjectStats: List<SubjectStat>,
    val typeStats: List<TypeStat>,
    val weekHeatmap: List<WeekDay>,
    val streak: Int
)

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

data class ResourceCategory(
    val id: Int,
    val name: String,
    val iconRes: Int // Can be mapped to local icons or URLs
)

data class FavoriteQuestion(
    val id: Int,
    val question_id: Int,
    val title: String,
    val type: String,
    val favorite_time: String
)

data class FavoriteAddRequest(
    val question_id: Int
)

data class GenericMessageResponse(
    val message: String
)

data class UploadResponse(
    val url: String,
    val filename: String,
    val size: Long
)

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

data class UserProfileData(
    val id: Int,
    val username: String,
    val avatar_icon: String? = null,
    val role: String,
    val join_date: String? = null
)

data class UserProfileResponse(
    val user: UserProfileData,
    val articles: List<Article>,
    val dynamics: List<Comment>
)

data class CommentRequest(
    val content: String
)
