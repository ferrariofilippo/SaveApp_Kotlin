// Copyright (c) 2024 Filippo Ferrario
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
import com.ferrariofilippo.saveapp.data.repository.TagRepository
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.StatsUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val saveAppApplication = application as SaveAppApplication

    // Observers
    private val _mapsChangedObserver = Observer<Boolean> {
        clearTagsObservers()
        months = StatsUtil.monthTags
        years = StatsUtil.yearTags
        life = StatsUtil.lifeTags
        observeTags()
    }

    private val _monthIncomesObserver =
        Observer<Double> { _monthSummary.value = it - monthExpenses.value!! }

    private val _monthExpensesObserver =
        Observer<Double> { _monthSummary.value = monthIncomes.value!! - it }

    private val _yearIncomesObserver =
        Observer<Double> { _yearSummary.value = it - yearExpenses.value!! }

    private val _yearExpensesObserver =
        Observer<Double> { _yearSummary.value = yearIncomes.value!! - it }

    private val _lifeIncomesObserver =
        Observer<Double> { _lifeNetWorth.value = it - lifeExpenses.value!! }

    private val _lifeExpensesObserver =
        Observer<Double> { _lifeNetWorth.value = lifeIncomes.value!! - it }

    private val _monthObservers = mutableMapOf<Int, Observer<Double>>()
    private val _yearObservers = mutableMapOf<Int, Observer<Double>>()
    private val _lifeObservers = mutableMapOf<Int, Observer<Double>>()

    // Data & UI
    private val tagRepository: TagRepository = saveAppApplication.tagRepository

    private var months: Map<Int, MutableLiveData<Double>> = StatsUtil.monthTags
    private var years: Map<Int, MutableLiveData<Double>> = StatsUtil.yearTags
    private var life: Map<Int, MutableLiveData<Double>> = StatsUtil.lifeTags

    private val defaultTag: Tag = Tag(0, "", R.color.emerald_500, false)

    private val _symbol = MutableLiveData(Currencies.EUR.toSymbol())
    val symbol: LiveData<String> = _symbol

    val zero = 0.0

    // Month data
    val currentMonth: LiveData<String> = MutableLiveData(setMonth())

    private val _monthSummary = MutableLiveData(0.0)
    val monthSummary: LiveData<Double> = _monthSummary

    val monthExpenses: LiveData<Double> = StatsUtil.monthExpenses

    val monthIncomes: LiveData<Double> = StatsUtil.monthIncomes

    private val _monthHighestTag = MutableLiveData(defaultTag)
    val monthHighestTag: LiveData<Tag?> = _monthHighestTag

    private val _monthHighestTagValue = MutableLiveData(-1.0)
    val monthHighestTagValue: LiveData<Double> = _monthHighestTagValue

    // Year data
    val currentYear: LiveData<String> = MutableLiveData(LocalDate.now().year.toString())

    private val _yearSummary = MutableLiveData(0.0)
    val yearSummary: MutableLiveData<Double> = _yearSummary

    val yearExpenses: LiveData<Double> = StatsUtil.yearExpenses

    val yearIncomes: LiveData<Double> = StatsUtil.yearIncomes

    private val _yearHighestTag = MutableLiveData(defaultTag)
    val yearHighestTag: LiveData<Tag?> = _yearHighestTag

    private val _yearHighestTagValue = MutableLiveData(-1.0)
    val yearHighestTagValue: LiveData<Double> = _yearHighestTagValue

    // Life data
    private val _lifeNetWorth = MutableLiveData(0.0)
    val lifeNetWorth: LiveData<Double> = _lifeNetWorth

    val lifeExpenses: LiveData<Double> = StatsUtil.lifeExpenses

    val lifeIncomes: LiveData<Double> = StatsUtil.lifeIncomes

    private val _lifeHighestTag = MutableLiveData(defaultTag)
    val lifeHighestTag: LiveData<Tag?> = _lifeHighestTag

    private val _lifeHighestTagValue = MutableLiveData(-1.0)
    val lifeHighestTagValue: LiveData<Double> = _lifeHighestTagValue

    init {
        viewModelScope.launch {
            SettingsUtil.getCurrency().collect {
                _symbol.value = Currencies.from(it).toSymbol()
            }
        }

        setupMonth()
        setupYear()
        setupLife()

        StatsUtil.mapsChangedFlipFlop.observeForever(_mapsChangedObserver)
        observeTags()
    }

    // Overrides
    override fun onCleared() {
        monthExpenses.removeObserver(_monthExpensesObserver)
        monthIncomes.removeObserver(_monthIncomesObserver)
        yearExpenses.removeObserver(_yearExpensesObserver)
        yearIncomes.removeObserver(_yearIncomesObserver)
        lifeExpenses.removeObserver(_lifeExpensesObserver)
        lifeIncomes.removeObserver(_lifeIncomesObserver)
        StatsUtil.mapsChangedFlipFlop.removeObserver(_mapsChangedObserver)
        clearTagsObservers()
        super.onCleared()
    }

    // Methods
    private fun setMonth(): String {
        return when (LocalDate.now().monthValue) {
            1 -> saveAppApplication.getString(R.string.january)
            2 -> saveAppApplication.getString(R.string.february)
            3 -> saveAppApplication.getString(R.string.march)
            4 -> saveAppApplication.getString(R.string.april)
            5 -> saveAppApplication.getString(R.string.may)
            6 -> saveAppApplication.getString(R.string.june)
            7 -> saveAppApplication.getString(R.string.july)
            8 -> saveAppApplication.getString(R.string.august)
            9 -> saveAppApplication.getString(R.string.september)
            10 -> saveAppApplication.getString(R.string.october)
            11 -> saveAppApplication.getString(R.string.november)
            else -> saveAppApplication.getString(R.string.december)
        }
    }

    private fun setupMonth() {
        monthExpenses.observeForever(_monthExpensesObserver)
        monthIncomes.observeForever(_monthIncomesObserver)

        runBlocking {
            for (i: Int in months.keys) {
                val value = months[i]!!.value!!
                if (value > _monthHighestTagValue.value!!) {
                    _monthHighestTag.value = tagRepository.getById(i)
                    _monthHighestTagValue.value = value
                }
            }
        }
    }

    private fun setupYear() {
        yearExpenses.observeForever(_yearExpensesObserver)
        yearIncomes.observeForever(_yearIncomesObserver)

        runBlocking {
            for (i: Int in years.keys) {
                val value = years[i]!!.value!!
                if (value > _yearHighestTagValue.value!!) {
                    _yearHighestTag.value = tagRepository.getById(i)
                    _yearHighestTagValue.value = value
                }
            }
        }
    }

    private fun setupLife() {
        lifeExpenses.observeForever(_lifeExpensesObserver)
        lifeIncomes.observeForever(_lifeIncomesObserver)

        runBlocking {
            for (i: Int in life.keys) {
                val value = life[i]!!.value!!
                if (value > _lifeHighestTagValue.value!!) {
                    _lifeHighestTag.value = tagRepository.getById(i)
                    _lifeHighestTagValue.value = value
                }
            }
        }
    }

    private fun clearTagsObservers() {
        for (i in _monthObservers.keys) {
            months[i]!!.removeObserver(_monthObservers[i]!!)
        }
        for (i in _yearObservers.keys) {
            years[i]!!.removeObserver(_yearObservers[i]!!)
        }
        for (i in _lifeObservers.keys) {
            life[i]!!.removeObserver(_lifeObservers[i]!!)
        }
        _monthObservers.clear()
        _yearObservers.clear()
        _lifeObservers.clear()
    }

    private fun observeTags() {
        for (i: Int in months.keys) {
            _monthObservers[i] = Observer {
                if (it > _monthHighestTagValue.value!!) {
                    _monthHighestTag.value = runBlocking { tagRepository.getById(i) }
                    _monthHighestTagValue.value = it
                }
            }
            months[i]!!.observeForever(_monthObservers[i]!!)
        }
        for (i: Int in years.keys) {
            _yearObservers[i] = Observer {
                if (it > _yearHighestTagValue.value!!) {
                    _yearHighestTag.value = runBlocking { tagRepository.getById(i) }
                    _yearHighestTagValue.value = it
                }
            }
            years[i]!!.observeForever(_yearObservers[i]!!)
        }
        for (i: Int in life.keys) {
            _lifeObservers[i] = Observer {
                if (it > lifeHighestTagValue.value!!) {
                    _lifeHighestTag.value = runBlocking { tagRepository.getById(i) }
                    _lifeHighestTagValue.value = it
                }
            }
            life[i]!!.observeForever(_lifeObservers[i]!!)
        }
    }
}
