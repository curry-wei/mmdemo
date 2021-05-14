package cloud.bjx.mm.android.adapter

import cloud.bjx.mm.android.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class LiveRoomEventAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_event) {

    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.text_event, item)
    }

}