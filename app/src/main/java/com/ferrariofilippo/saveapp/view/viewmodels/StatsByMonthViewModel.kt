// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.statsitems.MonthMovementsSum
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate

class StatsByMonthViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val INCOME_ID = 1
    }

    private val _app = application as SaveAppApplication

    private var _movements: List<TaggedMovement> = listOf()

    private val _monthSums: MutableList<Double> =
        mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

    private val _setLabel: String = application.getString(R.string.expenses_by_month)

    private val _colors: List<Int> = listOf(
        _app.getColor(R.color.purple_200),
        _app.getColor(R.color.emerald_200),
        _app.getColor(R.color.green_200),
        _app.getColor(R.color.blue_200),
        _app.getColor(R.color.red_200),
        _app.getColor(R.color.cyan_200),
        _app.getColor(R.color.purple_800),
        _app.getColor(R.color.emerald_800),
        _app.getColor(R.color.green_800),
        _app.getColor(R.color.blue_800),
        _app.getColor(R.color.red_800),
        _app.getColor(R.color.cyan_800)
    )

    private val monthsNames: List<String> = listOf(
        _app.getString(R.string.january_name),
        _app.getString(R.string.february_name),
        _app.getString(R.string.march_name),
        _app.getString(R.string.april_name),
        _app.getString(R.string.may_name),
        _app.getString(R.string.june_name),
        _app.getString(R.string.july_name),
        _app.getString(R.string.august_name),
        _app.getString(R.string.september_name),
        _app.getString(R.string.october_name),
        _app.getString(R.string.november_name),
        _app.getString(R.string.december_name)
    )

    private val _monthSumItems: MutableLiveData<List<MonthMovementsSum>> = MutableLiveData(listOf())
    val monthSumItems get(): LiveData<List<MonthMovementsSum>> = _monthSumItems

    private val _year: MutableLiveData<String> = MutableLiveData(LocalDate.now().year.toString())
    val year: LiveData<String> = _year

    private val _showEmptyMessage: MutableLiveData<Boolean> = MutableLiveData(false)
    val showEmptyMessage: LiveData<Boolean> = _showEmptyMessage

    var years: List<String> = listOf()

    var dataSet: PieDataSet = PieDataSet(listOf(), _setLabel)

    var onMovementsChangeCallback: () -> Unit = { }

    init {
        initYears()
        _year.observeForever { value ->
            viewModelScope.launch {
                _movements = _app.movementRepository.getAllTaggedByYear(value)
                _showEmptyMessage.value = _movements.isEmpty()
                clearSums()
                _movements.forEach {
                    if (it.tagId != INCOME_ID) {
                        _monthSums[it.date.monthValue - 1] += it.amount
                    }
                }
                updateEntries()
            }
        }
    }

    // Methods
    fun setYear(value: String) {
        _year.value = value
    }

    private fun clearSums() {
        for (i: Int in _monthSums.indices) {
            _monthSums[i] = 0.0
        }
    }

    private fun updateEntries() {
        var generalSum = 0.0
        _monthSums.forEach { generalSum += it }

        val entries: MutableList<PieEntry> = mutableListOf()
        val items: MutableList<MonthMovementsSum> = mutableListOf()
        for (i: Int in _monthSums.indices) {
            val sum = _monthSums[i]
            val percentage = if (generalSum != 0.0) sum * 100.0 / generalSum else 0.0
            items.add(MonthMovementsSum(monthsNames[i], sum, percentage))
            if (sum != 0.0) {
                entries.add(PieEntry(percentage.toFloat(), monthsNames[i]))
            }
        }

        _monthSumItems.value = items.toList()

        dataSet = PieDataSet(entries, _setLabel)
        dataSet.colors = _colors
        dataSet.valueFormatter = PercentFormatter()

        onMovementsChangeCallback()
    }

    private fun initYears() {
        val values = mutableStateListOf<String>()
        val currentYear = LocalDate.now().year
        for (i: Int in 0 until 7) {
            values.add((currentYear - i).toString())
        }

        years = values.toList()
    }
}
