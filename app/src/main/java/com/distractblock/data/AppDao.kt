package com.distractblock.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ── BlockedApp queries ───────────────────────────────────────────────────

    @Query("SELECT * FROM blocked_apps ORDER BY appName ASC")
    fun observeAllBlocked(): Flow<List<BlockedApp>>

    @Query("SELECT * FROM blocked_apps ORDER BY appName ASC")
    suspend fun getAllBlocked(): List<BlockedApp>

    /** Fast O(1) existence check used by the AccessibilityService on every event. */
    @Query("SELECT COUNT(*) FROM blocked_apps WHERE packageName = :pkg")
    suspend fun isBlocked(pkg: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertApp(app: BlockedApp)

    @Delete
    suspend fun deleteApp(app: BlockedApp)

    @Query("UPDATE blocked_apps SET dailyOpenCount = dailyOpenCount + 1, totalOpenCount = totalOpenCount + 1, lastOpenedDate = :date WHERE packageName = :pkg")
    suspend fun incrementOpenCount(pkg: String, date: String)

    @Query("UPDATE blocked_apps SET dailyOpenCount = 0 WHERE packageName = :pkg")
    suspend fun resetDailyCount(pkg: String)

    // ── UsageStat queries ────────────────────────────────────────────────────

    @Query("SELECT * FROM usage_stats WHERE packageName = :pkg ORDER BY date DESC LIMIT 7")
    suspend fun getLast7Days(pkg: String): List<UsageStat>

    @Query("SELECT * FROM usage_stats WHERE packageName = :pkg ORDER BY date DESC LIMIT 7")
    fun observeLast7Days(pkg: String): Flow<List<UsageStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUsageStat(stat: UsageStat)
}
