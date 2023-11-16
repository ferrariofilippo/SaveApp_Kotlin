package com.ferrariofilippo.saveapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.data.dao.BudgetDao
import com.ferrariofilippo.saveapp.data.dao.MovementDao
import com.ferrariofilippo.saveapp.data.dao.SubscriptionDao
import com.ferrariofilippo.saveapp.data.dao.TagDao
import com.ferrariofilippo.saveapp.model.entities.Budget
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.entities.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

@Database(entities = [Budget::class, Movement::class, Subscription::class, Tag::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao

    abstract fun movementDao(): MovementDao

    abstract fun subscriptionDao(): SubscriptionDao

    abstract fun tagDao(): TagDao

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
            dao.deleteAll()

            dao.insert(
                Tag(0, cxt.getString(R.string.income), R.color.emerald_500)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.bets), R.color.purple_200)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.clothes), R.color.emerald_200)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.culture), R.color.red_200)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.entertainment), R.color.green_200)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.food), R.color.cyan_200)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.gifts), R.color.blue_200)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.holidays), R.color.purple_800)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.personal_care), R.color.emerald_800)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.others), R.color.red_800)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.sport), R.color.green_800)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.tech), R.color.cyan_800)
            )
            dao.insert(
                Tag(0, cxt.getString(R.string.transports), R.color.blue_800)
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saveapp_database"
                )
                    .addCallback(AppDatabaseCallback(scope, context))
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
