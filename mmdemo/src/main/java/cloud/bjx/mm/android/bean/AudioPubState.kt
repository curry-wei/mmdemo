package cloud.bjx.mm.android.bean

import cloud.bjx.mm.sdk.Constants

data class AudioPubState(val oldState: Int, val newState: Int) {

    fun getEvent(): String = "音频发布状态 => ${getStateDesc(newState)}"

    private fun getStateDesc(state: Int): String = when (state) {
        Constants.PUB_STATE_IDLE -> "Idle"
        Constants.PUB_STATE_PUBLISHING -> "Publishing"
        Constants.PUB_STATE_NO_PUBLISHED -> "NoPublished"
        Constants.PUB_STATE_PUBLISHED -> "Published"
        else -> "Invalid"
    }

}