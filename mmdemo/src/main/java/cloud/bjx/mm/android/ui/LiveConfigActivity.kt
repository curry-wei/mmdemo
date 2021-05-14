package cloud.bjx.mm.android.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import cloud.bjx.mm.android.R
import cloud.bjx.mm.android.bean.ChannelOption
import cloud.bjx.mm.android.databinding.ActivityLiveConfigBinding
import cloud.bjx.mm.android.utils.ToastUtil
import cloud.bjx.mm.android.utils.checkId

class LiveConfigActivity : AppCompatActivity() {

    var channelId: String = ""
    var modeLiveBroadcasting: Boolean = true
    var roleBroadcaster: Boolean = true
        set(value) {
            if (!value) autoSubscribe.set(false)
            field = value
        }
    val autoSubscribe = ObservableBoolean(true)
    var useSpeaker: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "音频配置"

        val binding: ActivityLiveConfigBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_live_config)
        binding.activity = this
        binding.lifecycleOwner = this
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun actionJoin() {
        if (TextUtils.isEmpty(channelId)) {
            ToastUtil.showShortToast("ChannelId empty")
            return
        }

        if (!channelId.checkId()) {
            ToastUtil.showShortToast("ChannelId invalid")
            return
        }

        val option = ChannelOption(
            channelId,
            modeLiveBroadcasting,
            useSpeaker,
            roleBroadcaster,
            autoSubscribe.get()
        )
        LiveRoomActivity.start(this, option)
    }

}