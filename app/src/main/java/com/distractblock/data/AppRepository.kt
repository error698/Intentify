package com.distractblock.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Single source of truth for all app data.
 * All heavy work runs on Dispatchers.IO.
 */
class AppRepository(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).appDao()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ── Blocked apps ─────────────────────────────────────────────────────────

    fun observeBlockedApps(): Flow<List<BlockedApp>> = dao.observeAllBlocked()

    suspend fun addApp(packageName: String) = withContext(Dispatchers.IO) {
        val name = try {
            val info = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) { packageName }

        dao.insertApp(BlockedApp(packageName = packageName, appName = name))
    }

    suspend fun removeApp(packageName: String) = withContext(Dispatchers.IO) {
        dao.deleteApp(BlockedApp(packageName = packageName, appName = ""))
    }

    /**
     * Called by the AccessibilityService. Uses an in-memory cache so
     * the DB is not hit on every single accessibility event.
     */
    private val blockedCache = mutableSetOf<String>()
    private var cacheValid = false

    suspend fun isBlocked(packageName: String): Boolean = withContext(Dispatchers.IO) {
        if (!cacheValid) {
            blockedCache.clear()
            blockedCache.addAll(dao.getAllBlocked().map { it.packageName })
            cacheValid = true
        }
        blockedCache.contains(packageName)
    }

    /** Call after add/remove to bust the cache. */
    fun invalidateCache() { cacheValid = false }

    // ── Open counting ─────────────────────────────────────────────────────────

    suspend fun recordOpen(packageName: String) = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        dao.incrementOpenCount(packageName, today)

        // Upsert today's usage stat open count
        val existing = dao.getLast7Days(packageName).firstOrNull { it.date == today }
        dao.upsertUsageStat(
            UsageStat(
                packageName = packageName,
                date = today,
                minutesUsed = existing?.minutesUsed ?: 0,
                openCount = (existing?.openCount ?: 0) + 1
            )
        )
    }

    // ── Usage stats (from UsageStatsManager + our DB) ─────────────────────────

    /**
     * Fetches real screen-time minutes from UsageStatsManager for the last 7 days,
     * merges with our open-count DB, and upserts the results.
     * Requires PACKAGE_USAGE_STATS permission.
     */
    suspend fun syncUsageStats(packageName: String) = withContext(Dispatchers.IO) {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance()

        repeat(7) { daysAgo ->
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val endMs = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startMs = cal.timeInMillis

            val dateStr = dateFormat.format(Date(startMs))
            val stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startMs, endMs
            )
            val appStat = stats?.firstOrNull { it.packageName == packageName }
            val minutes = ((appStat?.totalTimeInForeground ?: 0L) / 60_000L).toInt()

            val existing = dao.getLast7Days(packageName).firstOrNull { it.date == dateStr }
            dao.upsertUsageStat(
                UsageStat(
                    packageName = packageName,
                    date = dateStr,
                    minutesUsed = minutes,
                    openCount = existing?.openCount ?: 0
                )
            )
        }
    }

    fun observeUsageStats(packageName: String): Flow<List<UsageStat>> =
        dao.observeLast7Days(packageName)

    suspend fun getLast7DaysStats(packageName: String): List<UsageStat> =
        withContext(Dispatchers.IO) { dao.getLast7Days(packageName) }

    // ── Installed apps helper ─────────────────────────────────────────────────

    suspend fun getInstallableApps(excludePackages: Set<String>): List<Pair<String, String>> =
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { info ->
                    info.packageName !in excludePackages &&
                    pm.getLaunchIntentForPackage(info.packageName) != null &&
                    (info.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0
                }
                .map { info -> info.packageName to pm.getApplicationLabel(info).toString() }
                .sortedBy { it.second.lowercase() }
        }
}
