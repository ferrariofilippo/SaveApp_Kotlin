package com.ferrariofilippo.saveapp.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.data.repository.TagRepository
import com.ferrariofilippo.saveapp.model.entities.Tag
import kotlinx.coroutines.flow.map

object StatsUtil {
    private var statsStore: DataStore<Preferences>? = null

    private var tagRepository: TagRepository? = null

    private var incomeStr: String = ""

    val monthExpensesKey = doublePreferencesKey("month_expenses")
    val monthIncomesKey = doublePreferencesKey("month_incomes")
    val yearExpensesKey = doublePreferencesKey("year_expenses")
    val yearIncomesKey = doublePreferencesKey("year_incomes")
    val lifeExpensesKey = doublePreferencesKey("life_expenses")
    val lifeIncomesKey = doublePreferencesKey("life_incomes")

    var monthKeys: Array<Preferences.Key<Double>> = arrayOf()

    var yearKeys: Array<Preferences.Key<Double>> = arrayOf()

    var lifeKeys: Array<Preferences.Key<Double>> = arrayOf()

    fun init(application: SaveAppApplication) {
        statsStore = application.statsStore
        tagRepository = application.tagRepository
        incomeStr = application.getString(R.string.income)

        val tags = tagRepository!!.allTags.asLiveData().value?.filter {
            it.name != incomeStr
        }

        tags ?: return
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

    fun getStat(key: Preferences.Key<Double>): LiveData<Double> {
        return statsStore!!.data.map { preferences ->
            preferences[key] ?: 0.0
        }.asLiveData()
    }

    fun getTagsStats(keys: Array<Preferences.Key<Double>>): Array<LiveData<Double>> {
        return keys.map {
            statsStore!!.data.map { preferences ->
                preferences[it] ?: 0.0
            }.asLiveData()
        }.toTypedArray()
    }

    private fun createKeys(prefix: String, tags: List<Tag>): Array<Preferences.Key<Double>> {
        return tags.map {
            doublePreferencesKey("${prefix}_${it.name}")
        }.toTypedArray()
    }
}
