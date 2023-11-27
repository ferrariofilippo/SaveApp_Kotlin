// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import android.content.Context
import android.os.Handler
import android.util.JsonReader
import android.util.JsonWriter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.entities.Tag
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDate

object StatsUtil {
    private const val FILE_NAME = "stats.json"

    private const val LAST_UPDATE = "last_update"
    private const val MONTH_EXPENSES = "month_expenses"
    private const val MONTH_INCOMES = "month_incomes"
    private const val YEAR_EXPENSES = "year_expenses"
    private const val YEAR_INCOMES = "year_incomes"
    private const val LIFE_EXPENSES = "life_expenses"
    private const val LIFE_INCOMES = "life_incomes"
    private const val MONTH_TAGS = "month_tags"
    private const val YEAR_TAGS = "year_tags"
    private const val LIFE_TAGS = "life_tags"

    private var _lastUpdate: LocalDate? = null

    private val _monthExpenses = MutableLiveData(0.0)
    val monthExpenses get(): LiveData<Double> = _monthExpenses

    private val _monthIncomes = MutableLiveData(0.0)
    val monthIncomes get(): LiveData<Double> = _monthIncomes

    private val _yearExpenses = MutableLiveData(0.0)
    val yearExpenses get(): LiveData<Double> = _yearExpenses

    private val _yearIncomes = MutableLiveData(0.0)
    val yearIncomes get(): LiveData<Double> = _yearIncomes

    private val _lifeExpenses = MutableLiveData(0.0)
    val lifeExpenses get(): LiveData<Double> = _lifeExpenses

    private val _lifeIncomes = MutableLiveData(0.0)
    val lifeIncomes get(): LiveData<Double> = _lifeIncomes

    var monthTags: Map<Int, MutableLiveData<Double>> = mapOf()

    var yearTags: Map<Int, MutableLiveData<Double>> = mapOf()

    var lifeTags: Map<Int, MutableLiveData<Double>> = mapOf()

    fun init(application: SaveAppApplication) {
        try {
            application.openFileInput(FILE_NAME).use {
                readJSON(JsonReader(InputStreamReader(it, "UTF-8")))
            }

            if (checkForReset()) {
                application.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
                    writeJSON(JsonWriter(OutputStreamWriter(it, "UTF-8")))
                }
            }
        } catch (_: FileNotFoundException) {
        } finally {
            application.applicationScope.launch {
                application.tagRepository.allTags.collect {
                    val tags = it.filter { tag -> !tag.isIncome  }

                    if (monthTags.isEmpty() || monthTags.size != tags.size) {
                        monthTags = getMapFromTags(tags)
                    }

                    if (yearTags.isEmpty() || yearTags.size != tags.size) {
                        yearTags = getMapFromTags(tags)
                    }

                    if (lifeTags.isEmpty() || lifeTags.size != tags.size) {
                        lifeTags = getMapFromTags(tags)
                    }
                }
            }
        }
    }

    fun applyRateToAll(context: Context, rate: Double) {
        _monthExpenses.value = _monthExpenses.value!! * rate
        _monthIncomes.value = _monthIncomes.value!! * rate
        _yearExpenses.value = _yearExpenses.value!! * rate
        _yearIncomes.value = _yearIncomes.value!! * rate
        _lifeExpenses.value = _lifeExpenses.value!! * rate
        _lifeIncomes.value = _lifeIncomes.value!! * rate

        monthTags.keys.forEach { monthTags[it]!!.value = monthTags[it]!!.value!! * rate }
        yearTags.keys.forEach { monthTags[it]!!.value = yearTags[it]!!.value!! * rate }
        lifeTags.keys.forEach { lifeTags[it]!!.value = lifeTags[it]!!.value!! * rate }

        context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
            writeJSON(JsonWriter(OutputStreamWriter(it, "UTF-8")))
        }
    }

    fun addMovementToStat(context: Context, mov: Movement) {
        val isSameMonth =
            LocalDate.now().month == mov.date.month && LocalDate.now().year == mov.date.year

        val isSameYear = LocalDate.now().year == mov.date.year

        val handler = Handler(context.mainLooper)
        handler.post {
            if (TagUtil.incomeTagIds.contains(mov.tagId)) {
                if (isSameMonth) {
                    _monthIncomes.value = _monthIncomes.value!! + mov.amount
                }

                if (isSameYear) {
                    _yearIncomes.value = _yearIncomes.value!! + mov.amount
                }

                _lifeIncomes.value = _lifeIncomes.value!! + mov.amount
            } else {
                if (isSameMonth) {
                    _monthExpenses.value = _monthExpenses.value!! + mov.amount
                    monthTags[mov.tagId]!!.value = monthTags[mov.tagId]!!.value!! + mov.amount
                }

                if (isSameYear) {
                    _yearExpenses.value = _yearExpenses.value!! + mov.amount
                    yearTags[mov.tagId]!!.value = yearTags[mov.tagId]!!.value!! + mov.amount
                }

                _lifeExpenses.value = _lifeExpenses.value!! + mov.amount
                lifeTags[mov.tagId]!!.value = lifeTags[mov.tagId]!!.value!! + mov.amount
            }

            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
                writeJSON(JsonWriter(OutputStreamWriter(it, "UTF-8")))
            }
        }
    }

    private fun readJSON(reader: JsonReader) {
        reader.use { jsonReader ->
            jsonReader.beginObject()

            while (jsonReader.hasNext()) {
                when (jsonReader.nextName()) {
                    LAST_UPDATE -> _lastUpdate = LocalDate.parse(jsonReader.nextString())
                    MONTH_EXPENSES -> _monthExpenses.value = jsonReader.nextDouble()
                    MONTH_INCOMES -> _monthIncomes.value = jsonReader.nextDouble()
                    YEAR_EXPENSES -> _yearExpenses.value = jsonReader.nextDouble()
                    YEAR_INCOMES -> _yearIncomes.value = jsonReader.nextDouble()
                    LIFE_EXPENSES -> _lifeExpenses.value = jsonReader.nextDouble()
                    LIFE_INCOMES -> _lifeIncomes.value = jsonReader.nextDouble()
                    MONTH_TAGS -> monthTags = readMap(jsonReader)
                    YEAR_TAGS -> yearTags = readMap(jsonReader)
                    LIFE_TAGS -> lifeTags = readMap(jsonReader)
                }
            }

            jsonReader.endObject()
        }
    }

    private fun readMap(reader: JsonReader): Map<Int, MutableLiveData<Double>> {
        val map = mutableMapOf<Int, MutableLiveData<Double>>()

        reader.beginObject()
        while (reader.hasNext()) {
            val id = reader.nextName().toIntOrNull() ?: 0
            val value = reader.nextDouble()
            map[id] = MutableLiveData(value)
        }
        reader.endObject()

        return map.toMap()
    }

    private fun writeJSON(writer: JsonWriter) {
        writer.setIndent("  ")
        writer.beginObject()

        writer.name(LAST_UPDATE).value(LocalDate.now().toString())
        writer.name(MONTH_EXPENSES).value(_monthExpenses.value!!)
        writer.name(MONTH_INCOMES).value(_monthIncomes.value!!)
        writer.name(YEAR_EXPENSES).value(_yearExpenses.value!!)
        writer.name(YEAR_INCOMES).value(_yearIncomes.value!!)
        writer.name(LIFE_EXPENSES).value(_lifeExpenses.value!!)
        writer.name(LIFE_INCOMES).value(_lifeIncomes.value!!)

        writeMap(writer, MONTH_TAGS, monthTags)
        writeMap(writer, YEAR_TAGS, yearTags)
        writeMap(writer, LIFE_TAGS, lifeTags)

        writer.endObject()
        writer.close()
    }

    private fun writeMap(writer: JsonWriter, key: String, map: Map<Int, MutableLiveData<Double>>) {
        writer.name(key)
        writer.beginObject()
        map.keys.forEach {
            writer.name(it.toString()).value(map[it]!!.value!!)
        }
        writer.endObject()
    }

    private fun getMapFromTags(tags: List<Tag>): Map<Int, MutableLiveData<Double>> {
        val map = mutableMapOf<Int, MutableLiveData<Double>>()
        tags.map {
            map[it.id] = MutableLiveData(0.0)
        }

        return map.toMap()
    }

    private fun checkForReset(): Boolean {
        if (_lastUpdate == null) {
            return false
        }

        var updated = false
        if (_lastUpdate!!.monthValue != LocalDate.now().monthValue) {
            _monthExpenses.value = 0.0
            _monthIncomes.value = 0.0
            monthTags.keys.forEach { monthTags[it]!!.value = 0.0 }
            updated = true
        }

        if (_lastUpdate!!.year != LocalDate.now().year) {
            _yearExpenses.value = 0.0
            _yearIncomes.value = 0.0
            yearTags.keys.forEach { yearTags[it]!!.value = 0.0 }
            updated = true
        }

        return updated
    }
}
