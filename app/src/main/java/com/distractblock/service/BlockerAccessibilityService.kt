package com.distractblock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.distractblock.data.AppRepository
import com.distractblock.ui.SplashBlockActivity
import kotlinx.coroutines.*

class BlockerAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "DistractBlock"
        private val SYSTEM_WHITELIST = setOf(
            "com.distractblock",
            "com.android.launcher", "com.android.launcher2", "com.android.launcher3",
            "com.google.android.apps.nexuslauncher",
            "com.miui.home", "com.sec.android.app.launcher",
            "com.huawei.android.launcher", "com.oppo.launcher",
            "com.android.systemui", "android"
        )

        /** How long a pass-through lasts after the last activity in the app. */
        private const val PASS_THROUGH_MS = 5 * 60 * 1000L  // 5 minutes of inactivity

        /** Packages the user chose to let through (timestamp-based expiry). */
        private val passThrough = mutableMapOf<String, Long>()

        /** Called by SplashBlockActivity when the countdown finishes. */
        fun allowPassThrough(packageName: String) {
            passThrough[packageName] = System.currentTimeMillis()
            Log.d(TAG, "Pass-through granted for $packageName")
        }

        /** Timestamp when pause expires (0 = not paused). */
        private var pauseUntil: Long = 0L

        /** Called from SettingsActivity to temporarily pause all blocking. */
        fun pauseBlocking(durationMs: Long) {
            pauseUntil = System.currentTimeMillis() + durationMs
        }

        fun isBlockingPaused(): Boolean = System.currentTimeMillis() < pauseUntil

        /**
         * Returns true if the package is in pass-through mode.
         * Also refreshes the timestamp so continuous use doesn't expire the session.
         */
        private fun isPassedThrough(packageName: String): Boolean {
            val ts = passThrough[packageName] ?: return false
            if (System.currentTimeMillis() - ts < PASS_THROUGH_MS) {
                // Refresh the timestamp — rolling window while actively using the app
                passThrough[packageName] = System.currentTimeMillis()
                return true
            }
            passThrough.remove(packageName)
            return false
        }
    }

    private val repository: AppRepository by lazy {
        AppRepository.getInstance(applicationContext)
    }
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Realtime in-memory cache synced with Room DB
    private val blockedPackagesCache = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    // Debounce: track last package + timestamp to avoid rapid re-fires
    private var lastBlockedPkg: String? = null
    private var lastBlockedTime: Long = 0L
    private val DEBOUNCE_MS = 1500L

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected")

        // Keep local cache continuously in sync with Room database
        serviceScope.launch {
            repository.observeBlockedApps().collect { apps ->
                blockedPackagesCache.clear()
                blockedPackagesCache.addAll(apps.map { it.packageName })
                Log.d(TAG, "Blocked apps cache updated: $blockedPackagesCache")
            }
        }
    }

    private fun isSystemOrLauncher(pkg: String): Boolean {
        if (pkg == packageName || pkg == "android" || pkg == "com.android.systemui") return true
        if (SYSTEM_WHITELIST.any { it != "android" && pkg.startsWith(it) }) return true

        try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = packageManager.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
            if (resolveInfo?.activityInfo?.packageName == pkg) return true
        } catch (e: Exception) {
            // Ignore error resolving launcher
        }

        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return

        if (isSystemOrLauncher(pkg)) {
            if (lastBlockedPkg != null) lastBlockedPkg = null
            return
        }

        // Allow pass-through if user waited through the countdown
        if (isBlockingPaused()) return
        if (isPassedThrough(pkg)) return

        // Debounce: don't re-fire for the same package within DEBOUNCE_MS
        val now = System.currentTimeMillis()
        if (pkg == lastBlockedPkg && now - lastBlockedTime < DEBOUNCE_MS) return

        serviceScope.launch {
            // Check in-memory synced cache first, fallback to DB
            val blocked = blockedPackagesCache.contains(pkg) || withContext(Dispatchers.IO) { repository.isBlocked(pkg) }
            if (blocked) {
                lastBlockedPkg = pkg
                lastBlockedTime = System.currentTimeMillis()
                Log.d(TAG, "Intercepting blocked app: $pkg")
                try {
                    startActivity(
                        Intent(this@BlockerAccessibilityService, SplashBlockActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(SplashBlockActivity.EXTRA_PACKAGE_NAME, pkg)
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to launch SplashBlockActivity for $pkg", e)
                }
            } else {
                lastBlockedPkg = null
            }
        }
    }

    override fun onInterrupt() { Log.d(TAG, "Interrupted") }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}

