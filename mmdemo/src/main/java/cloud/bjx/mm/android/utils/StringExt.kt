package cloud.bjx.mm.android.utils

import java.util.regex.Pattern

fun String?.checkId(): Boolean {
    if (this == null) return false
    if (this.isBlank() || this.length > 64) return false
    val regex = "^[A-Za-z0-9#=@,._-]*$"
    return Pattern.matches(regex, this)
}