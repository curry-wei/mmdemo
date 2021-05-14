package cloud.bjx.mm.android.fulive

import cloud.bjx.mm.android.ContextProvider
import cloud.bjx.mm.android.utils.LogUtil
import java.io.IOException
import java.io.InputStream

class FileUtil {

    /**
     * 从 assets 文件夹或者本地磁盘读文件
     *
     * @param path
     * @return
     */
    companion object {
        fun readFile(path: String): ByteArray? {
            var input: InputStream? = null
            try {
                input = ContextProvider.get().assets.open(path)
                val buffer = ByteArray(input.available())
                val length = input.read(buffer)
                LogUtil.i("FULive readFile. path: $path, length: $length Byte")

                return buffer
            } catch (e: IOException) {
                LogUtil.e("FULive readFile: e", e)
            } finally {
                input?.close()
            }

            return null
        }
    }

}