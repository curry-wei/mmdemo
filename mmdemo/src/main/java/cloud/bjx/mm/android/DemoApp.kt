package cloud.bjx.mm.android

import android.app.Application
import cloud.bjx.mm.android.fulive.utils.ThreadHelper
import cloud.bjx.mm.sdk.RtcEngine
import com.faceunity.FURenderer
import com.faceunity.utils.FileUtils
import com.tencent.bugly.crashreport.CrashReport
import java.io.File

class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        RtcEngine.setLogLevel(UserSettings.getLogLevel())
        // bugLy 测试为true 发布为false
        CrashReport.initCrashReport(this, "04abbb5dc2", true)

        // 初始化FULive SDK
        FURenderer.initFURenderer(this)
        ThreadHelper.getInstance().execute {
            FileUtils.copyAssetsFileToLocal(
                this,
                File(
                    getExternalFilesDir(null),
                    "bg_seg_green"
                ),
                "bg_seg_green/sample"
            )
            FileUtils.copyAssetsChangeFaceTemplate(this)
        }

    }

}