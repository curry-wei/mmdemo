package cloud.bjx.mm.android.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtil private constructor(private var activity: Activity) {

    private var requestCode: Int = 0
    private var permissions: Array<String>? = null

    companion object {
        @JvmStatic
        fun with(activity: Activity): PermissionUtil = PermissionUtil(activity)

        @JvmStatic
        fun findDeniedPermissions(ctx: Context, permissions: Array<String>): List<String> {
            val denyPermissions = mutableListOf<String>()
            permissions.forEach {
                if (ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED) {
                    denyPermissions.add(it)
                }
            }
            return denyPermissions
        }

        @JvmStatic
        fun hasPermission(ctx: Context, permissions: Array<String>): Boolean {
            for (permission in permissions) {
                val hasPermission = ContextCompat.checkSelfPermission(ctx, permission) ==
                        PackageManager.PERMISSION_GRANTED
                if (!hasPermission) {
                    return false
                }
            }
            return true
        }

        fun hasPermission(ctx: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(ctx, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestCode(requestCode: Int): PermissionUtil {
        this.requestCode = requestCode
        return this
    }

    fun permissions(permissions: Array<String>): PermissionUtil {
        this.permissions = permissions
        return this
    }

    fun request() {
        val deniedPermissions = permissions?.let { findDeniedPermissions(activity, it) }
        LogUtil.d("----> deniedPermissions: $deniedPermissions")

        if (!deniedPermissions.isNullOrEmpty()) {
            ActivityCompat.requestPermissions(activity, permissions!!, requestCode)
        }
    }

}