// Copyright (c) 2024 Filippo Ferrario
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
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedTransaction
import com.ferrariofilippo.saveapp.util.TagUtil

class StatsByYearViewModel(application: Application) : AndroidViewModel(application) {
    private val _app = application as SaveAppApplication

    private val _transactionsObserver = Observer<List<TaggedTransaction>> { value ->
        _expensesByYear.keys.forEach {
            _expensesByYear[it] = 0.0
            _incomesByYear[it] = 0.0
        }
        calculateSums(value)
        updateEntries()
    }

    private val _transactions: LiveData<List<TaggedTransaction>> =
        _app.transactionRepository.allTaggedTransactions.asLiveData()

    private val _expensesByYear: MutableMap<Int, Double> = mutableMapOf()
    private val _incomesByYear: MutableMap<Int, Double> = mutableMapOf()

    private val _yearStatsItems: MutableLiveData<List<YearStats>> = MutableLiveData(listOf())
    val yearStatsItems get(): LiveData<List<YearStats>> = _yearStatsItems

    init {
        _transactions.observeForever(_transactionsObserver)
    }

    // Overrides
    override fun onCleared() {
        _transactions.removeObserver(_transactionsObserver)
        super.onCleared()
    }

    // Methods
    private fun calculateSums(transactions: List<TaggedTransaction>) {
        transactions.forEach {
            val year = it.date.year
            val multipliers =
                if (TagUtil.incomeTagIds.contains(it.tagId)) listOf(0.0, 1.0) else listOf(1.0, 0.0)

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
