package com.distractblock.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _appDao: Lazy<AppDao> = lazy {
    AppDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "b3912912f887571e87226c5893866e20", "02344820a2400e50f3602133eda2966c") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `blocked_apps` (`packageName` TEXT NOT NULL, `appName` TEXT NOT NULL, `dailyOpenCount` INTEGER NOT NULL, `lastOpenedDate` TEXT NOT NULL, `totalOpenCount` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`packageName`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `usage_stats` (`packageName` TEXT NOT NULL, `date` TEXT NOT NULL, `minutesUsed` INTEGER NOT NULL, `openCount` INTEGER NOT NULL, PRIMARY KEY(`packageName`, `date`), FOREIGN KEY(`packageName`) REFERENCES `blocked_apps`(`packageName`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_usage_stats_packageName` ON `usage_stats` (`packageName`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b3912912f887571e87226c5893866e20')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `blocked_apps`")
        connection.execSQL("DROP TABLE IF EXISTS `usage_stats`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsBlockedApps: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBlockedApps.put("packageName", TableInfo.Column("packageName", "TEXT", true, 1,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBlockedApps.put("appName", TableInfo.Column("appName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBlockedApps.put("dailyOpenCount", TableInfo.Column("dailyOpenCount", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBlockedApps.put("lastOpenedDate", TableInfo.Column("lastOpenedDate", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBlockedApps.put("totalOpenCount", TableInfo.Column("totalOpenCount", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBlockedApps.put("addedAt", TableInfo.Column("addedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBlockedApps: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesBlockedApps: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoBlockedApps: TableInfo = TableInfo("blocked_apps", _columnsBlockedApps,
            _foreignKeysBlockedApps, _indicesBlockedApps)
        val _existingBlockedApps: TableInfo = read(connection, "blocked_apps")
        if (!_infoBlockedApps.equals(_existingBlockedApps)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |blocked_apps(com.distractblock.data.BlockedApp).
              | Expected:
              |""".trimMargin() + _infoBlockedApps + """
              |
              | Found:
              |""".trimMargin() + _existingBlockedApps)
        }
        val _columnsUsageStats: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUsageStats.put("packageName", TableInfo.Column("packageName", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsageStats.put("date", TableInfo.Column("date", "TEXT", true, 2, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsageStats.put("minutesUsed", TableInfo.Column("minutesUsed", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUsageStats.put("openCount", TableInfo.Column("openCount", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUsageStats: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysUsageStats.add(TableInfo.ForeignKey("blocked_apps", "CASCADE", "NO ACTION",
            listOf("packageName"), listOf("packageName")))
        val _indicesUsageStats: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesUsageStats.add(TableInfo.Index("index_usage_stats_packageName", false,
            listOf("packageName"), listOf("ASC")))
        val _infoUsageStats: TableInfo = TableInfo("usage_stats", _columnsUsageStats,
            _foreignKeysUsageStats, _indicesUsageStats)
        val _existingUsageStats: TableInfo = read(connection, "usage_stats")
        if (!_infoUsageStats.equals(_existingUsageStats)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |usage_stats(com.distractblock.data.UsageStat).
              | Expected:
              |""".trimMargin() + _infoUsageStats + """
              |
              | Found:
              |""".trimMargin() + _existingUsageStats)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "blocked_apps", "usage_stats")
  }

  public override fun clearAllTables() {
    super.performClear(true, "blocked_apps", "usage_stats")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(AppDao::class, AppDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun appDao(): AppDao = _appDao.value
}
