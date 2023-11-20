// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.statsitems.YearStats
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement

class StatsByYearViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val INCOME_ID = 1
    }

    private val _app = application as SaveAppApplication

    private val _movements: LiveData<List<TaggedMovement>> =
        _app.movementRepository.allTaggedMovements.asLiveData()

    private val _expensesByYear: MutableMap<Int, Double> = mutableMapOf()
    private val _incomesByYear: MutableMap<Int, Double> = mutableMapOf()

    private val _yearStatsItems: MutableLiveData<List<YearStats>> = MutableLiveData(listOf())
    val yearStatsItems get(): LiveData<List<YearStats>> = _yearStatsItems

    init {
        _movements.observeForever(Observer { movements ->
            _expensesByYear.keys.forEach {
                _expensesByYear[it] = 0.0
                _incomesByYear[it] = 0.0
            }
            movements?.let {
                calculateSums(movements)
            }
            updateEntries()
        })
    }

    // Methods
    private fun calculateSums(movements: List<TaggedMovement>) {
        movements.forEach {
            val year = it.date.year
            val multipliers = if (it.tagId == INCOME_ID) listOf(0.0, 1.0) else listOf(1.0, 0.0)

            _expensesByYear[year] = (_expensesByYear[year] ?: 0.0) + (it.amount * multipliers[0])
            _incomesByYear[year] = (_incomesByYear[year] ?: 0.0) + (it.amount * multipliers[1])
        }
    }

    private fun updateEntries() {
        val items: MutableList<YearStats> = mutableListOf()

        _incomesByYear.keys.forEach {
            items.add(0, YearStats(it, _incomesByYear[it] ?: 0.0, _expensesByYear[it] ?: 0.0))
        }

        _yearStatsItems.value = items.toList()
    }
}
