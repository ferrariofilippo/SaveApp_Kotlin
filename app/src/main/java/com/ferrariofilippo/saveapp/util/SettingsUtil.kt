// Copyright (c) 2025 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.enums.SaveAppThemes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object SettingsUtil {
    private val DEFAULT_CURRENCY = intPreferencesKey("default_currency")
    private val LAST_BACKUP_TIME_STAMP = stringPreferencesKey("last_backup_time_stamp")

    private val USE_COMPACT_MODE = booleanPreferencesKey("use_compact_mode")
    private val ENABLE_FORMULAS = booleanPreferencesKey("enable_formulas")
    private val USER_THEME = intPreferencesKey("user_theme")

    private val SUMMARY_INTEGRITY_CHECK_INTERVAL =
        longPreferencesKey("summary_integrity_check_interval")
    private val PERIODIC_BACKUP_VISIBLE = booleanPreferencesKey("periodic_backup_visible")
    private val PERIODIC_BACKUP_UPLOAD = booleanPreferencesKey("periodic_backup_upload")
    private val PERIODIC_BACKUP_INTERVAL = longPreferencesKey("periodic_backup_interval")
    private val PERIODIC_BACKUP_REQUIRES_WIFI =
        booleanPreferencesKey("periodic_backup_requires_wifi")

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

    suspend fun setEnableFormulas(enableFormulas: Boolean) {
        settingsStore!!.edit { pref -> pref[ENABLE_FORMULAS] = enableFormulas }
    }

    fun getEnableFormulas(): Flow<Boolean> {
        return settingsStore!!.data
            .catch { emit(emptyPreferences()) }
            .map { preferences -> preferences[ENABLE_FORMULAS] ?: false }
    }

    suspend fun setTheme(theme: SaveAppThemes) {
        settingsStore!!.edit { pref ->
            pref[USER_THEME] = theme.id
        }
    }

    fun getTheme(): Flow<SaveAppThemes> {
        return settingsStore!!.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                SaveAppThemes.from(preferences[USER_THEME] ?: 0)
            }
    }

    suspend fun setSummaryIntegrityCheckInterval(interval: Long) {
        settingsStore!!.edit { pref ->
            pref[SUMMARY_INTEGRITY_CHECK_INTERVAL] = interval
        }
    }

    fun getSummaryIntegrityCheckInterval(): Flow<Long> {
        return settingsStore!!.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[SUMMARY_INTEGRITY_CHECK_INTERVAL] ?: (24 * 60)
            }
    }

    suspend fun setPeriodicBackupVisible(enable: Boolean) {
        settingsStore!!.edit { pref ->
            pref[PERIODIC_BACKUP_VISIBLE] = enable
        }
    }

    fun getPeriodicBackupVisible(): Flow<Boolean> {
        return settingsStore!!.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[PERIODIC_BACKUP_VISIBLE] ?: false
            }
    }

    suspend fun setPeriodicBackupUpload(enable: Boolean) {
        settingsStore!!.edit { pref ->
            pref[PERIODIC_BACKUP_UPLOAD] = enable
        }
    }

    fun getPeriodicBackupUpload(): Flow<Boolean> {
        return settingsStore!!.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[PERIODIC_BACKUP_UPLOAD] ?: false
            }
    }

    suspend fun setPeriodicBackupInterval(interval: Long) {
        settingsStore!!.edit { pref ->
            pref[PERIODIC_BACKUP_INTERVAL] = interval
        }
    }

    fun getPeriodicBackupInterval(): Flow<Long> {
        return settingsStore!!.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[PERIODIC_BACKUP_INTERVAL] ?: (7 * 24 * 60)
            }
    }

    suspend fun setPeriodicBackupRequiresWiFi(requireWiFi: Boolean) {
        settingsStore!!.edit { pref ->
            pref[PERIODIC_BACKUP_REQUIRES_WIFI] = requireWiFi
        }
    }

    fun getPeriodicBackupRequiresWiFi(): Flow<Boolean> {
        return settingsStore!!.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[PERIODIC_BACKUP_REQUIRES_WIFI] ?: true
            }
    }
}
