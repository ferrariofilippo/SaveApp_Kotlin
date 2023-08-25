package com.ferrariofilippo.saveapp.data

import android.content.Context
import androidx.core.content.ContextCompat
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
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDb(database.tagDao());
                }
            }
        }

        private suspend fun populateDb(dao: TagDao) {
            dao.deleteAll();

            dao.insert(
                Tag(0, cxt.getString(R.string.bets), ContextCompat.getColor(cxt, R.color.yellow_200))
            );
            dao.insert(
                Tag(0, cxt.getString(R.string.clothes), ContextCompat.getColor(cxt, R.color.lime_200))
            );
            dao.insert(
                Tag(0, cxt.getString(R.string.culture), ContextCompat.getColor(cxt, R.color.red_200))
            );
            dao.insert(
                Tag(0, cxt.getString(R.string.entertainment), ContextCompat.getColor(cxt, R.color.fuchsia_200))
            );
            dao.insert(
                Tag(0, cxt.getString(R.string.food), ContextCompat.getColor(cxt, R.color.blue_200))
            );
            dao.insert(
                Tag(0, cxt.getString(R.string.gifts), ContextCompat.getColor(cxt, R.color.purple_200))
            );
            dao.insert(
                Tag(0, cxt.getString(R.string.holidays), ContextCompat.getColor(cxt, R.color.yellow_800))
            );
            dao.insert(
                Tag(0, cxt.getString(R.string.personal_care), ContextCompat.getColor(cxt, R.color.lime_800))
            );
            dao.insert(
                Tag(0, cxt.getString(R.string.others), ContextCompat.getColor(cxt, R.color.red_800))
            );
            dao.insert(
                Tag(0, cxt.getString(R.string.sport), ContextCompat.getColor(cxt, R.color.fuchsia_800))
            );
            dao.insert(
                Tag(0, cxt.getString(R.string.tech), ContextCompat.getColor(cxt, R.color.blue_800))
            );
            dao.insert(
                Tag(0, cxt.getString(R.string.transports), ContextCompat.getColor(cxt, R.color.purple_800))
            );
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null;

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saveapp_database"
                )
                 .addCallback(AppDatabaseCallback(scope, context))
                 .build();

                INSTANCE = instance;

                instance;
            }
        }
    }
}
