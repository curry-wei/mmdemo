package cloud.bjx.mm.android.utils

import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.ContextCompat

/** Return system service which type is [T] */
inline fun <reified T> Context.getSystemService(): T? =
    ContextCompat.getSystemService(this, T::class.java)

val Context.clipboardManager get() = getSystemService<ClipboardManager>()