package cloud.bjx.mm.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.children
import cloud.bjx.mm.android.R
import com.google.android.flexbox.FlexboxLayout

class ChipGroupView : FlexboxLayout, View.OnClickListener {

    private val mLayoutInflater: LayoutInflater
    private var mCheckedIndex = 0
    private var mBlock: ((index: Int) -> Unit)? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        this.mLayoutInflater = LayoutInflater.from(context)
    }

    fun setListener(black: ((index: Int) -> Unit)?) {
        mBlock = black
    }

    fun setChipItems(items: List<String>, checkedIndex: Int = 0) {
        if (items.count() == 0) return
        removeAllViews()
        mCheckedIndex = checkedIndex

        items.forEachIndexed { index, item ->
            val chip = mLayoutInflater.inflate(R.layout.view_chip, this, false) as TextView
            chip.text = item
            chip.isSelected = index == mCheckedIndex
            chip.setOnClickListener(this)
            this.addView(chip)
        }
        mBlock?.invoke(mCheckedIndex)
    }

    override fun onClick(v: View?) {
        if (v == null) return
        updateChipChecked(v)
    }

    private fun updateChipChecked(view: View) {
        for ((index, child) in children.withIndex()) {
            if (child === view) {
                child.isSelected = true
                mCheckedIndex = index
                mBlock?.invoke(mCheckedIndex)
            } else {
                child.isSelected = false
            }
        }
    }

}