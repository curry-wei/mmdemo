package cloud.bjx.mm.android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import cloud.bjx.mm.android.R
import cloud.bjx.mm.android.RtcEngineProxy
import cloud.bjx.mm.android.bean.VideoConfigBean
import cloud.bjx.mm.android.databinding.ActivityLiveVideoBinding
import cloud.bjx.mm.android.fulive.renderer.OnRendererStatusListener
import cloud.bjx.mm.android.fulive.ui.control.BeautyControlView
import cloud.bjx.mm.android.utils.*
import cloud.bjx.mm.android.viewmodel.LiveVideoViewModel
import cloud.bjx.mm.android.viewmodel.LiveVideoViewModelFactory
import cloud.bjx.mm.sdk.Constants.*
import cloud.bjx.mm.sdk.VideoCanvas
import com.faceunity.FURenderer
import com.google.android.flexbox.FlexboxLayout
import java.util.*
import java.util.concurrent.CountDownLatch

open class LiveVideoActivity : AppCompatActivity(), FURenderer.OnFUDebugListener,
    FURenderer.OnTrackingStatusChangedListener, OnRendererStatusListener {

    companion object {
        private const val KEY_VIDEO_CONFIG = "video_config"
        const val REQUEST_CODE = 100
        fun startActivityForResult(context: Activity, config: VideoConfigBean) {
            val intent = Intent(context, LiveVideoActivity::class.java)
            intent.putExtra(KEY_VIDEO_CONFIG, config)
            context.startActivityForResult(intent, REQUEST_CODE)
        }

    }


    private lateinit var mViewMode: LiveVideoViewModel
    private lateinit var mBinding: ActivityLiveVideoBinding

    private val mLocalVideoCanvas by lazy { VideoCanvas(this) }
    private val mRemoteVideoCanvasMap: MutableMap<String, VideoCanvas> by lazy { mutableMapOf() }

    private var mAudioEnabled = true
    private var mVideoEnabled = true

    lateinit var mFURenderer: FURenderer

    lateinit var mVideoConfig: VideoConfigBean

    private lateinit var remotePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        buildFULiveBeauty()
        mVideoConfig = intent.getParcelableExtra(KEY_VIDEO_CONFIG)!!
        val factory = LiveVideoViewModelFactory(mVideoConfig, mFURenderer)
        mViewMode = ViewModelProvider(this, factory).get(LiveVideoViewModel::class.java)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_live_video)
        mBinding.activity = this
        mBinding.viewModel = mViewMode
        mBinding.lifecycleOwner = this

        observeChannelJoin()
        observeChannelLeave()
        observePeerJoin()
        observePeerLeave()
        observePublishState()
        observeRemoteVideoAdd()
        observeAudioRoute()

        setFULiveBeautyListener()
        remotePath = getBaseVideoRecordPath() + "/room_" + mVideoConfig.channelId + ".mp4"
    }

    override fun onStart() {
        super.onStart()
        mFURenderer.onSurfaceCreated()
        mFURenderer.setBeautificationOn(true)
    }

    override fun onResume() {
        super.onResume()
        mBinding.fuBeautyControl.onResume()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mBinding.fuBeautyControl.isShown) {
            mBinding.fuBeautyControl.hideBottomLayoutAnimator()
        }
        return super.onTouchEvent(event)
    }

    override fun finish() {
        val countDownLatch = CountDownLatch(1)
        mFURenderer.queueEvent {
            mFURenderer.onSurfaceDestroyed()
            countDownLatch.countDown()
        }
        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        super.finish()
    }

    private fun buildFULiveBeauty() {
        mFURenderer = FURenderer.Builder(this)
            .maxFaces(4)
            .inputImageOrientation(0)
            .inputImageFormat(2)
            .createEGLContext(true)
            .setNeedFaceBeauty(true)
            .setOnFUDebugListener(this)
            .setOnTrackingStatusChangedListener(this)
            .build()

    }

    private fun setFULiveBeautyListener() {
        mBinding.fuBeautyControl.setOnFUControlListener(mFURenderer)
        mBinding.fuBeautyControl.setOnBottomAnimatorChangeListener(object :
            BeautyControlView.OnBottomAnimatorChangeListener {
            private val px166 = resources.getDimensionPixelSize(R.dimen.x160)
            private val px156 = resources.getDimensionPixelSize(R.dimen.x156)
            private val px402 = resources.getDimensionPixelSize(R.dimen.x402)
            private val diff = px402 - px156
            override fun onBottomAnimatorChangeListener(showRate: Float) {
                // 收起 1-->0，弹出 0-->1
            }
        })
    }

    private fun observeChannelJoin() {
        mViewMode.channelJoinLiveData.observe(this, {
            when (it) {
                is Outcome.Success -> {
                    ToastUtil.showShortToast("join success")
                }
                is Outcome.Failure -> {
                    ToastUtil.showShortToast(it.message)
                    finish()
                }
            }
        })
    }

    private fun observeChannelLeave() {
        mViewMode.channelLeaveLiveData.observe(this, {
            if (!it) ToastUtil.showShortToast("Channel Leave Error")
            val intent = Intent()
            intent.putExtra("savePath", remotePath)
            setResult(0, intent)
            finish()
        })
    }

    private fun observePeerJoin() {
        mViewMode.memberJoinLiveData.observe(this, {
            ToastUtil.showShortToast("$it join")
        })
    }

    private fun observePeerLeave() {
        mViewMode.memberLeaveLiveData.observe(this, {
            val videoCanvas = mRemoteVideoCanvasMap.remove(it)
            LogUtil.w("$it leave, videoCanvas: $videoCanvas")
            if (videoCanvas != null) {
                mBinding.layoutFlexBox.removeView(videoCanvas)
                layoutFlexBox()
                videoCanvas.dispose()
            } else {
                LogUtil.e("VideoCanvas not found for $it")
                ToastUtil.showShortToast("Video not found for $it")
            }
        })
    }

    private fun observePublishState() {
        mViewMode.videoPublishStateLiveData.observe(this, {
            if (it == PUB_STATE_PUBLISHED) {
                addLocalVideoCanvas()
            }
        })
    }


    private fun observeRemoteVideoAdd() {
        mViewMode.remoteVideoAddLiveData.observe(this, {
            LogUtil.w("------> addRemoteVideo for $it")
            addRemoteVideoCanvas(it)
        })
    }

    private fun observeAudioRoute() {
        mViewMode.audioRouteLiveData.observe(this, {
            when (it) {
                AUDIO_ROUTE_EARPIECE -> {
                    mBinding.btnSpeaker.isEnabled = true
                    mBinding.btnSpeaker.setImageResource(R.drawable.ic_speaker_disabled)
                }
                AUDIO_ROUTE_SPEAKERPHONE -> {
                    mBinding.btnSpeaker.isEnabled = true
                    mBinding.btnSpeaker.setImageResource(R.drawable.ic_speaker_enabled)
                }
                AUDIO_ROUTE_HEADSET -> {
                    mBinding.btnSpeaker.isEnabled = false
                }
                AUDIO_ROUTE_BLUETOOTH -> {
                    mBinding.btnSpeaker.isEnabled = false
                }
            }
        })
    }

    private fun addLocalVideoCanvas() {
        LogUtil.w("------> addLocalVideoCanvas")
        mBinding.layoutFlexBox.addView(mLocalVideoCanvas)
        layoutFlexBox()
        mViewMode.setLocalVideoView(mLocalVideoCanvas)
    }


    private fun addRemoteVideoCanvas(uid: String) {
        LogUtil.w("------> addRemoteVideoCanvas, uid=$uid")
        if (mRemoteVideoCanvasMap.contains(uid)) {
            LogUtil.e("------> $uid, already add video")
            return
        }

        val videoCanvas = VideoCanvas(this@LiveVideoActivity)
        videoCanvas.uid = uid
        mRemoteVideoCanvasMap[uid] = videoCanvas
        mBinding.layoutFlexBox.addView(videoCanvas)
        layoutFlexBox()
        mViewMode.setRemoteVideoView(uid, videoCanvas)
    }


    private fun layoutFlexBox() {
        val count = mBinding.layoutFlexBox.childCount
        val videoWidth: Int
        val videoHeight: Int
        when (count) {
            1 -> {
                videoWidth = displayWidth
                videoHeight = displayWidth
            }
            2, 3, 4 -> {
                videoWidth = displayWidth / 2
                videoHeight = videoWidth
            }
            else -> {
                videoWidth = displayWidth / 3
                videoHeight = videoWidth
            }
        }
        for (index in 0 until count) {
            val view = mBinding.layoutFlexBox.getChildAt(index)
            val params = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                width = videoWidth
                if (videoHeight > 0) height = videoHeight
            }
            view.layoutParams = params
        }
    }

    fun actionSwitchCamera() {
        mViewMode.switchCamera()
    }

    fun actionMuteLocalAudio() {
        LogUtil.d("----> actionMuteLocalAudio, AudioEnabled=$mAudioEnabled")
        mAudioEnabled = !mAudioEnabled
        if (mAudioEnabled) {
            mBinding.btnMuteAudio.setImageResource(R.drawable.ic_audio_enabled)
        } else {
            mBinding.btnMuteAudio.setImageResource(R.drawable.ic_audio_disabled)
        }
        mViewMode.muteLocalAudio(!mAudioEnabled)
    }

    fun actionMuteLocalVideo() {
        LogUtil.d("----> actionMuteLocalVideo, VideoEnabled=$mVideoEnabled")
        mVideoEnabled = !mVideoEnabled
        if (mVideoEnabled) {
            mBinding.btnMuteVideo.setImageResource(R.drawable.ic_video_enabled)
            mLocalVideoCanvas.background = null
            RtcEngineProxy.get().setupLocalVideo(mLocalVideoCanvas)
        } else {
            mBinding.btnMuteVideo.setImageResource(R.drawable.ic_video_disabled)
            RtcEngineProxy.get().setupLocalVideo(null)
            mLocalVideoCanvas.background =
                ContextCompat.getDrawable(this, R.drawable.defalut_head)
        }
        mViewMode.muteLocalVideo(!mVideoEnabled)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) return true
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        mLocalVideoCanvas.dispose()
        val iterator = mRemoteVideoCanvasMap.iterator()
        while (iterator.hasNext()) {
            iterator.next().value.dispose()
            iterator.remove()
        }
        super.onDestroy()
    }

    override fun onFpsChange(fps: Double, renderTime: Double) {

    }

    override fun onTrackStatusChanged(type: Int, status: Int) {
        runOnUiThread {
            mBinding.ivFaceDetect.visibility = (if (status > 0) View.INVISIBLE else View.VISIBLE)
            Log.w("changeeee", "----> type=$type,status=$status")
            if (status <= 0) {
                if (type == FURenderer.TRACK_TYPE_FACE) {
                    mBinding.ivFaceDetect.text = "未检测到人脸"
                }
            }
        }
    }

    override fun onSurfaceCreated() {
        mFURenderer.onSurfaceCreated()
        mFURenderer.setBeautificationOn(true)
    }

    override fun onSurfaceChanged(viewWidth: Int, viewHeight: Int) {

    }

    @Volatile
    protected var mIsDualInput = true
    override fun onDrawFrame(
        cameraNv21Byte: ByteArray?,
        cameraTexId: Int,
        cameraWidth: Int,
        cameraHeight: Int,
        mvpMatrix: FloatArray?,
        texMatrix: FloatArray?,
        timeStamp: Long
    ): Int {
        return if (mIsDualInput) {
            mFURenderer.onDrawFrame(cameraNv21Byte, cameraTexId, cameraWidth, cameraHeight)
        } else {
            mFURenderer.onDrawFrame(cameraNv21Byte, cameraWidth, cameraHeight)
        }
    }

    override fun onSurfaceDestroy() {
        mFURenderer.onSurfaceDestroyed()
    }

    override fun onCameraChanged(cameraFacing: Int, cameraOrientation: Int) {
        mFURenderer.onCameraChange(cameraFacing, cameraOrientation)
    }

    private var mEnabledRecord = true

    fun actionEnableVideoRecord() {
        mEnabledRecord = !mEnabledRecord
        if (mEnabledRecord) {
            mBinding.btnVideoRecord.alpha = 1F
            mViewMode.rtcEngine.stopVideoRecorder()
        } else {
            mViewMode.rtcEngine.startVideoRecorder(remotePath)
            mBinding.btnVideoRecord.alpha = 0.5F
        }
    }

}