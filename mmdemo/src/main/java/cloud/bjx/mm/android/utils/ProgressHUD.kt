package cloud.bjx.mm.android.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.widget.ContentLoadingProgressBar
import cloud.bjx.mm.android.ContextProvider
import cloud.bjx.mm.android.R

class ProgressHUD : Dialog {

    companion object {
        private var instance: ProgressHUD? = null

        fun show(context: Context, cancelable: Boolean = true) {
            dismiss()
            instance = ProgressHUD(context)
            instance?.setCancelable(cancelable)
            instance?.show()
        }

        fun show(context: Context, @StringRes resId: Int, cancelable: Boolean = true) {
            dismiss()
            instance = ProgressHUD(context, ContextProvider.get().getString(resId))
            instance?.setCancelable(cancelable)
            instance?.show()
        }

        fun show(context: Context, text: String, cancelable: Boolean = true) {
            dismiss()
            instance = ProgressHUD(context, text)
            instance?.setCancelable(cancelable)
            instance?.show()
        }

        fun dismiss() {
            instance?.dismiss()
            instance = null
        }
    }

    private var message: String? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, text: String?) : super(context, R.style.ProgressHUD) {
        message = text
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_progerss_hud)

        if (message == null) {
            findViewById<TextView>(R.id.text_message).visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.text_message).apply {
                visibility = View.VISIBLE
                text = message
            }
        }

        val progressBar = findViewById<ContentLoadingProgressBar>(R.id.progress_bar)
        @Suppress("DEPRECATION")
        progressBar.indeterminateDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
    }

}