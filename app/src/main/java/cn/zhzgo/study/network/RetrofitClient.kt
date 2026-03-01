package cn.zhzgo.study.network

import android.content.Context
import cn.zhzgo.study.data.RefreshRequest
import cn.zhzgo.study.data.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthInterceptor(private val userPreferences: UserPreferences) : Interceptor {
    @Volatile
    private var isRefreshing = false
    private val lock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            userPreferences.authToken.first()
        }
        
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        val response = chain.proceed(requestBuilder.build())
        
        // If 401 and this is NOT the refresh endpoint itself, try to refresh
        if (response.code == 401 && !chain.request().url.encodedPath.contains("/auth/refresh")) {
            synchronized(lock) {
                // Re-check token — another thread may have refreshed it
                val currentToken = runBlocking { userPreferences.authToken.first() }
                if (currentToken == token) {
                    // Token hasn't changed, we need to refresh
                    val refreshToken = runBlocking { userPreferences.refreshToken.first() }
                    if (!refreshToken.isNullOrEmpty()) {
                        try {
                            val refreshApi = createRefreshApi()
                            val refreshResponse = runBlocking {
                                refreshApi.refreshToken(RefreshRequest(refreshToken))
                            }
                            // Save new access token
                            runBlocking {
                                userPreferences.saveAuthToken(refreshResponse.token)
                            }
                            // Close the old response and retry with new token
                            response.close()
                            val newRequest = chain.request().newBuilder()
                                .header("Authorization", "Bearer ${refreshResponse.token}")
                                .build()
                            return chain.proceed(newRequest)
                        } catch (e: Exception) {
                            // Refresh failed, clear auth
                            runBlocking {
                                userPreferences.clearAuthToken()
                            }
                        }
                    } else {
                        // No refresh token, clear auth
                        runBlocking {
                            userPreferences.clearAuthToken()
                        }
                    }
                } else {
                    // Token was refreshed by another thread, retry with new token
                    response.close()
                    val newRequest = chain.request().newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                    return chain.proceed(newRequest)
                }
            }
        }
        return response
    }

    private fun createRefreshApi(): ApiService {
        return Retrofit.Builder()
            .baseUrl(RetrofitClient.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

object RetrofitClient {
    const val BASE_URL = "https://studyapi.zhzgo.cn/"

    fun create(context: Context): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val userPreferences = UserPreferences(context)

        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(userPreferences))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
