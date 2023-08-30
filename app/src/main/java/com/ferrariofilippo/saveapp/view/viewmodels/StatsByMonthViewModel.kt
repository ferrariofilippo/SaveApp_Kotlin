package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.statsitems.MonthMovementsSum
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class StatsByMonthViewModel(application: Application) : AndroidViewModel(application) {
    private val _app = application as SaveAppApplication

    private val _movements: LiveData<List<TaggedMovement>> =
        _app.movementRepository.allTaggedMovements.asLiveData()

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

    val monthsNames: List<String> = listOf(
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

    var dataSet: PieDataSet = PieDataSet(listOf(), _setLabel)

    var onMovementsChangeCallback: () -> Unit = { }

    init {
        _movements.observeForever(Observer { movements ->
            clearSums()
            movements.let {
                movements.forEach {
                    _monthSums[it.date.monthValue - 1] += it.amount
                }
            }
            updateEntries()
        })
    }

    // Methods
    private fun clearSums() {
        for (i: Int in _monthSums.indices)
            _monthSums[i] = 0.0
    }

    private fun updateEntries() {
        var generalSum = 0.0
        _monthSums.forEach { generalSum += it }

        val entries: MutableList<PieEntry> = mutableListOf()
        val items: MutableList<MonthMovementsSum> = mutableListOf()
        for (i: Int in _monthSums.indices) {
            val sum = _monthSums[i]
            items.add(MonthMovementsSum(monthsNames[i], sum))
            if (sum != 0.0)
                entries.add(PieEntry((sum * 100.0 / generalSum).toFloat(), monthsNames[i]))
        }

        _monthSumItems.value = items.toList()

        dataSet = PieDataSet(entries, _setLabel)
        dataSet.colors = _colors
        dataSet.valueFormatter = PercentFormatter()

        onMovementsChangeCallback()
    }
}
