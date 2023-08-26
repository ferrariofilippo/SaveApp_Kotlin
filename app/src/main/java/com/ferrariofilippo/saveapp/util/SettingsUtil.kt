package com.ferrariofilippo.saveapp.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.enums.Currencies
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SettingsUtil {
    private var settingsStore: DataStore<Preferences>? = null

    private val DEFAULT_CURRENCY = intPreferencesKey("default_currency")

    fun init(application: SaveAppApplication) {
        settingsStore = application.settingsStore
    }

    suspend fun setCurrency(newCurrency: Currencies) {
        settingsStore!!.edit { pref ->
            pref[DEFAULT_CURRENCY] = newCurrency.id
        }
    }

    fun getCurrency(): Flow<Int> {
        return settingsStore!!.data.map { preferences ->
            preferences[DEFAULT_CURRENCY] ?: 0
        }
    }
}
