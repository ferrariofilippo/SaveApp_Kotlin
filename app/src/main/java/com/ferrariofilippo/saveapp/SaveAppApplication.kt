package com.ferrariofilippo.saveapp

import android.app.Activity
import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.ferrariofilippo.saveapp.data.AppDatabase
import com.ferrariofilippo.saveapp.data.repository.BudgetRepository
import com.ferrariofilippo.saveapp.data.repository.MovementRepository
import com.ferrariofilippo.saveapp.data.repository.SubscriptionRepository
import com.ferrariofilippo.saveapp.data.repository.TagRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class SaveAppApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    private val database by lazy { AppDatabase.getInstance(this, applicationScope) }

    val movementRepository by lazy { MovementRepository(database.movementDao()) }

    val budgetRepository by lazy { BudgetRepository(database.budgetDao()) }

    val subscriptionRepository by lazy { SubscriptionRepository(database.subscriptionDao()) }

    val tagRepository by lazy { TagRepository(database.tagDao()) }

    val ratesStore: DataStore<Preferences> by preferencesDataStore("currencies")

    val settingsStore: DataStore<Preferences> by preferencesDataStore("settings")

    val statsStore: DataStore<Preferences> by preferencesDataStore("stats")

    private var currentActivity: Activity? = null;

    fun getCurrentActivity(): Activity? {
        return currentActivity;
    }

    fun setCurrentActivity(activity: Activity?) {
        currentActivity = activity;
    }
}
