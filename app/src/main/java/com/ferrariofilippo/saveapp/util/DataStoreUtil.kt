package com.ferrariofilippo.saveapp.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.enums.Currencies
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreUtil(context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private val dataStore = context.dataStore;

    companion object {
        val DEFAULT_CURRENCY = intPreferencesKey("currency")
    }

    suspend fun setCurrency(newCurrency: Currencies) {
        dataStore.edit { pref ->
            pref[DEFAULT_CURRENCY] = newCurrency.id
        }
    }

    suspend fun getCurrency() : Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[DEFAULT_CURRENCY] ?: 0
        }
    }
}
