package com.ferrariofilippo.saveapp.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.enums.Currencies
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

object SettingsUtil {
    private var settingsStore: DataStore<Preferences>? = null

    private val DEFAULT_CURRENCY = intPreferencesKey("default_currency")

    fun setStore(application: SaveAppApplication) {
        settingsStore = application.settingsStore
    }

    suspend fun setCurrency(newCurrency: Currencies) {
        settingsStore!!.edit { pref ->
            pref[DEFAULT_CURRENCY] = newCurrency.id
        }
    }

    fun getCurrency(): LiveData<Int> {
        return settingsStore!!.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[DEFAULT_CURRENCY] ?: 0
            }.asLiveData()
    }
}
