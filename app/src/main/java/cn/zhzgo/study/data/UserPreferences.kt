package cn.zhzgo.study.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_INFO = stringPreferencesKey("user_info") // Store simple JSON or just username if needed
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val AVATAR_ICON = stringPreferencesKey("avatar_icon") // Name of the selected Material Icon
        private val APPEARANCE = stringPreferencesKey("appearance") // "system", "light", "dark"
        private val PRIMARY_COLOR = stringPreferencesKey("primary_color") // Hex code
        private val NICKNAME = stringPreferencesKey("nickname")
        private val QQ_OPENID = stringPreferencesKey("qq_openid")
        private val QQ_AVATAR = stringPreferencesKey("qq_avatar")
        private val DASHBOARD_TOOLS = stringPreferencesKey("dashboard_tools")
        private val TOOLS_VIEW_MODE = stringPreferencesKey("tools_view_mode")
    }

    val authToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[AUTH_TOKEN]
        }

    val refreshToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[REFRESH_TOKEN]
        }

    val userName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_INFO]
        }

    val userRole: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ROLE]
        }

    val avatarIcon: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[AVATAR_ICON] ?: "Person"
        }

    val appearance: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[APPEARANCE] ?: "system"
        }

    val primaryColor: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PRIMARY_COLOR] ?: "#000000"
        }

    val nickname: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[NICKNAME]
        }

    val qqOpenId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[QQ_OPENID]
        }

    val qqAvatar: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[QQ_AVATAR]
        }
    val dashboardTools: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[DASHBOARD_TOOLS] ?: "base_converter,calculator,bmi_calculator,date_calculator"
        }

    val toolsViewMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[TOOLS_VIEW_MODE] ?: "grid"
        }


    suspend fun saveUser(user: cn.zhzgo.study.data.User) {
        context.dataStore.edit { preferences ->
            preferences[USER_INFO] = user.username
            preferences[USER_ROLE] = user.role
            preferences[AVATAR_ICON] = user.avatar_icon ?: "Person"
            user.nickname?.let { preferences[NICKNAME] = it } ?: preferences.remove(NICKNAME)
            user.qq_openid?.let { preferences[QQ_OPENID] = it } ?: preferences.remove(QQ_OPENID)
            
            // If the incoming avatar is a URL (QQ), save it as the QQ avatar too
            if (user.avatar_icon?.startsWith("http") == true && !user.avatar_icon.contains("dicebear")) {
                preferences[QQ_AVATAR] = user.avatar_icon
            }
        }
    }
    
    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_INFO] = name
        }
    }

    suspend fun saveUserRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ROLE] = role
        }
    }

    suspend fun setAvatarIcon(iconName: String) {
        context.dataStore.edit { preferences ->
            preferences[AVATAR_ICON] = iconName
        }
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }

    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN] = token
        }
    }

    suspend fun setAppearance(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[APPEARANCE] = theme
        }
    }

    suspend fun setPrimaryColor(colorHex: String) {
        context.dataStore.edit { preferences ->
            preferences[PRIMARY_COLOR] = colorHex
        }
    }

    suspend fun clearAuthToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(USER_INFO)
            preferences.remove(USER_ROLE)
            preferences.remove(AVATAR_ICON)
            preferences.remove(NICKNAME)
            preferences.remove(QQ_OPENID)
            preferences.remove(QQ_AVATAR)
        }
    }

    suspend fun setDashboardTools(toolsStr: String) {
        context.dataStore.edit { preferences ->
            preferences[DASHBOARD_TOOLS] = toolsStr
        }
    }

    suspend fun setToolsViewMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[TOOLS_VIEW_MODE] = mode
        }
    }

}