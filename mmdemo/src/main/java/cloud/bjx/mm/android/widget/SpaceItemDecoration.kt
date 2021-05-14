package cloud.bjx.mm.android.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(
    private val spanCount: Int,
    private val rowSpacing: Int,
    private val columnSpacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount
        val spacing = rowSpacing / 2

        when (column) {
            0 -> {
                outRect.left = rowSpacing
                outRect.right = spacing
            }
            spanCount - 1 -> {
                outRect.left = spacing
                outRect.right = rowSpacing
            }
            else -> {
                outRect.left = spacing
                outRect.right = spacing
            }
        }

        if (position >= spanCount) {
            outRect.top = columnSpacing
        }
    }

}