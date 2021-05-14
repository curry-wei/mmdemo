package cloud.bjx.mm.android.utils

import android.content.Context
import android.content.pm.PackageManager

fun Context.getVersionName(pkgName: String = packageName): String {
    if (pkgName.isBlank()) return "1.0.0"
    try {
        return packageManager.getPackageInfo(pkgName, 0).versionName
    } catch (e: Exception) {
        LogUtil.e("getVersionName error:", e)
    }
    return "1.0.0"
}

@Suppress("DEPRECATION")
fun Context.getVersionCode(pkgName: String = packageName): Int {
    if (pkgName.isBlank()) return 0
    try {
        return packageManager.getPackageInfo(pkgName, 0).versionCode
    } catch (e: Exception) {
        LogUtil.e("getVersionCode error:", e)
    }
    return 0
}

fun Context.getMetaData(key: String): String? {
    try {
        return packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData.getString(key)
    } catch (e: Exception) {
        LogUtil.e("getMetaData error:", e)
    }
    return null
}

fun Context.getChannel(): String? {
    return getMetaData("CHANNEL")
}