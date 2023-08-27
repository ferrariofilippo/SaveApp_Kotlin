package com.ferrariofilippo.saveapp.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Tag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object StatsUtil {
    private var statsStore: DataStore<Preferences>? = null

    var monthKeys: Array<Preferences.Key<Double>> = arrayOf()

    var yearKeys: Array<Preferences.Key<Double>> = arrayOf()

    var lifeKeys: Array<Preferences.Key<Double>> = arrayOf()

    suspend fun init(application: SaveAppApplication) {
        statsStore = application.statsStore

        val tags = application.tagRepository.allTags.first().filter {
            it.name != application.resources.getString(R.string.income)
        }

        monthKeys = createKeys("month", tags)
        yearKeys = createKeys("year", tags)
        lifeKeys = createKeys("life", tags)
    }

    suspend fun setStat(name: String, value: Double) {
        var key: Preferences.Key<Double>? = monthKeys.firstOrNull {
            it.name == name
        }

        if (key == null)
            key = yearKeys.firstOrNull {
                it.name == name
            }

        if (key == null)
            key = lifeKeys.firstOrNull {
                it.name == name
            }

        if (key != null) {
            statsStore!!.edit { pref ->
                pref[key] = value
            }
        }
    }

    fun getMonthStats(): Array<LiveData<Double>> {
        return monthKeys.map {
            statsStore!!.data.map { preferences ->
                preferences[it] ?: 0.0
            }.asLiveData()
        }.toTypedArray()
    }

    fun getYearStats(): Array<LiveData<Double>> {
        return yearKeys.map {
            statsStore!!.data.map { preferences ->
                preferences[it] ?: 0.0
            }.asLiveData()
        }.toTypedArray()
    }

    fun getLifeStats(): Array<LiveData<Double>> {
        return lifeKeys.map {
            statsStore!!.data.map { preferences ->
                preferences[it] ?: 0.0
            }.asLiveData()
        }.toTypedArray()
    }

    private fun createKeys(prefix: String, tags: List<Tag>): Array<Preferences.Key<Double>> {
        val prefs = tags.map {
            doublePreferencesKey("${prefix}_${it.name}")
        }.toMutableList()
        prefs.add(0, doublePreferencesKey("${prefix}_incomes"))
        prefs.add(0, doublePreferencesKey("${prefix}_expenses"))

        return prefs.toTypedArray()
    }
}
