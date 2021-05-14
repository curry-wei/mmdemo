package cloud.bjx.mm.android.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChannelOption(
    val channelId: String,
    val modeLiveBroadcasting: Boolean,
    val useSpeakerphone: Boolean,
    var roleBroadcaster: Boolean,
    val autoSubscribe: Boolean
) : Parcelable