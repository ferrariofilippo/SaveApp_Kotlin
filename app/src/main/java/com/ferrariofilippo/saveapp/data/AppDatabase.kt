// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.data.dao.BudgetDao
import com.ferrariofilippo.saveapp.data.dao.SubscriptionDao
import com.ferrariofilippo.saveapp.data.dao.TagDao
import com.ferrariofilippo.saveapp.data.dao.TransactionDao
import com.ferrariofilippo.saveapp.data.dao.UtilDao
import com.ferrariofilippo.saveapp.model.entities.Budget
import com.ferrariofilippo.saveapp.model.entities.Transaction
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.util.ColorUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(
    entities = [Budget::class, Transaction::class, Subscription::class, Tag::class],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao

    abstract fun transactionDao(): TransactionDao

    abstract fun subscriptionDao(): SubscriptionDao

    abstract fun tagDao(): TagDao

    abstract fun utilDao(): UtilDao

    private class AppDatabaseCallback(
        private val scope: CoroutineScope,
        private val cxt: Context
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(scope.coroutineContext, CoroutineStart.UNDISPATCHED) {
                    populateDb(database.tagDao())
                }
            }
        }

        private suspend fun populateDb(dao: TagDao) {
            if (dao.getAll().first().isNotEmpty()) {
                return
            }

            dao.deleteAll()

            dao.insert(
                Tag(0, cxt.getString(R.string.income), cxt.getColor(R.color.emerald_500), true)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.bets), cxt.getColor(R.color.purple_200), false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.clothes), cxt.getColor(R.color.emerald_200), false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.culture), cxt.getColor(R.color.red_200), false)
            )
            dao.insert(
                Tag(
                    0,
                    cxt.getString(R.string.entertainment),
                    cxt.getColor(R.color.green_200),
                    false
                )
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.food), cxt.getColor(R.color.cyan_200), false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.gifts), cxt.getColor(R.color.blue_200), false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.holidays), cxt.getColor(R.color.purple_800), false)
            )
            dao.insert(
                Tag(
                    0,
                    cxt.getString(R.string.personal_care),
                    cxt.getColor(R.color.emerald_800),
                    false
                )
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.others), cxt.getColor(R.color.red_800), false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.sport), cxt.getColor(R.color.green_800), false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.tech), cxt.getColor(R.color.cyan_800), false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.transports), cxt.getColor(R.color.blue_800), false)
            )
        }
    }

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tags ADD COLUMN isIncome INTEGER DEFAULT 0 NOT NULL")
                db.execSQL("UPDATE tags SET isIncome = 1 WHERE id = 1")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE movements RENAME TO transactions")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            var ctx: Context? = null

            override fun migrate(db: SupportSQLiteDatabase) {
                if (ctx == null) {
                    return
                }

                try {
                    val localCtx = ctx!!
                    val tagsCursor = db.query("SELECT id, color FROM tags")
                    val idColorMap: MutableMap<Int, Int> = mutableMapOf()
                    tagsCursor.moveToFirst()
                    while (!tagsCursor.isLast) {
                        idColorMap[tagsCursor.getInt(0)] =
                            ColorUtil.getColorOrDefault(localCtx, tagsCursor.getInt(1))

                        tagsCursor.moveToNext()
                    }
                    tagsCursor.close()

                    for (id in idColorMap.keys) {
                        db.execSQL("UPDATE tags SET color = ${idColorMap[id]} WHERE id = $id")
                    }
                } finally {
                    ctx = null
                }
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        const val DB_NAME = "saveapp_database"

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                MIGRATION_3_4.ctx = context

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(AppDatabaseCallback(scope, context))
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
