package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        OrderEntity::class,
        RepairBookingEntity::class,
        AppSettingsEntity::class,
        ReviewEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reviews ADD COLUMN productId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN videoUrl TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "icase_database"
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.appDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        val dao = database.appDao()
                        val alreadySeeded = dao.getSettingDirect(ScreenGuardCatalog.CATALOG_SEEDED_KEY)?.value == "true"
                        val count = dao.getProductCount()
                        if (!alreadySeeded && count == 0) {
                            populateDatabase(dao)
                        } else {
                            if (!alreadySeeded && count > 0) {
                                dao.insertSetting(AppSettingsEntity(ScreenGuardCatalog.CATALOG_SEEDED_KEY, "true"))
                            }
                            for (setting in InitialData.defaultSettings) {
                                if (dao.getSettingDirect(setting.key) == null) {
                                    dao.insertSetting(setting)
                                }
                            }
                        }
                    }
                }
            }

            private fun parseDeletedProductIds(raw: String?): Set<String> {
                if (raw.isNullOrBlank()) return emptySet()
                val trimmed = raw.trim()
                if (trimmed.startsWith("[")) {
                    try {
                        val arr = org.json.JSONArray(trimmed)
                        return (0 until arr.length())
                            .mapNotNull { arr.optString(it).trim().takeIf { s -> s.isNotEmpty() } }
                            .toSet()
                    } catch (_: Exception) {
                    }
                }
                return trimmed.split(",")
                    .map { it.trim().trim('"', '[', ']') }
                    .filter { it.isNotBlank() }
                    .toSet()
            }

            private suspend fun populateDatabase(dao: AppDao) {
                val deletedRaw = dao.getSettingDirect(ScreenGuardCatalog.DELETED_PRODUCTS_KEY)?.value
                val deletedIds = parseDeletedProductIds(deletedRaw)
                val toSeed = InitialData.products.filterNot { deletedIds.contains(it.id) }
                if (toSeed.isNotEmpty()) {
                    dao.insertAllProducts(toSeed)
                }
                for (order in InitialData.orders) {
                    dao.insertOrder(order)
                }
                for (repair in InitialData.repairs) {
                    dao.insertRepair(repair)
                }
                dao.insertAllSettings(InitialData.defaultSettings)
                dao.insertSetting(AppSettingsEntity(ScreenGuardCatalog.CATALOG_SEEDED_KEY, "true"))
            }
        }
    }
}
