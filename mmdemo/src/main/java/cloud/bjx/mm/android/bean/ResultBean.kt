package cloud.bjx.mm.android.bean

import androidx.annotation.Keep

@Keep
data class ResultBean<T>(
    val code: Int,
    val message: String,
    val data: T,
    val timestamp: Long
) {

    fun isSuccess(): Boolean = code == 0

    override fun toString(): String {
        return "code=$code, message=$message, data=$data"
    }
}