// Copyright (c) 2024 Filippo Ferrario
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
import com.ferrariofilippo.saveapp.model.statsitems.MonthTransactionsSum
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedTransaction
import com.ferrariofilippo.saveapp.util.TagUtil
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate

class StatsByMonthViewModel(application: Application) : AndroidViewModel(application) {
    private val _app = application as SaveAppApplication

    private var _transactions: List<TaggedTransaction> = listOf()

    private val _monthSums: MutableList<Double> =
        mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

    private var _setLabel: String = application.getString(R.string.expenses_by_month)

    private val _isShowingExpenses: MutableLiveData<Boolean> = MutableLiveData(true)

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

    private val _monthSumItems: MutableLiveData<List<MonthTransactionsSum>> = MutableLiveData(listOf())
    val monthSumItems get(): LiveData<List<MonthTransactionsSum>> = _monthSumItems

    private val _year: MutableLiveData<String> = MutableLiveData(LocalDate.now().year.toString())
    val year: LiveData<String> = _year

    private val _showEmptyMessage: MutableLiveData<Boolean> = MutableLiveData(false)
    val showEmptyMessage: LiveData<Boolean> = _showEmptyMessage

    var years: List<String> = listOf()

    var dataSet: PieDataSet = PieDataSet(listOf(), _setLabel)

    var onTransactionsChangeCallback: () -> Unit = { }

    init {
        initYears()
        _year.observeForever { value ->
            viewModelScope.launch {
                updateTransactions(value)
            }
        }
        _isShowingExpenses.observeForever { value ->
            _setLabel = if (value)
                application.getString(R.string.expenses_by_month)
            else
                application.getString(R.string.incomes_by_month)

            viewModelScope.launch {
                updateTransactions(year.value!!)
            }
        }
    }

    // Methods
    fun setYear(value: String) {
        _year.value = value
    }

    fun setType(isExpenses: Boolean) {
        _isShowingExpenses.value = isExpenses
    }

    private suspend fun updateTransactions(year: String) {
        _transactions = _app.transactionRepository.getAllTaggedByYear(year)
        _showEmptyMessage.value = _transactions.isEmpty()
        clearSums()
        _transactions.forEach {
            if (_isShowingExpenses.value!! xor TagUtil.incomeTagIds.contains(it.tagId)) {
                _monthSums[it.date.monthValue - 1] += it.amount
            }
        }
        updateEntries()
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
        val items: MutableList<MonthTransactionsSum> = mutableListOf()
        for (i: Int in _monthSums.indices) {
            val sum = _monthSums[i]
            val percentage = if (generalSum != 0.0) sum * 100.0 / generalSum else 0.0
            items.add(MonthTransactionsSum(monthsNames[i], sum, percentage))
            if (sum != 0.0) {
                entries.add(PieEntry(percentage.toFloat(), monthsNames[i]))
            }
        }

        _monthSumItems.value = items.toList()

        dataSet = PieDataSet(entries, _setLabel)
        dataSet.colors = _colors
        dataSet.valueFormatter = PercentFormatter()

        onTransactionsChangeCallback
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
