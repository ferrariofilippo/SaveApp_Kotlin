// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.JsonReader
import android.util.JsonWriter
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.entities.Transaction
import com.ferrariofilippo.saveapp.util.TagUtil
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDate

class SummaryStatistics {
    companion object {
        const val FILE_NAME = "stats.json"

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
    }

    var date: LocalDate? = null
    var monthExpenses: Double = 0.0
    var monthIncomes: Double = 0.0
    var yearExpenses: Double = 0.0
    var yearIncomes: Double = 0.0
    var lifeExpenses: Double = 0.0
    var lifeIncomes: Double = 0.0
    val monthTags: MutableMap<Int, Double> = mutableMapOf()
    val yearTags: MutableMap<Int, Double> = mutableMapOf()
    val lifeTags: MutableMap<Int, Double> = mutableMapOf()

    fun addTransaction(t: Transaction) {
        val isSameYear = LocalDate.now().year == t.date.year
        val isSameMonth = LocalDate.now().month == t.date.month && isSameYear

        if (TagUtil.incomeTagIds.contains(t.tagId)) {
            if (isSameYear) {
                yearIncomes += t.amount

                if (isSameMonth) {
                    monthIncomes += t.amount
                }
            }

            lifeIncomes += t.amount
        } else {
            val rootTagId = TagUtil.getTagRootId(t.tagId)
            if (isSameYear) {
                yearExpenses += t.amount
                yearTags[rootTagId] = (yearTags[rootTagId] ?: 0.0) + t.amount

                if (isSameMonth) {
                    monthExpenses += t.amount
                    monthTags[rootTagId] = (monthTags[rootTagId] ?: 0.0) + t.amount
                }
            }

            lifeExpenses += t.amount
            lifeTags[rootTagId] = (lifeTags[rootTagId] ?: 0.0) + t.amount
        }
    }

    fun saveChanges(ctx: Context) {
        ctx.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
            writeJSON(JsonWriter(OutputStreamWriter(it, "UTF-8")))
        }
    }

    fun loadData(application: SaveAppApplication, tagsChangeCallback: () -> Unit) {
        try {
            application.openFileInput(FILE_NAME).use {
                readJSON(JsonReader(InputStreamReader(it, "UTF-8")))
            }

            if (checkForReset()) {
                saveChanges(application)
            }
        } catch (_: IOException) {
        } finally {
            application.applicationScope.launch {
                application.tagRepository.allTags.collect {
                    val tags = it.filter { tag -> !tag.isIncome && tag.parentTagId == 0 }
                    updateTagsMap(monthTags, tags)
                    updateTagsMap(yearTags, tags)
                    updateTagsMap(lifeTags, tags)

                    Handler(Looper.getMainLooper()).post {
                        tagsChangeCallback()
                    }
                }
            }
        }
    }

    private fun writeJSON(writer: JsonWriter) {
        writer.setIndent("  ")
        writer.beginObject()

        writer.name(LAST_UPDATE).value(LocalDate.now().toString())
        writer.name(MONTH_EXPENSES).value(monthExpenses)
        writer.name(MONTH_INCOMES).value(monthIncomes)
        writer.name(YEAR_EXPENSES).value(yearExpenses)
        writer.name(YEAR_INCOMES).value(yearIncomes)
        writer.name(LIFE_EXPENSES).value(lifeExpenses)
        writer.name(LIFE_INCOMES).value(lifeIncomes)

        writeMap(writer, MONTH_TAGS, monthTags)
        writeMap(writer, YEAR_TAGS, yearTags)
        writeMap(writer, LIFE_TAGS, lifeTags)

        writer.endObject()
        writer.close()
    }

    private fun writeMap(writer: JsonWriter, key: String, map: Map<Int, Double>) {
        writer.name(key)
        writer.beginObject()
        map.keys.forEach {
            writer.name(it.toString()).value(map[it]!!)
        }
        writer.endObject()
    }

    private fun readJSON(reader: JsonReader) {
        reader.use { jsonReader ->
            jsonReader.beginObject()

            while (jsonReader.hasNext()) {
                when (jsonReader.nextName()) {
                    LAST_UPDATE -> date = LocalDate.parse(jsonReader.nextString())
                    MONTH_EXPENSES -> monthExpenses = jsonReader.nextDouble()
                    MONTH_INCOMES -> monthIncomes = jsonReader.nextDouble()
                    YEAR_EXPENSES -> yearExpenses = jsonReader.nextDouble()
                    YEAR_INCOMES -> yearIncomes = jsonReader.nextDouble()
                    LIFE_EXPENSES -> lifeExpenses = jsonReader.nextDouble()
                    LIFE_INCOMES -> lifeIncomes = jsonReader.nextDouble()
                    MONTH_TAGS -> readMap(jsonReader, monthTags)
                    YEAR_TAGS -> readMap(jsonReader, yearTags)
                    LIFE_TAGS -> readMap(jsonReader, lifeTags)
                }
            }

            jsonReader.endObject()
        }
    }

    private fun readMap(reader: JsonReader, outMap: MutableMap<Int, Double>) {
        reader.beginObject()
        while (reader.hasNext()) {
            val id = reader.nextName().toIntOrNull() ?: 0
            val value = reader.nextDouble()
            outMap[id] = value
        }
        reader.endObject()
    }

    private fun updateTagsMap(tagsMap: MutableMap<Int, Double>, tags: List<Tag>) {
        if (tagsMap.isNotEmpty() && tagsMap.size == tags.size) {
            return
        }

        tags.map {
            if (!tagsMap.containsKey(it.id)) {
                tagsMap[it.id] = 0.0
            }
        }
    }

    private fun checkForReset(): Boolean {
        if (date == null) {
            return false
        }

        var updated = false
        if (date!!.monthValue != LocalDate.now().monthValue) {
            monthExpenses = 0.0
            monthIncomes = 0.0
            monthTags.keys.forEach { monthTags[it] = 0.0 }
            updated = true
        }

        if (date!!.year != LocalDate.now().year) {
            yearExpenses = 0.0
            yearIncomes = 0.0
            yearTags.keys.forEach { yearTags[it] = 0.0 }
            updated = true
        }

        return updated
    }
}
