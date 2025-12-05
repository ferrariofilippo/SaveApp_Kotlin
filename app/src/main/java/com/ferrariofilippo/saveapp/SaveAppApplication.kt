// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp

import android.app.Activity
import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.ferrariofilippo.saveapp.data.AppDatabase
import com.ferrariofilippo.saveapp.data.repository.BudgetRepository
import com.ferrariofilippo.saveapp.data.repository.TransactionRepository
import com.ferrariofilippo.saveapp.data.repository.SubscriptionRepository
import com.ferrariofilippo.saveapp.data.repository.TagRepository
import com.ferrariofilippo.saveapp.data.repository.UtilRepository
import com.ferrariofilippo.saveapp.util.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class SaveAppApplication : Application() {
    companion object {
        const val SETTINGS_FILE_NAME = "settings"
    }

    val applicationScope = CoroutineScope(SupervisorJob())

    private val database by lazy { AppDatabase.getInstance(this, applicationScope) }

    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }

    val budgetRepository by lazy { BudgetRepository(database.budgetDao()) }

    val subscriptionRepository by lazy { SubscriptionRepository(database.subscriptionDao()) }

    val tagRepository by lazy { TagRepository(database.tagDao()) }

    val utilRepository by lazy { UtilRepository(database.utilDao()) }

    val ratesStore: DataStore<Preferences> by preferencesDataStore("currencies")

    val settingsStore: DataStore<Preferences> by preferencesDataStore(SETTINGS_FILE_NAME)

    private var currentActivity: Activity? = null

    fun getCurrentActivity(): Activity? {
        return currentActivity
    }

    fun setCurrentActivity(activity: Activity?) {
        currentActivity = activity
    }

    override fun onCreate() {
        super.onCreate()
        LogUtil.setLogFilePath(filesDir.absolutePath)
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            LogUtil.logException(
                e,
                javaClass.kotlin.simpleName ?: "",
                "defaultUncaughtExceptionHandler",
                false
            )
            oldHandler?.uncaughtException(t, e)
            Runtime.getRuntime().exit(1)
        }
    }
}
