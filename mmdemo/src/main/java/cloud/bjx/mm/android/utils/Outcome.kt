package cloud.bjx.mm.android.utils

sealed class Outcome<out T : Any> {
    data class Success<out T : Any>(val value: T) : Outcome<T>()
    data class Failure(val message: String, val cause: Exception? = null) : Outcome<Nothing>()
}