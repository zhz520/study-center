package cn.zhzgo.study.utils

import android.app.Activity
import android.content.Context
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import org.json.JSONObject

class QQLoginHelper(private val context: Context) {
    private var mTencent: Tencent? = null
    private val APP_ID = "102861787"

    init {
        try {
            mTencent = Tencent.createInstance(APP_ID, context.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("QQLoginHelper", "Tencent SDK init error: ${e.message}")
        }
    }

    fun login(activity: Activity, listener: QQLoginListener) {
        if (mTencent == null) {
            listener.onError(-1, "Tencent instance is null", null)
            return
        }
        
        val uiListener = object : IUiListener {
            override fun onComplete(response: Any?) {
                if (response == null) {
                    listener.onError(-1, "Response is null", null)
                    return
                }
                
                try {
                    val jsonObject = response as JSONObject
                    if (jsonObject.length() == 0) {
                        listener.onError(-1, "Response is empty", null)
                        return
                    }
                    
                    val openId = jsonObject.optString("openid")
                    val accessToken = jsonObject.optString("access_token")
                    val expiresIn = jsonObject.optString("expires_in")
                    
                    if (openId.isNotEmpty() && accessToken.isNotEmpty()) {
                        mTencent?.setOpenId(openId)
                        mTencent?.setAccessToken(accessToken, expiresIn)
                        
                        // Fetch user info after successful login
                        fetchUserInfo(listener, openId, accessToken)
                    } else {
                        listener.onError(-1, "Missing openid or access_token", null)
                    }
                } catch (e: Exception) {
                    listener.onError(-1, e.message ?: "Unknown error parsing response", null)
                }
            }

            override fun onError(uiError: UiError) {
                listener.onError(uiError.errorCode, uiError.errorMessage, uiError.errorDetail)
            }

            override fun onCancel() {
                listener.onCancel()
            }
            
            override fun onWarning(code: Int) {}
        }
        
        if (!mTencent!!.isSessionValid) {
            mTencent!!.login(activity, "all", uiListener)
        } else {
            // Already valid session, fetch info
            fetchUserInfo(listener, mTencent!!.openId, mTencent!!.accessToken)
        }
    }

    private fun fetchUserInfo(listener: QQLoginListener, openId: String, accessToken: String) {
        val userInfo = com.tencent.connect.UserInfo(context, mTencent?.qqToken)
        userInfo.getUserInfo(object : IUiListener {
            override fun onComplete(response: Any?) {
                val json = response as? JSONObject
                val nickname = json?.optString("nickname")
                val avatarUrl = (json?.optString("figureurl_qq_2") // 100x100
                    ?: json?.optString("figureurl_qq_1")) // 40x40
                    ?.replace("http://", "https://")
                
                listener.onSuccess(openId, accessToken, nickname, avatarUrl)
            }

            override fun onError(error: UiError) {
                // Return openid even if info fetch fails
                listener.onSuccess(openId, accessToken, null, null)
            }

            override fun onCancel() {
                listener.onSuccess(openId, accessToken, null, null)
            }

            override fun onWarning(p0: Int) {}
        })
    }
    
    fun logout(context: Context) {
        mTencent?.logout(context)
    }

    interface QQLoginListener {
        fun onSuccess(openId: String, accessToken: String, nickname: String?, avatarUrl: String?)
        fun onError(code: Int, message: String, detail: String?)
        fun onCancel()
    }
}
