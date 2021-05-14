package cloud.bjx.mm.android.utils

import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes
import cloud.bjx.mm.android.ContextProvider

object ToastUtil {

    private var toast: Toast? = null

    @JvmStatic
    fun showShortToast(msg: CharSequence) {
        show(msg, Toast.LENGTH_SHORT)
    }

    @JvmStatic
    fun showShortToast(@StringRes resId: Int) {
        show(ContextProvider.get().getString(resId), Toast.LENGTH_SHORT)
    }

    @JvmStatic
    fun showLongToast(msg: CharSequence) {
        show(msg, Toast.LENGTH_LONG)
    }

    @JvmStatic
    fun showLongToast(@StringRes resId: Int) {
        show(ContextProvider.get().getString(resId), Toast.LENGTH_LONG)
    }

    @JvmStatic
    private fun show(msg: CharSequence, duration: Int) {
        toast?.cancel()
        toast = Toast.makeText(ContextProvider.get(), msg, duration).apply {
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }

}