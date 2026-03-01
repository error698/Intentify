package com.distractblock.util

import android.content.Context
import android.provider.Settings
import com.distractblock.service.BlockerAccessibilityService

fun Context.isAccessibilityServiceEnabled(): Boolean {
    val serviceName = "$packageName/${BlockerAccessibilityService::class.java.name}"
    val enabled = Settings.Secure.getString(
        contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabled.split(":").any { it.equals(serviceName, ignoreCase = true) }
}

fun Context.hasUsageStatsPermission(): Boolean {
    val usm = getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
    val stats = usm.queryUsageStats(
        android.app.usage.UsageStatsManager.INTERVAL_DAILY,
        System.currentTimeMillis() - 60_000L,
        System.currentTimeMillis()
    )
    return !stats.isNullOrEmpty()
}

fun Context.hasSystemAlertWindowPermission(): Boolean {
    return Settings.canDrawOverlays(this)
}
