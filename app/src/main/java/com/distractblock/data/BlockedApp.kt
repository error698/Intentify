package com.distractblock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing one blocked app.
 * dailyOpenCount resets each day via a scheduled worker or on first open of the day.
 */
@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val dailyOpenCount: Int = 0,
    val lastOpenedDate: String = "",   // yyyy-MM-dd, used to detect day rollovers
    val totalOpenCount: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
