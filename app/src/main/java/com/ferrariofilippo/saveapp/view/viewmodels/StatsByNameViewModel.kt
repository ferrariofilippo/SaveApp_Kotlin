// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.ByNameStatsData
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class StatsByNameViewModel(application: Application) : AndroidViewModel(application) {
    private val _app = application as SaveAppApplication

    private val _months = arrayOf(
        _app.getString(R.string.january),
        _app.getString(R.string.february),
        _app.getString(R.string.march),
        _app.getString(R.string.april),
        _app.getString(R.string.may),
        _app.getString(R.string.june),
        _app.getString(R.string.july),
        _app.getString(R.string.august),
        _app.getString(R.string.september),
        _app.getString(R.string.october),
        _app.getString(R.string.november),
        _app.getString(R.string.december),
    )

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

    private val _today = LocalDate.now()

    private val _monthSums: MutableList<Double> = mutableListOf()
    private val _monthFrequencies: MutableList<Int> = mutableListOf()

    private val _isShowingSums: MutableLiveData<Boolean> = MutableLiveData(true)

    private val _showNotFoundMessage: MutableLiveData<Boolean> = MutableLiveData(false)
    val showNotFoundMessage: LiveData<Boolean> = _showNotFoundMessage

    private val _currentMonthFrequency: MutableLiveData<Int> = MutableLiveData(0)
    val currentMonthFrequency: LiveData<Int> = _currentMonthFrequency

    private val _currentMonthSum: MutableLiveData<Double> = MutableLiveData(0.0)
    val currentMonthSum: LiveData<Double> = _currentMonthSum

    private val _currentWeekSum: MutableLiveData<Double> = MutableLiveData(0.0)
    val currentWeekSum: LiveData<Double> = _currentWeekSum

    private val _avgMonthlyFrequency: MutableLiveData<Int> = MutableLiveData(0)
    val avgMonthlyFrequency: LiveData<Int> = _avgMonthlyFrequency

    private val _avgMonthlySum: MutableLiveData<Double> = MutableLiveData(0.0)
    val avgMonthlySum: LiveData<Double> = _avgMonthlySum

    private val _avgValue: MutableLiveData<Double> = MutableLiveData(0.0)
    val avgValue: LiveData<Double> = _avgValue

    private val _graphTitle: MutableLiveData<String> = MutableLiveData("")
    val graphTitle: LiveData<String> = _graphTitle

    private val _hint: MutableLiveData<String> = MutableLiveData("")
    val hint: LiveData<String> = _hint

    private val _symbol = MutableLiveData(Currencies.EUR.toSymbol())
    val symbol: LiveData<String> = _symbol

    val query: MutableLiveData<String> = MutableLiveData("")

    var dataSet: BarDataSet = BarDataSet(listOf(), graphTitle.value)

    var valueFormatter: ValueFormatter

    var onStatsChangeCallback: () -> Unit = { }

    val zero = 0.0

    init {
        for (i in 0..11) {
            _monthSums.add(0, 0.0)
            _monthFrequencies.add(0, 0)
        }

        valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val monthIndex = (value.toInt() + _today.monthValue) % 12
                return _months[monthIndex]
            }
        }

        viewModelScope.launch {
            _symbol.value = Currencies.from(SettingsUtil.getCurrency().first()).toSymbol()
        }

        query.observeForever { q ->
            viewModelScope.launch {
                updateStats(q)
            }
        }
        _isShowingSums.observeForever { value ->
            _graphTitle.value =
                if (value) _app.getString(R.string.sums) else _app.getString(R.string.frequencies)

            viewModelScope.launch {
                updateStats(query.value!!)
            }
        }
    }

    // Methods
    fun setHint(h: String) {
        _hint.value = h
    }

    fun setDisplayType(showSums: Boolean) {
        _isShowingSums.value = showSums
    }

    private suspend fun updateStats(q: String) {
        if (q.isEmpty()) {
            _showNotFoundMessage.value = true
            return
        }

        val stats = ByNameStatsData()
        val transactions = _app.movementRepository.getByDescriptionWithinOneYear(q)
        clearValues()

        if (transactions.isEmpty()) {
            _showNotFoundMessage.value = true
            updateEntries()
        }
        _showNotFoundMessage.value = false

        val minDateTr = transactions.minByOrNull { t -> t.date }
        if (minDateTr == null) {
            _showNotFoundMessage.value = true
            return
        }

        val monthSpan = _today
            .minusYears(minDateTr.date.year.toLong())
            .minusDays(minDateTr.date.dayOfYear.toLong())
            .monthValue

        val oneMonthAgo = _today.minusMonths(1)
        val oneWeekAgo = _today.minusDays(7)
        transactions.forEach { t ->
            if (t.date.isAfter(oneMonthAgo)) {
                stats.currentMonthFrequency++
                stats.currentMonthSum += t.amount
                if (t.date.isAfter(oneWeekAgo)) {
                    stats.currentWeekSum += t.amount
                }
            }

            val index = 11 - (_today.minusMonths(t.date.monthValue.toLong()).monthValue % 12)

            _monthSums[index] += t.amount
            _monthFrequencies[index]++

            stats.frequency++
            stats.transactionSum += t.amount
        }

        _avgValue.value =
            if (stats.frequency == 0) 0.0 else (stats.transactionSum / stats.frequency)
        _currentWeekSum.value = stats.currentWeekSum
        _currentMonthSum.value = stats.currentMonthSum
        _currentMonthFrequency.value = stats.currentMonthFrequency

        if (monthSpan == 0) {
            _avgMonthlySum.value = 0.0
            _avgMonthlyFrequency.value = 0
        } else {
            _avgMonthlySum.value = stats.transactionSum / monthSpan
            _avgMonthlyFrequency.value = stats.frequency / monthSpan
        }

        updateEntries()
    }

    private fun updateEntries() {
        val entries: MutableList<BarEntry> = mutableListOf()

        for (i in 0..11) {
            entries.add(
                BarEntry(
                    i.toFloat(),
                    if (_isShowingSums.value == true)
                        _monthSums[i].toFloat()
                    else
                        _monthFrequencies[i].toFloat()
                )
            )
        }

        dataSet = BarDataSet(entries, _graphTitle.value)
        dataSet.colors = _colors
        dataSet.setDrawValues(false)

        onStatsChangeCallback()
    }

    private fun clearValues() {
        for (i in 0..11) {
            _monthSums[i] = 0.0
            _monthFrequencies[i] = 0
        }
    }
}
