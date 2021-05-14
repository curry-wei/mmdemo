package cloud.bjx.mm.android.adapter

import cloud.bjx.mm.android.R
import cloud.bjx.mm.android.bean.Member
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class LiveRoomMemberAdapter : BaseQuickAdapter<Member, BaseViewHolder>(R.layout.item_member) {

    override fun convert(holder: BaseViewHolder, item: Member) {
        if (item.isMyself) {
            holder.setText(R.id.text_uid, "我")
        } else {
            holder.setText(R.id.text_uid, item.uid)
        }

        if (item.isMyself) {
            holder.setBackgroundResource(R.id.container, R.drawable.bg_broadcaster_1)
            if (item.isBroadcaster) {
                if (item.isPublished) {
                    if (item.isMuted) {
                        holder.setText(R.id.text_action, "取消静音")
                    } else {
                        holder.setText(R.id.text_action, "点击静音")
                    }
                } else {
                    holder.setText(R.id.text_action, "未上麦")
                }
            } else {
                holder.setText(R.id.text_action, "观众")
            }
            holder.setVisible(R.id.img_speak_state, false)

        } else {
            holder.setBackgroundResource(R.id.container, R.drawable.bg_broadcaster_2)
            if (item.isBroadcaster) {
                if (item.isMuted) {
                    holder.setText(R.id.text_action, "取消静音")
                } else {
                    holder.setText(R.id.text_action, "点击静音")
                }
            } else {
                holder.setText(R.id.text_action, "")
            }

            holder.setVisible(R.id.img_speak_state, true)
            if (item.voiceActive) {
                holder.setImageResource(R.id.img_speak_state, R.drawable.ic_voice_active)
            } else {
                holder.setImageResource(R.id.img_speak_state, R.drawable.ic_voice_inactive)
            }
        }
    }

}