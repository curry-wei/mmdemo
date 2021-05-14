package cloud.bjx.mm.android.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cloud.bjx.mm.android.R
import cloud.bjx.mm.android.databinding.ActivityFlexTestBinding
import cloud.bjx.mm.android.utils.LogUtil
import cloud.bjx.mm.android.utils.displayWidth
import com.google.android.flexbox.FlexboxLayout
import java.lang.IllegalArgumentException


class FlexTestActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityFlexTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityFlexTestBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.btnAdd.setOnClickListener { actionAddView() }
        mBinding.btnDel.setOnClickListener { actionDelView() }
    }

    private fun actionAddView() {
        val view = View(this)
        setViewBgColor(view, mBinding.layoutFlexBox.childCount)
        mBinding.layoutFlexBox.addView(view)
        layoutFlexBox()
    }

    private fun actionDelView() {
        val count = mBinding.layoutFlexBox.childCount
        if (count == 0) return
        mBinding.layoutFlexBox.removeViewAt(count - 1)
        layoutFlexBox()
    }

    private fun layoutFlexBox() {
        val count = mBinding.layoutFlexBox.childCount
        LogUtil.d("----> count: $count")
        val videoSize = when (count) {
            1 -> displayWidth - 10
            2, 3, 4 -> displayWidth / 2 - 10
            else -> displayWidth / 3 - 10
        }
        LogUtil.d("----> videoSize: $videoSize")

        for (index in 0 until count) {
            LogUtil.d("----> index: $index")
            val view = mBinding.layoutFlexBox.getChildAt(index)
            val params = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                width = videoSize
                height = videoSize
            }
            view.layoutParams = params
        }
    }

    private fun setViewBgColor(view: View, index: Int) {
        val color = when (index % 2) {
            0 -> ContextCompat.getColor(this, R.color.photo1)
            1 -> ContextCompat.getColor(this, R.color.photo2)
            else -> throw IllegalArgumentException()
        }
        view.setBackgroundColor(color)
    }

}