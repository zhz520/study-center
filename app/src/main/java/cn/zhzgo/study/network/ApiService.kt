package cn.zhzgo.study.network

import cn.zhzgo.study.data.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: LoginRequest): RegisterResponse

    @POST("api/auth/qq-login")
    suspend fun qqLogin(@Body request: QQLoginRequest): retrofit2.Response<QQLoginResponse>

    @POST("api/auth/bind-qq")
    suspend fun bindQq(@Body request: QQBindRequest): retrofit2.Response<LoginResponse>

    @POST("api/auth/unbind-qq")
    suspend fun unbindQq(): GenericMessageResponse

    @GET("api/data/majors")
    suspend fun getMajors(): List<Major>

    @GET("api/data/subjects")
    suspend fun getSubjects(@Query("major_id") majorId: Int?): List<Subject>

    @GET("api/data/questions")
    suspend fun getQuestions(
        @Query("subject_id") subjectId: Int,
        @Query("type") type: String?,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): List<Question>

    @GET("api/data/daily-content/random")
    suspend fun getDailyContent(): DailyContentResponse

    @GET("api/public/home-data")
    suspend fun getHomeData(): HomeDataResponse

    @GET("api/data/user-stats")
    suspend fun getUserStats(): UserStats

    @GET("api/data/detailed-stats")
    suspend fun getDetailedStats(): DetailedStatsResponse

    @POST("api/data/progress")
    suspend fun syncProgress(@Body request: ProgressRequest)

    @DELETE("api/data/progress")
    suspend fun resetProgress(@Query("subject_id") subjectId: String)

    @POST("api/data/progress/batch")
    suspend fun syncBatch(@Body payload: @JvmSuppressWildcards Map<String, Any>)

    // Get User Progress
    @GET("api/data/progress")
    suspend fun getProgress(): List<ProgressResponse>

    // AI Explanation (question)
    @POST("api/data/ai-explain")
    suspend fun getAiExplanation(@Body request: @JvmSuppressWildcards Map<String, Any>): @JvmSuppressWildcards Map<String, Any>

    // AI Learning Analytics (stats screen)
    @POST("api/data/ai-analysis")
    suspend fun getAiLearningAnalysis(@Body request: @JvmSuppressWildcards Map<String, Any>): @JvmSuppressWildcards Map<String, Any>

    @POST("api/data/update-avatar")
    suspend fun updateAvatar(@Body request: @JvmSuppressWildcards Map<String, String>): @JvmSuppressWildcards Map<String, String>
    
    @Multipart
    @POST("api/upload")
    suspend fun uploadImage(@Part image: MultipartBody.Part): UploadResponse
    
    // Settings APIs
    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: @JvmSuppressWildcards Map<String, String>): @JvmSuppressWildcards Map<String, String>

    @POST("api/data/submit-rating")
    suspend fun submitRating(@Body request: @JvmSuppressWildcards Map<String, Any>): @JvmSuppressWildcards Map<String, String>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): RefreshResponse

    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest)

    // App Update Endpoint
    @GET("api/app/check_update")
    suspend fun checkUpdate(@Query("versionCode") versionCode: Int): UpdateResponse

    @GET("api/app/versions")
    suspend fun getAppVersionHistory(): AppVersionHistoryResponse

    // Resources & Favorites
    @GET("api/data/resources/categories")
    suspend fun getResourceCategories(): List<ResourceCategory>

    @GET("api/data/resources/articles")
    suspend fun getArticles(@Query("category") category: String? = null): List<Article>

    @GET("api/data/resources/articles/{id}")
    suspend fun getArticleDetail(@Path("id") id: Int): Article

    @GET("api/data/favorites")
    suspend fun getFavorites(): List<FavoriteQuestion>

    @GET("api/data/favorites/practice")
    suspend fun getFavoritePracticeQuestions(): List<Question>

    @POST("api/data/favorites")
    suspend fun addFavorite(@Body request: FavoriteAddRequest): GenericMessageResponse

    @DELETE("api/data/favorites/{id}")
    suspend fun removeFavorite(@Path("id") id: Int): GenericMessageResponse

    @DELETE("api/data/favorites/question/{questionId}")
    suspend fun removeFavoriteByQuestionId(@Path("questionId") questionId: Int): GenericMessageResponse

    // Comment APIs
    @GET("api/data/resources/articles/{id}/comments")
    suspend fun getComments(@Path("id") id: Int): List<Comment>

    @POST("api/data/resources/articles/{id}/comments")
    suspend fun postComment(@Path("id") id: Int, @Body request: CommentRequest): GenericMessageResponse

    @DELETE("api/data/comments/{id}")
    suspend fun deleteComment(@Path("id") id: Int): GenericMessageResponse

    // User Profile API
    @GET("api/data/user/{id}")
    suspend fun getUserProfile(@Path("id") id: String): UserProfileResponse

    // Article Submission API
    @POST("api/data/resources/articles")
    suspend fun submitArticle(@Body article: @JvmSuppressWildcards Map<String, Any?>): GenericMessageResponse
}
