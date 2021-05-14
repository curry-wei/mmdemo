package cloud.bjx.mm.android.ui

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cloud.bjx.mm.android.ACTION_LOGOUT
import cloud.bjx.mm.android.R
import cloud.bjx.mm.android.UserSettings
import cloud.bjx.mm.android.databinding.ActivitySettingsBinding
import cloud.bjx.mm.android.utils.LogUtil
import cloud.bjx.mm.android.utils.ToastUtil
import cloud.bjx.mm.android.utils.clipboardManager
import cloud.bjx.mm.sdk.Constants
import cloud.bjx.mm.sdk.RtcEngine

class SettingsActivity : AppCompatActivity() {

    val sdkVersion by lazy { RtcEngine.getSdkVersion() ?: "null" }
    //val sdkBuildDate by lazy { cloud.bjx.mm.sdk.BuildConfig.BUILD_DATE }
    val userId by lazy { UserSettings.getUserId() ?: "" }
    val appKey by lazy { UserSettings.getAppKey() ?: "" }
    val logLevel by lazy { ObservableField<String>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = "设置"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val binding: ActivitySettingsBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_settings)
        binding.activity = this
        binding.lifecycleOwner = this
        setLogLevel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    fun actionAppKey() {
        val clipData = ClipData.newPlainText("app-key", appKey)
        clipboardManager?.let {
            it.setPrimaryClip(clipData)
            ToastUtil.showShortToast("复制成功")
        }
    }

    fun actionLogout() {
        AlertDialog.Builder(this)
            .setTitle("警告")
            .setMessage("确定退出登录？")
            .setNegativeButton("取消", null)
            .setPositiveButton("确定") { _, _ -> logout() }
            .create()
            .show()
    }

    fun actionLogLevel(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_log_level, popup.menu)
        popup.setOnMenuItemClickListener {
            var level: Int = Constants.LOG_LEVEL_DEBUG
            when (it.itemId) {
                R.id.action_level_debug -> level = Constants.LOG_LEVEL_DEBUG
                R.id.action_level_info -> level = Constants.LOG_LEVEL_INFO
                R.id.action_level_warn -> level = Constants.LOG_LEVEL_WARN
                R.id.action_level_error -> level = Constants.LOG_LEVEL_ERROR
                R.id.action_level_none -> level = Constants.LOG_LEVEL_NONE
            }
            RtcEngine.setLogLevel(level)
            UserSettings.setLogLevel(level)
            setLogLevel()
            true
        }
        popup.show()
    }

    private fun setLogLevel() {
        when (RtcEngine.getLogLevel()) {
            Constants.LOG_LEVEL_DEBUG -> logLevel.set("DEBUG")
            Constants.LOG_LEVEL_INFO -> logLevel.set("INFO")
            Constants.LOG_LEVEL_WARN -> logLevel.set("WARN")
            Constants.LOG_LEVEL_ERROR -> logLevel.set("ERROR")
            Constants.LOG_LEVEL_NONE -> logLevel.set("NONE")
        }
    }

    private fun logout() {
        UserSettings.clear()
        LogUtil.d(" ====> logout, send logout broadcast")
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_LOGOUT))
        finish()
    }

}