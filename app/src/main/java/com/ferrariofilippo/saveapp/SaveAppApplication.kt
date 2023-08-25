package com.ferrariofilippo.saveapp

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.ferrariofilippo.saveapp.data.AppDatabase
import com.ferrariofilippo.saveapp.data.repository.BudgetRepository
import com.ferrariofilippo.saveapp.data.repository.MovementRepository
import com.ferrariofilippo.saveapp.data.repository.SubscriptionRepository
import com.ferrariofilippo.saveapp.data.repository.TagRepository
import com.ferrariofilippo.saveapp.util.DataStoreUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class SaveAppApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    private val database by lazy { AppDatabase.getInstance(this, applicationScope) }

    val movementRepository by lazy { MovementRepository(database.movementDao()) }

    val budgetRepository by lazy { BudgetRepository(database.budgetDao()) }

    val subscriptionRepository by lazy { SubscriptionRepository(database.subscriptionDao()) }

    val tagRepository by lazy { TagRepository(database.tagDao()) }

    private var currentActivity: Activity? = null;

    fun getCurrentActivity() : Activity? {
        return currentActivity;
    }

    fun setCurrentActivity(activity: Activity?) {
        currentActivity = activity;
    }
}
