package cloud.bjx.mm.android.utils

import android.content.Context

inline val Context.displayWidth
    get() = resources.displayMetrics.widthPixels

inline val Context.displayHeight
    get() = resources.displayMetrics.heightPixels

fun Context.getStatusBarHeight(): Int {
    val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
    return resources.getDimensionPixelSize(resourceId)
}

fun Context.dp2px(dp: Int): Int {
    return (dp * resources.displayMetrics.density + 0.5f).toInt()
}

fun Context.px2dp(px: Int): Int {
    return (px / resources.displayMetrics.density + 0.5f).toInt()
}

fun Context.getBaseVideoRecordPath(): String {
    return externalCacheDir?.path + "/rtc"
}

