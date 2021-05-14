package cloud.bjx.mm.android.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cloud.bjx.mm.android.RtcEngineProxy
import cloud.bjx.mm.android.UserSettings
import cloud.bjx.mm.android.bean.*
import cloud.bjx.mm.android.repo.ServiceFactory
import cloud.bjx.mm.android.utils.*
import cloud.bjx.mm.sdk.Constants
import cloud.bjx.mm.sdk.IRtcEngineEventHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class LiveRoomViewModelFactory(private val option: ChannelOption) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LiveRoomViewModel(option) as T
    }
}


class LiveRoomViewModel(private val option: ChannelOption) : ViewModel() {

    private val rtcEngine by lazy { RtcEngineProxy.get() }
    private val uid by lazy { UserSettings.getUserId() ?: "" }

    val channelName by lazy { "频道ID: ${option.channelId}" }
    val channelInfo by lazy { ObservableField<String>() }
    val rtmpDump by lazy { ObservableField<String>() }

    val channelJoinLiveData by lazy { MutableLiveData<Outcome<Boolean>>() }
    val channelLeaveLiveData by lazy { MutableLiveData<Boolean>() }
    val memberJoinLiveData by lazy { MutableLiveData<Member>() }
    val memberLeaveLiveData by lazy { MutableLiveData<String>() }
    val audioPublishStateLiveData by lazy { MutableLiveData<AudioPubState>() }
    val roleChangeLiveData by lazy { MutableLiveData<Outcome<Boolean>>() }
    val rtmpStateLiveData by lazy { MutableLiveData<String>() }
    val activeSpeakerLiveData by lazy { MutableLiveData<ActiveSpeaker>() }
    val audioRouteLiveData by lazy { MutableLiveData<Int>() }

    private var rtmpDumpJob: Job? = null

    private val mEventHandler = object : IRtcEngineEventHandler() {
        override fun onError(err: Int) {
            LogUtil.e(" ---> RtcEvent, onError: ${err.errorMessage()}")
            when (err) {
               Constants.ERR_INVALID_TOKEN -> {
                    channelJoinLiveData.value = Outcome.Failure("invalid token")
                }
                Constants.ERR_CHANNEL_JOIN -> {
                    channelJoinLiveData.value = Outcome.Failure("joinChannel fail")
                }
                Constants.ERR_SET_ROLE -> roleChangeLiveData.value = Outcome.Failure("修改角色失败")
            }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: String?) {
            LogUtil.i(" ---> RtcEvent, onJoinChannelSuccess, channel=$channel, uid=$uid")
            channelJoinLiveData.value = Outcome.Success(true)
            memberJoinLiveData.value = Member(this@LiveRoomViewModel.uid, option.roleBroadcaster)
            startRtmpDump()
        }

        override fun onLeaveChannel() {
            LogUtil.i(" ---> RtcEvent, onLeaveChannel")
            channelLeaveLiveData.value = true
            stopRtmpDump()
        }

        override fun onClientRoleChanged(oldRole: Int, newRole: Int) {
            LogUtil.i(" ---> RtcEvent, onClientRoleChanged, from $oldRole to $newRole")
            option.roleBroadcaster = newRole == Constants.CLIENT_ROLE_BROADCASTER
            setChannelInfo()
            roleChangeLiveData.value = Outcome.Success(option.roleBroadcaster)
            startRtmpDump()
        }

        override fun onUserJoined(uid: String?) {
            LogUtil.i(" ---> RtcEvent, onUserJoined, uid=$uid")
            uid?.let { memberJoinLiveData.value = Member(it, option.roleBroadcaster) }
        }

        override fun onUserOffline(uid: String?, reason: Int) {
            LogUtil.i(" ---> RtcEvent, onUserOffline, uid=$uid")
            uid?.let { memberLeaveLiveData.value = it }
        }

        override fun onAudioPublishStateChanged(channel: String?, oldState: Int, newState: Int) {
            LogUtil.i(" ---> RtcEvent, onAudioPublishStateChanged, channel=$String, oldState=$oldState, newState=$newState")
            if (channel == null) return
            audioPublishStateLiveData.value = AudioPubState(oldState, newState)
        }

        override fun onRtmpStreamingStateChanged(url: String?, state: Int) {
            val rtmpState = state.rtmpState()
            LogUtil.d("======> Rtmp state change to $rtmpState: $url")
            rtmpStateLiveData.value = "RTMP 状态: $rtmpState"
        }

        override fun onActiveSpeaker(speakerUid: String?, active: Boolean) {
            if (speakerUid == null) return
            activeSpeakerLiveData.value = ActiveSpeaker(speakerUid, active)
        }

        override fun onAudioRouteChanged(audioRoute: Int) {
            audioRouteLiveData.value = audioRoute
        }
    }

    init {
        LogUtil.d("----> channelId=${option.channelId}, autoSubscribe=${option.autoSubscribe}")
        if (option.modeLiveBroadcasting) {
            rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            if (option.roleBroadcaster) {
                rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
                rtcEngine.setDefaultAudioRouteToSpeakerphone(option.useSpeakerphone)
            } else {
                rtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
                rtcEngine.setDefaultAudioRouteToSpeakerphone(true)
            }
        } else {
            rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
        }
        rtcEngine.addHandler(mEventHandler)
        rtcEngine.setDefaultMuteAllRemoteAudioStreams(!option.autoSubscribe)
        rtcEngine.disableVideo()

        //val pushUrl = "rtmp://127.0.0.1:1935/live/${option.channelId}"
        //val pullUrl = "rtmp://192.168.88.222:1935/live/${option.channelId}"

        val pushUrl = "rtmp://106.14.122.122:1935/live/${option.channelId}"
        val pullUrl = "rtmp://106.14.122.122:1935/live/${option.channelId}"
        rtcEngine.setPublishStreamUrl(pushUrl, pullUrl) // 要加入channel之前调用

        setChannelInfo()
        joinChannel()
    }

    override fun onCleared() {
        super.onCleared()
        rtcEngine.removeHandler(mEventHandler)
    }

    private fun setChannelInfo() {
        channelInfo.set(if (option.roleBroadcaster) "角色: 主播" else "角色: 观众")
    }

    private fun joinChannel() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val uid = UserSettings.getUserId() ?: ""
                val bean = ServiceFactory.apiService.fetchToken(uid)
                LogUtil.i("fetchToken result: $bean")

                if (rtcEngine.joinChannel(bean.data.token, option.channelId, uid) != 0) {
                    channelJoinLiveData.value = Outcome.Failure("joinChannel fail")
                }
            } catch (e: Exception) {
                LogUtil.e("fetchToken error", e)
                channelJoinLiveData.value = Outcome.Failure("fetch token error")
            }
        }
    }

    fun setSpeakerOn() {
        when (val route = audioRouteLiveData.value ?: -1) {
            Constants.AUDIO_ROUTE_SPEAKERPHONE -> rtcEngine.setEnableSpeakerphone(false)
            Constants.AUDIO_ROUTE_EARPIECE -> rtcEngine.setEnableSpeakerphone(true)
            else -> LogUtil.w("Current audio route ${route.audioRoute()}")
        }
    }

    fun leaveChannel() {
        if (rtcEngine.leaveChannel() != 0) {
            channelLeaveLiveData.value = false
        }
    }

    fun muteLocalAudio(muted: Boolean) {
        if (option.roleBroadcaster) {
            rtcEngine.muteLocalAudioStream(muted)
        } else {
            LogUtil.w("Audience can not muteLocalAudio to $muted")
        }
    }

    fun muteRemoteAudio(uid: String, muted: Boolean) {
        rtcEngine.muteRemoteAudioStream(uid, muted)
    }

    fun muteAllRemoteAudio(muted: Boolean) {
        if (!option.roleBroadcaster) {
            ToastUtil.showShortToast("观众不可操作")
            return
        }
        rtcEngine.muteAllRemoteAudioStreams(muted)
    }

    fun grantRole(isBroadcaster: Boolean) {
        if (this.option.roleBroadcaster == isBroadcaster) {
            val error = if (isBroadcaster) "已经是主播" else "已经是观众"
            roleChangeLiveData.value = Outcome.Failure(error)
            return
        }
        val newRole =
            if (isBroadcaster) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        if (rtcEngine.setClientRole(newRole) != 0) {
            roleChangeLiveData.value = Outcome.Failure("GrantRole Failed")
        }
    }

    private fun startRtmpDump() {
        if (option.roleBroadcaster) return
//        rtmpDumpJob = viewModelScope.launch {
//            while (isActive) {
//                val vo = PlayerDump.getPlayerDumpInfo();
//                val sb = StringBuilder()
//                sb.append("load-cost:\t\t\t\t\t\t\t").append("${vo.loadCost} ms").append("\n")
//                sb.append("cache-duration:\t\t").append(formatDuration(vo.audioCacheDuration))
//                    .append("\n")
//                sb.append("cache-bytes:\t\t\t\t\t").append(formatSize(vo.audioCacheBytes))
//                    .append("\n")
//                sb.append("cache-packets:\t\t").append(vo.audioCachePackets).append("\n")
//                sb.append("tcp-speed:\t\t\t\t\t\t\t").append(formatSpeed(vo.tcpSpeed, 1000))
//                rtmpDump.set(sb.toString())
//                delay(1000)
//            }
//        }
    }

    private fun stopRtmpDump() {
        rtmpDumpJob?.cancel()
    }

    private fun formatDuration(duration: Long): String? {
        return if (duration >= 1000) {
            String.format(Locale.US, "%.2f s", duration.toFloat() / 1000)
        } else {
            String.format(Locale.US, "%d ms", duration)
        }
    }

    private fun formatSpeed(bytes: Long, elapsed_milli: Long): String? {
        if (elapsed_milli <= 0) {
            return "0 B/s"
        }
        if (bytes <= 0) {
            return "0 B/s"
        }
        val bytesPerSec = bytes.toFloat() * 1000f / elapsed_milli
        return when {
            bytesPerSec >= 1000 * 1000 -> String.format(
                Locale.US,
                "%.2f MB/s",
                bytesPerSec / 1000 / 1000
            )
            bytesPerSec >= 1000 -> String.format(Locale.US, "%.1f KB/s", bytesPerSec / 1000)
            else -> String.format(Locale.US, "%d B/s", bytesPerSec.toLong())
        }
    }

    private fun formatSize(bytes: Long): String? {
        return when {
            bytes >= 100 * 1000 -> String.format(
                Locale.US,
                "%.2f MB",
                bytes.toFloat() / 1000 / 1000
            )
            bytes >= 100 -> String.format(Locale.US, "%.1f KB", bytes.toFloat() / 1000)
            else -> String.format(Locale.US, "%d B", bytes)
        }
    }

    fun setAllUserVolume(volume: Int) {
        val res = rtcEngine.adjustPlaybackSignalVolume(volume)
        LogUtil.d("set all remote volume: $volume, res: $res")
    }

    fun setLocalRecordVolume(volume: Int) {
        val res = rtcEngine.adjustRecordingSignalVolume(volume)
        LogUtil.d("set local record volume: $volume, res: $res")
    }

}