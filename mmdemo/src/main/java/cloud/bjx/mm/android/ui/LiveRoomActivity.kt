package cloud.bjx.mm.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cloud.bjx.mm.android.R
import cloud.bjx.mm.android.UserSettings
import cloud.bjx.mm.android.adapter.LiveRoomEventAdapter
import cloud.bjx.mm.android.adapter.LiveRoomMemberAdapter
import cloud.bjx.mm.android.bean.ChannelOption
import cloud.bjx.mm.android.bean.Member
import cloud.bjx.mm.android.databinding.ActivityLiveRoomBinding
import cloud.bjx.mm.android.utils.LogUtil
import cloud.bjx.mm.android.utils.Outcome
import cloud.bjx.mm.android.utils.ProgressHUD
import cloud.bjx.mm.android.utils.ToastUtil
import cloud.bjx.mm.android.viewmodel.LiveRoomViewModel
import cloud.bjx.mm.android.viewmodel.LiveRoomViewModelFactory
import cloud.bjx.mm.android.widget.SpaceItemDecoration
import cloud.bjx.mm.sdk.Constants
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener

class LiveRoomActivity : AppCompatActivity(), OnItemClickListener, SeekBar.OnSeekBarChangeListener {

    companion object {
        private const val KEY_CHANNEL_OPTION = "channel_option"
        fun start(context: Context, option: ChannelOption) {
            val intent = Intent(context, LiveRoomActivity::class.java)
            intent.putExtra(KEY_CHANNEL_OPTION, option)
            context.startActivity(intent)
        }
    }


    private val mUid = UserSettings.getUserId() ?: ""
    private lateinit var mViewMode: LiveRoomViewModel
    private lateinit var binding: ActivityLiveRoomBinding

    private val mMemberAdapter by lazy { LiveRoomMemberAdapter() }
    private val mEventAdapter by lazy { LiveRoomEventAdapter() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val option: ChannelOption = intent.getParcelableExtra(KEY_CHANNEL_OPTION)!!
        val factory = LiveRoomViewModelFactory(option)
        mViewMode = ViewModelProvider(this, factory).get(LiveRoomViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_live_room)
        binding.activity = this
        binding.viewModel = mViewMode
        binding.lifecycleOwner = this

        binding.textRtmpDump.visibility = if (option.roleBroadcaster) View.GONE else View.VISIBLE
        binding.recyclerViewMember.apply {
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(SpaceItemDecoration(4, 24, 20))
            layoutManager = GridLayoutManager(this@LiveRoomActivity, 4)
            adapter = mMemberAdapter
        }

        binding.recyclerViewEvent.apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(this@LiveRoomActivity)
            adapter = mEventAdapter
        }

        mMemberAdapter.setOnItemClickListener(this)
        binding.seekBarUserVolume.setOnSeekBarChangeListener(this)
        binding.seekBarRecordVolume.setOnSeekBarChangeListener(this)
        binding.btnSpeaker.isEnabled = option.roleBroadcaster

        observeChannelJoin()
        observeChannelLeave()
        observeUserJoin()
        observeUserLeave()
        observeAudioPubState()
        observeRoleChange()
        observeRtmpState()
        observeActiveSpeaker()
        observeAudioRoute()
    }

    fun actionLeave() {
        ProgressHUD.show(this, false)
        mViewMode.leaveChannel()
    }

    fun chooseRole(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_role, popup.menu)
        popup.setOnMenuItemClickListener {
            ProgressHUD.show(this, false)
            mViewMode.grantRole(it.itemId == R.id.action_role_broadcaster)
            true
        }
        popup.show()
    }

    fun actionClearEvent() {
        mEventAdapter.setNewInstance(null)
    }

    private fun observeChannelJoin() {
        mViewMode.channelJoinLiveData.observe(this, {
            if (it is Outcome.Failure) {
                ToastUtil.showShortToast(it.message)
                finish()
            }
        })
    }

    private fun observeChannelLeave() {
        mViewMode.channelLeaveLiveData.observe(this, {
            ProgressHUD.dismiss()
            if (!it) ToastUtil.showShortToast("Channel Leave Error")
            finish()
        })
    }

    private fun observeUserJoin() {
        mViewMode.memberJoinLiveData.observe(this, {
            LogUtil.d("-------> observeUserJoin: $it")
            if (!isMemberInChannel(it.uid)) {
                mMemberAdapter.addData(it)
            } else {
                LogUtil.w("${it.uid} is already in channel")
            }
            if (it.isMyself) {
                addChannelEvent("我加入直播间")
            } else {
                addChannelEvent("${it.uid}加入直播间")
            }
        })
    }

    private fun observeUserLeave() {
        mViewMode.memberLeaveLiveData.observe(this, Observer {
            val member = findMember(it) ?: return@Observer
            mMemberAdapter.remove(member)
            addChannelEvent("${member.uid}离开直播间")
        })
    }

    private fun observeAudioPubState() {
        mViewMode.audioPublishStateLiveData.observe(this, Observer {
            addChannelEvent(it.getEvent())
            val member: Member = findMyself() ?: return@Observer
            member.pubState = it.newState
            updateMemberAdapter(member)
        })
    }

    private fun observeRoleChange() {
        mViewMode.roleChangeLiveData.observe(this, {
            ProgressHUD.dismiss()
            when (it) {
                is Outcome.Failure -> ToastUtil.showShortToast(it.message)
                is Outcome.Success -> handleRoleChange(it.value)
            }
        })
    }

    private fun observeRtmpState() {
        mViewMode.rtmpStateLiveData.observe(this, { addChannelEvent(it) })
    }

    private fun observeActiveSpeaker() {
        mViewMode.activeSpeakerLiveData.observe(this, Observer {
            val member = findMember(it.speakerUid) ?: return@Observer
            member.voiceActive = it.active
            updateMemberAdapter(member)
        })
    }

    private fun observeAudioRoute() {
        mViewMode.audioRouteLiveData.observe(this, {
            when (it) {
                Constants.AUDIO_ROUTE_EARPIECE -> {
                    addChannelEvent("音频路由 => 听筒")
                    binding.btnSpeaker.text = "打开扬声器"
                    binding.btnSpeaker.isEnabled = true
                }
                Constants.AUDIO_ROUTE_SPEAKERPHONE -> {
                    addChannelEvent("音频路由 => 扬声器")
                    binding.btnSpeaker.text = "关闭扬声器"
                    binding.btnSpeaker.isEnabled = true
                }
                Constants.AUDIO_ROUTE_HEADSET -> {
                    addChannelEvent("音频路由 => 有线耳机")
                    binding.btnSpeaker.isEnabled = false
                    binding.btnSpeaker.text = "有线耳机"
                }
                Constants.AUDIO_ROUTE_BLUETOOTH -> {
                    addChannelEvent("音频路由 => 蓝牙耳机")
                    binding.btnSpeaker.isEnabled = false
                    binding.btnSpeaker.text = "蓝牙耳机"
                }
            }
        })
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val member = mMemberAdapter.getItemOrNull(position) ?: return
        LogUtil.d("===========> click member: $member")

        if (!member.isBroadcaster) {
            ToastUtil.showShortToast("观众不可操作")
            return
        }

        if (member.isMyself) {
            if (member.isPublished) {
                member.isMuted = !member.isMuted
                mViewMode.muteLocalAudio(member.isMuted)
            }
        } else {
            member.isMuted = !member.isMuted
            mViewMode.muteRemoteAudio(member.uid, member.isMuted)
        }
        updateMemberAdapter(member)
    }

    private fun addChannelEvent(event: String) {
        mEventAdapter.addData(event)
        binding.recyclerViewEvent.smoothScrollToPosition(mEventAdapter.itemCount - 1)
    }

    private fun isMemberInChannel(uid: String): Boolean {
        return mMemberAdapter.data.count { it.uid == uid } > 0
    }

    private fun findMyself(): Member? =
        mMemberAdapter.data.find { it.uid == mUid }

    private fun findMember(uid: String): Member? = mMemberAdapter.data.find { it.uid == uid }

    private fun updateMemberAdapter(member: Member) {
        val position = mMemberAdapter.getItemPosition(member)
        mMemberAdapter.notifyItemChanged(position)
    }

    private fun handleRoleChange(isBroadcaster: Boolean) {
        if (!isBroadcaster) {
            mViewMode.setSpeakerOn()
        }
        binding.btnSpeaker.isEnabled = isBroadcaster

        binding.textRtmpDump.visibility = if (isBroadcaster) View.GONE else View.VISIBLE
        addChannelEvent(if (isBroadcaster) "我变为主播" else "我变为观众")
        mMemberAdapter.data.forEach {
            it.isBroadcaster = isBroadcaster
        }
        mMemberAdapter.notifyDataSetChanged()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) return true
        return super.onKeyDown(keyCode, event)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    @SuppressLint("SetTextI18n")
    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (seekBar == null) return
        val volume = seekBar.progress
        when (seekBar.id) {
            binding.seekBarRecordVolume.id -> {
                binding.textRecordVolume.text = "本地音量: $volume"
                mViewMode.setLocalRecordVolume(volume)
            }
            binding.seekBarUserVolume.id -> {
                binding.textRemoteVolume.text = "远程音量: $volume"
                mViewMode.setAllUserVolume(volume)
            }
        }
    }

}