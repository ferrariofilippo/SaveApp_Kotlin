// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.enums.Currencies
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object SettingsUtil {
    private val DEFAULT_CURRENCY = intPreferencesKey("default_currency")
    private val LAST_BACKUP_TIME_STAMP = stringPreferencesKey("last_backup_time_stamp")
    private val USE_COMPACT_MODE = booleanPreferencesKey("use_compact_mode")

    private var settingsStore: DataStore<Preferences>? = null

    var lastBackupTimeStamp: String = ""

    fun setStore(application: SaveAppApplication) {
        settingsStore = application.settingsStore

        application.applicationScope.launch {
            getLastBackupTimeStamp().collect {
                lastBackupTimeStamp = it
            }
        }
    }

    suspend fun setCurrency(newCurrency: Currencies) {
        settingsStore!!.edit { pref ->
            pref[DEFAULT_CURRENCY] = newCurrency.id
        }
    }

    fun getCurrency(): Flow<Int> {
        return settingsStore!!.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[DEFAULT_CURRENCY] ?: 0
            }
    }

    suspend fun setLastBackupTimeStamp(timestamp: String) {
        settingsStore!!.edit { pref ->
            pref[LAST_BACKUP_TIME_STAMP] = timestamp
        }
    }

    fun getLastBackupTimeStamp(): Flow<String> {
        return settingsStore!!.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[LAST_BACKUP_TIME_STAMP] ?: ""
            }
    }

    suspend fun setUseCompactMode(useCompactMode: Boolean) {
        settingsStore!!.edit { pref ->
            pref[USE_COMPACT_MODE] = useCompactMode
        }
    }

    fun getUseCompactMode(): Flow<Boolean> {
        return settingsStore!!.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[USE_COMPACT_MODE] ?: false
            }
    }
}
