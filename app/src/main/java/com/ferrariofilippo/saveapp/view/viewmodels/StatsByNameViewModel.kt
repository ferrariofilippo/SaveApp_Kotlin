// Copyright (c) 2025 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Transaction
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.statsitems.ByNameStats
import com.ferrariofilippo.saveapp.model.statsitems.ByNameStatsValues
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

    // Observers
    private val _queryObserver = Observer<String> { value ->
        viewModelScope.launch {
            updateStats(value)
        }
    }

    private val _showSumsObserver = Observer<Boolean> { value ->
        _graphTitle.value =
            if (value) _app.getString(R.string.sum) else _app.getString(R.string.count)

        viewModelScope.launch {
            updateStats(query.value!!)
        }
    }

    // UI
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

    private val _avgMonthlyCount: MutableLiveData<Int> = MutableLiveData(0)
    val avgMonthlyCount: LiveData<Int> = _avgMonthlyCount

    private val _avgMonthlySum: MutableLiveData<Double> = MutableLiveData(0.0)
    val avgMonthlySum: LiveData<Double> = _avgMonthlySum

    private val _graphTitle: MutableLiveData<String> = MutableLiveData("")
    val graphTitle: LiveData<String> = _graphTitle

    private val _hint: MutableLiveData<String> =
        MutableLiveData(application.getString(R.string.searchbar_hint))
    val hint: LiveData<String> = _hint

    private val _lastWeekSectionCollapsed = MutableLiveData(false)
    val lastWeekSectionCollapsed: LiveData<Boolean> = _lastWeekSectionCollapsed

    private val _lastMonthSectionCollapsed = MutableLiveData(true)
    val lastMonthSectionCollapsed: LiveData<Boolean> = _lastMonthSectionCollapsed

    private val _lastYearSectionCollapsed = MutableLiveData(true)
    val lastYearSectionCollapsed: LiveData<Boolean> = _lastYearSectionCollapsed

    private val _graphSectionCollapsed = MutableLiveData(true)
    val graphSectionCollapsed: LiveData<Boolean> = _graphSectionCollapsed

    private val _symbol = MutableLiveData(Currencies.EUR.toSymbol())
    val symbol: LiveData<String> = _symbol

    val query: MutableLiveData<String> = MutableLiveData("")

    val lastWeekStats = ByNameStats()

    val lastMonthStats = ByNameStats()

    val lastYearStats = ByNameStats()

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

        query.observeForever(_queryObserver)
        _isShowingSums.observeForever(_showSumsObserver)
    }

    // Overrides
    override fun onCleared() {
        query.removeObserver(_queryObserver)
        _isShowingSums.removeObserver(_showSumsObserver)
        super.onCleared()
    }

    // Methods
    fun setHint(h: String) {
        _hint.value = h
    }

    fun setDisplayType(showSums: Boolean) {
        _isShowingSums.value = showSums
    }

    fun changeLastWeekSectionVisibility() {
        _lastWeekSectionCollapsed.value = !_lastWeekSectionCollapsed.value!!
    }

    fun changeLastMonthSectionVisibility() {
        _lastMonthSectionCollapsed.value = !_lastMonthSectionCollapsed.value!!
    }

    fun changeLastYearSectionVisibility() {
        _lastYearSectionCollapsed.value = !_lastYearSectionCollapsed.value!!
    }

    fun changeGraphSectionVisibility() {
        _graphSectionCollapsed.value = !_graphSectionCollapsed.value!!
    }

    private suspend fun updateStats(q: String) {
        if (q.isEmpty()) {
            _showNotFoundMessage.value = true
            return
        }

        val transactions = _app.transactionRepository.getByDescriptionWithinOneYear(q)
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

        val stats = processTransactions(transactions)
        lastWeekStats.setSum(stats.weekSum)
        lastWeekStats.setAvg(if (stats.weekCount == 0) 0.0 else (stats.weekSum / stats.weekCount))
        lastWeekStats.setCount(stats.weekCount)
        lastWeekStats.setMax(stats.weekMax)
        lastWeekStats.setMin(if (stats.weekMin == Double.MAX_VALUE) 0.0 else stats.weekMin)

        lastMonthStats.setSum(stats.monthSum)
        lastMonthStats.setAvg(if (stats.monthCount == 0) 0.0 else (stats.monthSum / stats.monthCount))
        lastMonthStats.setCount(stats.monthCount)
        lastMonthStats.setMax(stats.monthMax)
        lastMonthStats.setMin(if (stats.monthMin == Double.MAX_VALUE) 0.0 else stats.monthMin)

        lastYearStats.setSum(stats.yearSum)
        lastYearStats.setAvg(if (stats.yearCount == 0) 0.0 else (stats.yearSum / stats.yearCount))
        lastYearStats.setCount(stats.yearCount)
        lastYearStats.setMax(stats.yearMax)
        lastYearStats.setMin(if (stats.yearMin == Double.MAX_VALUE) 0.0 else stats.yearMin)

        val monthSpan = _today
            .minusYears(minDateTr.date.year.toLong())
            .minusDays(minDateTr.date.dayOfYear.toLong())
            .monthValue

        if (monthSpan == 0) {
            _avgMonthlySum.value = 0.0
            _avgMonthlyCount.value = 0
        } else {
            _avgMonthlySum.value = stats.yearSum / monthSpan
            _avgMonthlyCount.value = stats.yearCount / monthSpan
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

    private fun processTransactions(transactions: List<Transaction>): ByNameStatsValues {
        val stats = ByNameStatsValues()
        val oneMonthAgo = _today.minusMonths(1)
        val oneWeekAgo = _today.minusDays(7)

        transactions.forEach { t ->
            if (t.date.isAfter(oneMonthAgo)) {
                stats.monthSum += t.amount
                stats.monthCount++

                if (t.amount < stats.monthMin) {
                    stats.monthMin = t.amount
                }
                if (t.amount > stats.monthMax) {
                    stats.monthMax = t.amount
                }

                if (t.date.isAfter(oneWeekAgo)) {
                    stats.weekSum += t.amount
                    stats.weekCount++

                    if (t.amount < stats.weekMin) {
                        stats.weekMin = t.amount
                    }
                    if (t.amount > stats.weekMax) {
                        stats.weekMax = t.amount
                    }
                }
            }

            val index = 11 - (_today.minusMonths(t.date.monthValue.toLong()).monthValue % 12)
            _monthSums[index] += t.amount
            _monthFrequencies[index]++

            stats.yearSum += t.amount
            stats.yearCount++

            if (t.amount < stats.yearMin) {
                stats.yearMin = t.amount
            }
            if (t.amount > stats.yearMax) {
                stats.yearMax = t.amount
            }
        }

        return stats
    }
}
