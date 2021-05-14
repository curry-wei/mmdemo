package cloud.bjx.mm.android.bean

import cloud.bjx.mm.android.UserSettings
import cloud.bjx.mm.sdk.Constants

data class Member(
    val uid: String,
    var isBroadcaster: Boolean = true // 当前用户是否时主播
) {

    var isMuted: Boolean = false
    var voiceActive: Boolean = false

    val isMyself: Boolean
    var pubState: Int = Constants.PUB_STATE_IDLE

    val isPublished: Boolean
        get() = pubState == Constants.PUB_STATE_PUBLISHED

    init {
        val currentUserId: String = UserSettings.getUserId() ?: ""
        isMyself = uid == currentUserId
    }

}