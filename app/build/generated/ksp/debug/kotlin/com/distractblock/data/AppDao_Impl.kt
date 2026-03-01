package com.distractblock.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDao_Impl(
  __db: RoomDatabase,
) : AppDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfBlockedApp: EntityInsertAdapter<BlockedApp>

  private val __insertAdapterOfUsageStat: EntityInsertAdapter<UsageStat>

  private val __deleteAdapterOfBlockedApp: EntityDeleteOrUpdateAdapter<BlockedApp>
  init {
    this.__db = __db
    this.__insertAdapterOfBlockedApp = object : EntityInsertAdapter<BlockedApp>() {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `blocked_apps` (`packageName`,`appName`,`dailyOpenCount`,`lastOpenedDate`,`totalOpenCount`,`addedAt`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: BlockedApp) {
        statement.bindText(1, entity.packageName)
        statement.bindText(2, entity.appName)
        statement.bindLong(3, entity.dailyOpenCount.toLong())
        statement.bindText(4, entity.lastOpenedDate)
        statement.bindLong(5, entity.totalOpenCount.toLong())
        statement.bindLong(6, entity.addedAt)
      }
    }
    this.__insertAdapterOfUsageStat = object : EntityInsertAdapter<UsageStat>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `usage_stats` (`packageName`,`date`,`minutesUsed`,`openCount`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UsageStat) {
        statement.bindText(1, entity.packageName)
        statement.bindText(2, entity.date)
        statement.bindLong(3, entity.minutesUsed.toLong())
        statement.bindLong(4, entity.openCount.toLong())
      }
    }
    this.__deleteAdapterOfBlockedApp = object : EntityDeleteOrUpdateAdapter<BlockedApp>() {
      protected override fun createQuery(): String =
          "DELETE FROM `blocked_apps` WHERE `packageName` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: BlockedApp) {
        statement.bindText(1, entity.packageName)
      }
    }
  }

  public override suspend fun insertApp(app: BlockedApp): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfBlockedApp.insert(_connection, app)
  }

  public override suspend fun upsertUsageStat(stat: UsageStat): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfUsageStat.insert(_connection, stat)
  }

  public override suspend fun deleteApp(app: BlockedApp): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfBlockedApp.handle(_connection, app)
  }

  public override fun observeAllBlocked(): Flow<List<BlockedApp>> {
    val _sql: String = "SELECT * FROM blocked_apps ORDER BY appName ASC"
    return createFlow(__db, false, arrayOf("blocked_apps")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfPackageName: Int = getColumnIndexOrThrow(_stmt, "packageName")
        val _columnIndexOfAppName: Int = getColumnIndexOrThrow(_stmt, "appName")
        val _columnIndexOfDailyOpenCount: Int = getColumnIndexOrThrow(_stmt, "dailyOpenCount")
        val _columnIndexOfLastOpenedDate: Int = getColumnIndexOrThrow(_stmt, "lastOpenedDate")
        val _columnIndexOfTotalOpenCount: Int = getColumnIndexOrThrow(_stmt, "totalOpenCount")
        val _columnIndexOfAddedAt: Int = getColumnIndexOrThrow(_stmt, "addedAt")
        val _result: MutableList<BlockedApp> = mutableListOf()
        while (_stmt.step()) {
          val _item: BlockedApp
          val _tmpPackageName: String
          _tmpPackageName = _stmt.getText(_columnIndexOfPackageName)
          val _tmpAppName: String
          _tmpAppName = _stmt.getText(_columnIndexOfAppName)
          val _tmpDailyOpenCount: Int
          _tmpDailyOpenCount = _stmt.getLong(_columnIndexOfDailyOpenCount).toInt()
          val _tmpLastOpenedDate: String
          _tmpLastOpenedDate = _stmt.getText(_columnIndexOfLastOpenedDate)
          val _tmpTotalOpenCount: Int
          _tmpTotalOpenCount = _stmt.getLong(_columnIndexOfTotalOpenCount).toInt()
          val _tmpAddedAt: Long
          _tmpAddedAt = _stmt.getLong(_columnIndexOfAddedAt)
          _item =
              BlockedApp(_tmpPackageName,_tmpAppName,_tmpDailyOpenCount,_tmpLastOpenedDate,_tmpTotalOpenCount,_tmpAddedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllBlocked(): List<BlockedApp> {
    val _sql: String = "SELECT * FROM blocked_apps ORDER BY appName ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfPackageName: Int = getColumnIndexOrThrow(_stmt, "packageName")
        val _columnIndexOfAppName: Int = getColumnIndexOrThrow(_stmt, "appName")
        val _columnIndexOfDailyOpenCount: Int = getColumnIndexOrThrow(_stmt, "dailyOpenCount")
        val _columnIndexOfLastOpenedDate: Int = getColumnIndexOrThrow(_stmt, "lastOpenedDate")
        val _columnIndexOfTotalOpenCount: Int = getColumnIndexOrThrow(_stmt, "totalOpenCount")
        val _columnIndexOfAddedAt: Int = getColumnIndexOrThrow(_stmt, "addedAt")
        val _result: MutableList<BlockedApp> = mutableListOf()
        while (_stmt.step()) {
          val _item: BlockedApp
          val _tmpPackageName: String
          _tmpPackageName = _stmt.getText(_columnIndexOfPackageName)
          val _tmpAppName: String
          _tmpAppName = _stmt.getText(_columnIndexOfAppName)
          val _tmpDailyOpenCount: Int
          _tmpDailyOpenCount = _stmt.getLong(_columnIndexOfDailyOpenCount).toInt()
          val _tmpLastOpenedDate: String
          _tmpLastOpenedDate = _stmt.getText(_columnIndexOfLastOpenedDate)
          val _tmpTotalOpenCount: Int
          _tmpTotalOpenCount = _stmt.getLong(_columnIndexOfTotalOpenCount).toInt()
          val _tmpAddedAt: Long
          _tmpAddedAt = _stmt.getLong(_columnIndexOfAddedAt)
          _item =
              BlockedApp(_tmpPackageName,_tmpAppName,_tmpDailyOpenCount,_tmpLastOpenedDate,_tmpTotalOpenCount,_tmpAddedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun isBlocked(pkg: String): Int {
    val _sql: String = "SELECT COUNT(*) FROM blocked_apps WHERE packageName = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, pkg)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLast7Days(pkg: String): List<UsageStat> {
    val _sql: String = "SELECT * FROM usage_stats WHERE packageName = ? ORDER BY date DESC LIMIT 7"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, pkg)
        val _columnIndexOfPackageName: Int = getColumnIndexOrThrow(_stmt, "packageName")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfMinutesUsed: Int = getColumnIndexOrThrow(_stmt, "minutesUsed")
        val _columnIndexOfOpenCount: Int = getColumnIndexOrThrow(_stmt, "openCount")
        val _result: MutableList<UsageStat> = mutableListOf()
        while (_stmt.step()) {
          val _item: UsageStat
          val _tmpPackageName: String
          _tmpPackageName = _stmt.getText(_columnIndexOfPackageName)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpMinutesUsed: Int
          _tmpMinutesUsed = _stmt.getLong(_columnIndexOfMinutesUsed).toInt()
          val _tmpOpenCount: Int
          _tmpOpenCount = _stmt.getLong(_columnIndexOfOpenCount).toInt()
          _item = UsageStat(_tmpPackageName,_tmpDate,_tmpMinutesUsed,_tmpOpenCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeLast7Days(pkg: String): Flow<List<UsageStat>> {
    val _sql: String = "SELECT * FROM usage_stats WHERE packageName = ? ORDER BY date DESC LIMIT 7"
    return createFlow(__db, false, arrayOf("usage_stats")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, pkg)
        val _columnIndexOfPackageName: Int = getColumnIndexOrThrow(_stmt, "packageName")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfMinutesUsed: Int = getColumnIndexOrThrow(_stmt, "minutesUsed")
        val _columnIndexOfOpenCount: Int = getColumnIndexOrThrow(_stmt, "openCount")
        val _result: MutableList<UsageStat> = mutableListOf()
        while (_stmt.step()) {
          val _item: UsageStat
          val _tmpPackageName: String
          _tmpPackageName = _stmt.getText(_columnIndexOfPackageName)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpMinutesUsed: Int
          _tmpMinutesUsed = _stmt.getLong(_columnIndexOfMinutesUsed).toInt()
          val _tmpOpenCount: Int
          _tmpOpenCount = _stmt.getLong(_columnIndexOfOpenCount).toInt()
          _item = UsageStat(_tmpPackageName,_tmpDate,_tmpMinutesUsed,_tmpOpenCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun incrementOpenCount(pkg: String, date: String) {
    val _sql: String =
        "UPDATE blocked_apps SET dailyOpenCount = dailyOpenCount + 1, totalOpenCount = totalOpenCount + 1, lastOpenedDate = ? WHERE packageName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        _argIndex = 2
        _stmt.bindText(_argIndex, pkg)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun resetDailyCount(pkg: String) {
    val _sql: String = "UPDATE blocked_apps SET dailyOpenCount = 0 WHERE packageName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, pkg)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
