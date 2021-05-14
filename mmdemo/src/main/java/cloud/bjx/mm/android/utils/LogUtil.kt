package cloud.bjx.mm.android.utils

import android.util.Log

object LogUtil {

    private const val MIN_STACK_OFFSET = 3
    private const val Log_LEVEL_CLOSE = Log.ASSERT + 1

    private const val TAG = "mmrtc"
    private const val switchThread = true
    private const val switchFileLineNumber = true

    private var level = Log.DEBUG

    @JvmStatic
    fun setLevel(value: Int) {
        level = when (value) {
            Log.VERBOSE -> Log.VERBOSE
            Log.DEBUG -> Log.DEBUG
            Log.INFO -> Log.INFO
            Log.WARN -> Log.WARN
            Log.ERROR -> Log.ERROR
            Log.ASSERT -> Log.ASSERT
            else -> Log_LEVEL_CLOSE
        }
    }

    @JvmStatic
    fun getLevel(): Int = level

    @JvmStatic
    @JvmOverloads
    fun e(msg: String, e: Throwable? = null) {
        printLog(Log.ERROR, msg, e)
    }

    @JvmStatic
    @JvmOverloads
    fun w(msg: String, e: Throwable? = null) {
        printLog(Log.WARN, msg, e)
    }

    @JvmStatic
    @JvmOverloads
    fun i(msg: String, e: Throwable? = null) {
        printLog(Log.INFO, msg, e)
    }

    @JvmStatic
    @JvmOverloads
    fun d(msg: String, e: Throwable? = null) {
        printLog(Log.DEBUG, msg, e)
    }

    @JvmStatic
    @JvmOverloads
    fun v(msg: String, e: Throwable? = null) {
        printLog(Log.VERBOSE, msg, e)
    }

    private fun printLog(priority: Int, msg: String, e: Throwable?) {
        if (priority < level) return

        val msgBuilder = StringBuilder()
        val currentThread = Thread.currentThread()

        if (switchThread) {
            msgBuilder.append("<")
            msgBuilder.append(currentThread.name)
            msgBuilder.append("> ")
        }

        if (switchFileLineNumber) {
            val elements = currentThread.stackTrace
            val offset = getStackOffset(elements)
            val targetElement = elements[offset]
            msgBuilder.append("[")
            msgBuilder.append(targetElement.fileName)
            msgBuilder.append(":")
            msgBuilder.append(targetElement.lineNumber)
            msgBuilder.append("] ")
        }

        msgBuilder.append(msg)
        if (e != null) {
            msgBuilder.append("\n")
            msgBuilder.append(Log.getStackTraceString(e))
        }

        val msgLen: Int = msgBuilder.length
        val stepLen = 4000
        if (msgLen > stepLen) {
            for (i in 0..msgLen step stepLen) {
                if (i + stepLen < msgLen) {
                    Log.println(priority, TAG, msgBuilder.substring(i, i + stepLen))
                } else {
                    Log.println(priority, TAG, msgBuilder.substring(i, msgLen))
                }
            }
        } else {
            Log.println(priority, TAG, msgBuilder.toString())
        }
    }

    private fun getStackOffset(trace: Array<StackTraceElement>): Int {
        var offset = MIN_STACK_OFFSET
        while (offset < trace.size) {
            val e = trace[offset]
            val name = e.className
            if (name != LogUtil::class.java.name) {
                return offset
            }
            offset++
        }
        return 2
    }

}