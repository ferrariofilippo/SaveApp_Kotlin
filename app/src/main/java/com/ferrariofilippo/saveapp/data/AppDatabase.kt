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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(entities = [Budget::class, Transaction::class, Subscription::class, Tag::class], version = 3)
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
                Tag(0, cxt.getString(R.string.income), R.color.emerald_500, true)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.bets), R.color.purple_200, false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.clothes), R.color.emerald_200, false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.culture), R.color.red_200, false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.entertainment), R.color.green_200, false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.food), R.color.cyan_200, false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.gifts), R.color.blue_200, false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.holidays), R.color.purple_800, false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.personal_care), R.color.emerald_800, false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.others), R.color.red_800, false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.sport), R.color.green_800, false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.tech), R.color.cyan_800, false)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.transports), R.color.blue_800, false)
            )
        }
    }

    companion object {
        private val MIGRATION_1_2 = object: Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tags ADD COLUMN isIncome INTEGER DEFAULT 0 NOT NULL")
                db.execSQL("UPDATE tags SET isIncome = 1 WHERE id = 1")
            }
        }

        private val MIGRATION_2_3 = object: Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE movements RENAME TO transactions")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        const val DB_NAME = "saveapp_database"

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(AppDatabaseCallback(scope, context))
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
