package cloud.bjx.mm.android.utils

import cloud.bjx.mm.sdk.Constants

fun Int.errorMessage(): String {
    return when (this) {
        Constants.ERR_INVALID_TOKEN -> "token error"
        Constants.ERR_SET_ROLE -> "setClientRole error"
        Constants.ERR_CHANNEL_JOIN -> "joinChannel error"
        else -> "unknown error"
    }
}


fun Int.pubState(): String = when (this) {
    Constants.PUB_STATE_IDLE -> "IDLE"
    Constants.PUB_STATE_PUBLISHING -> "PUBLISHING"
    Constants.PUB_STATE_PUBLISHED -> "PUBLISHED "
    Constants.PUB_STATE_NO_PUBLISHED -> "NO_PUBLISHED"
    else -> "UNKNOWN"
}


fun Int.audioRoute(): String = when (this) {
    Constants.AUDIO_ROUTE_EARPIECE -> "Earpiece"
    Constants.AUDIO_ROUTE_SPEAKERPHONE -> "Speakerphone"
    Constants.AUDIO_ROUTE_HEADSET -> "Handset "
    Constants.AUDIO_ROUTE_BLUETOOTH -> "Bluetooth"
    else -> "Unknown"
}


fun Int.rtmpState(): String {
    when (this) {
        Constants.RTMP_STATE_IDLE -> return "idle"
        Constants.RTMP_STATE_CONNECTING -> return "connecting"
        Constants.RTMP_STATE_RUNNING -> return "running"
        Constants.RTMP_STATE_RECOVERING -> return "recovering"
    }
    return "unknown state $this"
}