package com.ferrariofilippo.saveapp.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.data.repository.TagRepository
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.entities.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

object StatsUtil {
    private const val INCOME_TAG_ID = 1

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

    suspend fun init(application: SaveAppApplication) {
        statsStore = application.statsStore
        tagRepository = application.tagRepository
        incomeStr = application.getString(R.string.income)

        val tags = tagRepository!!.allTags.first().filter {
            it.name != incomeStr
        }

        monthKeys = createKeys("month", tags)
        yearKeys = createKeys("year", tags)
        lifeKeys = createKeys("life", tags)
    }

    suspend fun setStat(key: Preferences.Key<Double>, value: Double) {
        statsStore!!.edit { pref ->
            pref[key] = value
        }
    }

    suspend fun addMovementToStat(movement: Movement, tag: String?) {
        if (LocalDate.now().month == movement.date.month)
            addMovementToStat(movement, tag, monthIncomesKey, monthExpensesKey, "month")

        if (LocalDate.now().year == movement.date.year)
            addMovementToStat(movement, tag, yearIncomesKey, yearExpensesKey, "year")

        addMovementToStat(movement, tag, lifeIncomesKey, lifeExpensesKey, "life")
    }

    fun getStat(key: Preferences.Key<Double>): Flow<Double> {
        return statsStore!!.data.map { preferences ->
            preferences[key] ?: 0.0
        }
    }

    fun getTagsStats(keys: Array<Preferences.Key<Double>>): Array<Flow<Double>> {
        return keys.map {
            statsStore!!.data.map { preferences ->
                preferences[it] ?: 0.0
            }
        }.toTypedArray()
    }

    private fun createKeys(prefix: String, tags: List<Tag>): Array<Preferences.Key<Double>> {
        return tags.map {
            doublePreferencesKey("${prefix}_${it.name}")
        }.toTypedArray()
    }

    private suspend fun addToStat(key: Preferences.Key<Double>, value: Double) {
        val oldValue = getStat(key).first()
        setStat(key, oldValue + value)
    }

    private suspend fun addToStat(name: String, value: Double) {
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

        if (key != null)
            addToStat(key, value)
    }

    private suspend fun addMovementToStat(
        movement: Movement,
        tag: String?,
        incomesKey: Preferences.Key<Double>,
        expensesKey: Preferences.Key<Double>,
        tagKeyPrefix: String
    ) {
        if (movement.tagId == INCOME_TAG_ID) {
            addToStat(incomesKey, movement.amount)
        } else {
            addToStat(expensesKey, movement.amount)

            if (tag != null)
                addToStat("${tagKeyPrefix}_${tag}", movement.amount)
        }
    }
}
