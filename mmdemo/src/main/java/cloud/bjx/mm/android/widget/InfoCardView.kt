package cloud.bjx.mm.android.widget

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import cloud.bjx.mm.android.R

class InfoCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val mTitle: TextView
    private val mDetail: TextView
    private val mDividerTop: View
    private val mDividerBottom: View

    init {
        LayoutInflater.from(context).inflate(R.layout.view_info_card, this, true)
        mTitle = findViewById(R.id.text_title)
        mDetail = findViewById(R.id.text_detail)
        mDividerTop = findViewById(R.id.divider_top)
        mDividerBottom = findViewById(R.id.divider_bottom)

        attrs?.let {
            val a = context.obtainStyledAttributes(attrs, R.styleable.InfoCardView)
            setTitle(a.getString(R.styleable.InfoCardView_ic_title))
            setDetail(a.getString(R.styleable.InfoCardView_ic_detail))
            setDividerType(a.getInt(R.styleable.InfoCardView_ic_divider, 0))
            a.recycle()
        }

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            mDetail, // textView
            2,       // autoSizeMinTextSize
            15,      // autoSizeMaxTextSize
            1,       // unit
            TypedValue.COMPLEX_UNIT_SP
        )
        isFocusable = true
        isClickable = true
        setBackgroundResource(R.drawable.list_selector)
    }

    private fun setTitle(text: CharSequence?) {
        mTitle.text = text
    }

    fun setDetail(text: CharSequence?) {
        mDetail.text = text
    }

    // 0:none, 1:top, 2:bottom, 3:both
    private fun setDividerType(type: Int) {
        var visibilityTop = View.VISIBLE
        var visibilityBottom = View.VISIBLE
        when (type) {
            0 -> {
                visibilityTop = View.GONE
                visibilityBottom = View.GONE
            }
            1 -> {
                visibilityTop = View.VISIBLE
                visibilityBottom = View.GONE
            }
            2 -> {
                visibilityTop = View.GONE
                visibilityBottom = View.VISIBLE
            }
            3 -> {
                visibilityTop = View.VISIBLE
                visibilityBottom = View.VISIBLE
            }
        }
        mDividerTop.visibility = visibilityTop
        mDividerBottom.visibility = visibilityBottom
    }


    companion object {
        @BindingAdapter("ic_detail")
        @JvmStatic
        fun setDetail(view: InfoCardView, value: String) = view.setDetail(value)
    }

}