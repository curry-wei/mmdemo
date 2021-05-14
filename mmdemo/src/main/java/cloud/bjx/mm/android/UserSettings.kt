package cloud.bjx.mm.android

import android.content.Context
import android.content.SharedPreferences
import cloud.bjx.mm.sdk.RtcEngine

object UserSettings {

    private const val PREF_NAME = "user_settings"

    private val sharedPref by lazy {
        ContextProvider.get().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    @Synchronized
    private fun getEditor(): SharedPreferences.Editor = sharedPref.edit()

    private const val APP_KEY = "app_key"
    private const val USER_ID = "user_id"
    private const val LOG_LEVEL = "log_level"

    fun isLogin(): Boolean {
        val userId = getUserId()
        val appKey = getAppKey()
        return appKey != null && userId != null
    }

    fun setAppKey(value: String) = getEditor().putString(APP_KEY, value).commit()
    fun getAppKey(): String? = sharedPref.getString(APP_KEY, null)

    fun setUserId(value: String) = getEditor().putString(USER_ID, value).commit()
    fun getUserId(): String? = sharedPref.getString(USER_ID, null)

    fun setLogLevel(value: Int) = getEditor().putInt(LOG_LEVEL, value).commit()
    fun getLogLevel(): Int = sharedPref.getInt(LOG_LEVEL, RtcEngine.getLogLevel())

    fun clear() = getEditor().clear().commit()
}