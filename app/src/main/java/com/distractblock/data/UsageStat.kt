package com.distractblock.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Stores per-day usage minutes for each blocked app.
 * Primary key is composite (packageName + date) so upserts are safe.
 */
@Entity(
    tableName = "usage_stats",
    primaryKeys = ["packageName", "date"],
    foreignKeys = [ForeignKey(
        entity = BlockedApp::class,
        parentColumns = ["packageName"],
        childColumns = ["packageName"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("packageName")]
)
data class UsageStat(
    val packageName: String,
    val date: String,          // yyyy-MM-dd
    val minutesUsed: Int = 0,
    val openCount: Int = 0
)
