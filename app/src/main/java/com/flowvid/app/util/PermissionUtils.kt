package com.flowvid.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtils {

    /** The single permission FlowVid should request via the system dialog. */
    fun primaryMediaPermission(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private fun granted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /** True if FlowVid can see the full device library (not just a partial selection). */
    fun hasFullAccess(context: Context): Boolean = granted(context, primaryMediaPermission())

    /**
     * True on Android 14+ if the user granted the "select photos/videos" partial
     * access instead of full access. FlowVid still works in this mode; it will
     * simply only see the videos the user chose.
     */
    fun hasPartialAccess(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return false
        return granted(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
    }

    fun hasAnyAccess(context: Context): Boolean = hasFullAccess(context) || hasPartialAccess(context)
}
