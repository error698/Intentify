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

    private lateinit var repository: AppRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Debounce: track last package + timestamp to avoid rapid re-fires
    private var lastBlockedPkg: String? = null
    private var lastBlockedTime: Long = 0L
    private val DEBOUNCE_MS = 2000L

    override fun onServiceConnected() {
        super.onServiceConnected()
        repository = AppRepository(applicationContext)
        Log.d(TAG, "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return
        if (SYSTEM_WHITELIST.any { pkg.startsWith(it) }) {
            if (lastBlockedPkg != null) lastBlockedPkg = null
            return
        }

        // Allow pass-through if user waited through the countdown
        if (isBlockingPaused()) return
        if (isPassedThrough(pkg)) return

        // Debounce: don't re-fire for the same package within 2 seconds
        val now = System.currentTimeMillis()
        if (pkg == lastBlockedPkg && now - lastBlockedTime < DEBOUNCE_MS) return

        // Check DB on IO thread, launch splash on Main
        serviceScope.launch {
            val blocked = withContext(Dispatchers.IO) { repository.isBlocked(pkg) }
            if (blocked) {
                lastBlockedPkg = pkg
                lastBlockedTime = System.currentTimeMillis()
                Log.d(TAG, "Intercepting: $pkg")
                startActivity(
                    Intent(this@BlockerAccessibilityService, SplashBlockActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(SplashBlockActivity.EXTRA_PACKAGE_NAME, pkg)
                    }
                )
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

