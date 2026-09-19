package com.distractblock.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.distractblock.service.BlockerAccessibilityService

fun Context.isAccessibilityServiceEnabled(): Boolean {
    val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
    val enabledServices = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    if (enabledServices != null && enabledServices.any {
        it.resolveInfo.serviceInfo.packageName == packageName &&
        it.resolveInfo.serviceInfo.name == BlockerAccessibilityService::class.java.name
    }) {
        return true
    }

    val expectedFull = ComponentName(this, BlockerAccessibilityService::class.java).flattenToString()
    val expectedShort = ComponentName(this, BlockerAccessibilityService::class.java).flattenToShortString()
    val enabled = Settings.Secure.getString(
        contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    return enabled.split(":").any {
        it.equals(expectedFull, ignoreCase = true) || it.equals(expectedShort, ignoreCase = true)
    }
}

fun Context.hasUsageStatsPermission(): Boolean {
    val appOps = getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

fun Context.hasSystemAlertWindowPermission(): Boolean {
    return Settings.canDrawOverlays(this)
}
