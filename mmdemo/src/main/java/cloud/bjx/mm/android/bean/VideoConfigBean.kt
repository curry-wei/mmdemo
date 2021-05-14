package cloud.bjx.mm.android.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class VideoConfigBean(
    val channelId: String,
    val frameRate: Int,
    val videoWidth: Int,
    val videoHeight: Int,
    val useSpeaker: Boolean
) : Parcelable