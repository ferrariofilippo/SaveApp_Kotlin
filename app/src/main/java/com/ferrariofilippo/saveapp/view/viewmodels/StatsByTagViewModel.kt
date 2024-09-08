// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.statsitems.TagTransactionsSum
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedTransaction
import com.ferrariofilippo.saveapp.util.TagUtil
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate

class StatsByTagViewModel(application: Application) : AndroidViewModel(application) {
    private val _app = application as SaveAppApplication

    // Observers
    private val _tagsObserver = Observer<List<Tag>> { value -> manageTagChange(value) }

    private val _yearObserver = Observer<String> { value ->
        viewModelScope.launch {
            _transactions = _app.transactionRepository.getAllTaggedByYear(value)
            _showEmptyMessage.value = _transactions.isEmpty()

            _tagSums.keys.forEach {
                _tagSums[it] = 0.0
            }
            calculateSums(_isShowingExpenses.value!!)
        }
    }

    private val _showExpensesObserver = Observer<Boolean> { value ->
        _setLabel = if (value)
            application.getString(R.string.expenses_by_tag)
        else
            application.getString(R.string.incomes_by_tag)

        viewModelScope.launch {
            _transactions = _app.transactionRepository.getAllTaggedByYear(_year.value!!)
            _showEmptyMessage.value = _transactions.isEmpty()
            _tagSums.clear()
            _tags.value?.forEach {
                if ((value xor it.isIncome) && (it.parentTagId == 0 || !aggregateSubTags.value!!))
                    _tagSums[it.id] = 0.0
            }
            calculateSums(value)
        }
    }

    private val _aggregateObserver = Observer<Boolean> { refreshTags(_tags.value) }

    // Data
    private val _tags: LiveData<List<Tag>> = _app.tagRepository.allTags.asLiveData()

    private var _transactions: List<TaggedTransaction> = listOf()

    private val _tagSums: MutableMap<Int, Double> = mutableMapOf()

    private var _setLabel: String = application.getString(R.string.expenses_by_tag)

    private val _isShowingExpenses: MutableLiveData<Boolean> = MutableLiveData(true)

    private val _tagSumItems: MutableLiveData<List<TagTransactionsSum>> = MutableLiveData(listOf())
    val tagSumItems get(): LiveData<List<TagTransactionsSum>> = _tagSumItems

    private val _year: MutableLiveData<String> = MutableLiveData(LocalDate.now().year.toString())
    val year: LiveData<String> = _year

    private val _showEmptyMessage: MutableLiveData<Boolean> = MutableLiveData(false)
    val showEmptyMessage: LiveData<Boolean> = _showEmptyMessage

    val aggregateSubTags: MutableLiveData<Boolean> = MutableLiveData(true)

    var years: List<String> = listOf()

    var dataSet: PieDataSet = PieDataSet(listOf(), _setLabel)

    var onTransactionsChangeCallback: () -> Unit = { }

    init {
        initYears()

        _tags.observeForever(_tagsObserver)
        _year.observeForever(_yearObserver)
        _isShowingExpenses.observeForever(_showExpensesObserver)
        aggregateSubTags.observeForever(_aggregateObserver)
    }

    // Overrides
    override fun onCleared() {
        _tags.removeObserver(_tagsObserver)
        _year.removeObserver(_yearObserver)
        _isShowingExpenses.removeObserver(_showExpensesObserver)
        aggregateSubTags.removeObserver(_aggregateObserver)
        super.onCleared()
    }

    // Methods
    fun setYear(value: String) {
        _year.value = value
    }

    fun setType(isExpenses: Boolean) {
        _isShowingExpenses.value = isExpenses
    }

    private fun manageTagChange(tags: List<Tag>?) {
        if (tags == null) {
            return
        }

        tags.forEach { TagUtil.computeTagFullName(it) }
        refreshTags(tags)
    }

    private fun refreshTags(tags: List<Tag>?) {
        if (tags == null) {
            return
        }

        tags.let {
            _tagSums.clear()
            tags.forEach {
                if ((_isShowingExpenses.value!! xor it.isIncome) && (it.parentTagId == 0 || !aggregateSubTags.value!!)) {
                    _tagSums[it.id] = 0.0
                }
            }
        }
        calculateSums(_isShowingExpenses.value!!)
    }

    private fun calculateSums(selectExpenses: Boolean) {
        if (aggregateSubTags.value!!) {
            _transactions.forEach {
                if (selectExpenses xor TagUtil.incomeTagIds.contains(it.tagId)) {
                    val id = TagUtil.getTagRootId(it.tagId)
                    _tagSums[id] = (_tagSums[id] ?: 0.0) + it.amount
                }
            }
        } else {
            _transactions.forEach {
                if (selectExpenses xor TagUtil.incomeTagIds.contains(it.tagId)) {
                    _tagSums[it.tagId] = (_tagSums[it.tagId] ?: 0.0) + it.amount
                }
            }
        }

        updateEntries(selectExpenses)
    }

    private fun updateEntries(selectExpenses: Boolean) {
        var generalSum = 0.0
        _tagSums.values.forEach { generalSum += it }

        val entries: MutableList<PieEntry> = mutableListOf()
        val items: MutableList<TagTransactionsSum> = mutableListOf()
        val colors: MutableList<Int> = mutableListOf()
        _tags.value?.forEach {
            val sum = _tagSums[it.id] ?: 0.0
            val percentage = if (generalSum != 0.0) sum * 100.0 / generalSum else 0.0
            if ((selectExpenses xor it.isIncome) && (it.parentTagId == 0 || !aggregateSubTags.value!!)) {
                items.add(TagTransactionsSum(it.id, it.fullName, it.color, sum, percentage))
            }

            if (sum != 0.0) {
                colors.add(it.color)
                entries.add(PieEntry(percentage.toFloat(), it.name))
            }
        }

        _tagSumItems.value = items.sortedBy { it.name }

        dataSet = PieDataSet(entries, _setLabel)
        dataSet.colors = colors
        dataSet.valueFormatter = PercentFormatter()

        onTransactionsChangeCallback()
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
