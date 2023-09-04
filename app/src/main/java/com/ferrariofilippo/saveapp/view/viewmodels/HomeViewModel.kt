package com.ferrariofilippo.saveapp.view.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.data.repository.TagRepository
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.StatsUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        // IDs start from 1. The first tag (ID = 1) is Income and it should be skipped
        private const val FIRST_TAG_ID = 2
    }

    private val saveAppApplication = application as SaveAppApplication

    private val tagRepository: TagRepository = saveAppApplication.tagRepository

    private val months: Array<Flow<Double>> = StatsUtil.getTagsStats(StatsUtil.monthKeys)
    private val years: Array<Flow<Double>> = StatsUtil.getTagsStats(StatsUtil.yearKeys)
    private val life: Array<Flow<Double>> = StatsUtil.getTagsStats(StatsUtil.lifeKeys)

    private val defaultTag: Tag = Tag(0, "", R.color.emerald_500)

    private val _symbol = MutableLiveData(Currencies.EUR.toSymbol())
    val symbol: LiveData<String> = _symbol

    val zero = 0.0

    // Month data
    val currentMonth: LiveData<String> = MutableLiveData(setMonth())

    private val monthExpensesFlow: Flow<Double> = StatsUtil.getStat(StatsUtil.monthExpensesKey)
    private val monthIncomesFlow: Flow<Double> = StatsUtil.getStat(StatsUtil.monthIncomesKey)

    private val _monthSummary = MutableLiveData(0.0)
    val monthSummary: LiveData<Double> = _monthSummary

    private val _monthExpenses = MutableLiveData(0.0)
    val monthExpenses: LiveData<Double> = _monthExpenses

    private val _monthIncomes = MutableLiveData(0.0)
    val monthIncomes: LiveData<Double> = _monthIncomes

    private val _monthHighestTag = MutableLiveData(defaultTag)
    val monthHighestTag: LiveData<Tag?> = _monthHighestTag

    private val _monthHighestTagValue = MutableLiveData(0.0)
    val monthHighestTagValue: LiveData<Double> = _monthHighestTagValue

    // Year data
    val currentYear: LiveData<String> = MutableLiveData(LocalDate.now().year.toString())

    private val yearExpensesFlow: Flow<Double> = StatsUtil.getStat(StatsUtil.yearExpensesKey)
    private val yearIncomesFlow: Flow<Double> = StatsUtil.getStat(StatsUtil.yearIncomesKey)

    private val _yearSummary = MutableLiveData(0.0)
    val yearSummary: MutableLiveData<Double> = _yearSummary

    private val _yearExpenses = MutableLiveData(0.0)
    val yearExpenses: LiveData<Double> = _yearExpenses

    private val _yearIncomes = MutableLiveData(0.0)
    val yearIncomes: LiveData<Double> = _yearIncomes

    private val _yearHighestTag = MutableLiveData(defaultTag)
    val yearHighestTag: LiveData<Tag?> = _yearHighestTag

    private val _yearHighestTagValue = MutableLiveData(0.0)
    val yearHighestTagValue: LiveData<Double> = _yearHighestTagValue

    // Life data
    private val lifeExpensesFlow: Flow<Double> = StatsUtil.getStat(StatsUtil.lifeExpensesKey)
    private val lifeIncomesFlow: Flow<Double> = StatsUtil.getStat(StatsUtil.lifeIncomesKey)

    private val _lifeNetWorth = MutableLiveData(0.0)
    val lifeNetWorth: LiveData<Double> = _lifeNetWorth

    private val _lifeExpenses = MutableLiveData(0.0)
    val lifeExpenses: LiveData<Double> = _lifeExpenses

    private val _lifeIncomes = MutableLiveData(0.0)
    val lifeIncomes: LiveData<Double> = _lifeIncomes

    private val _lifeHighestTag = MutableLiveData(defaultTag)
    val lifeHighestTag: LiveData<Tag?> = _lifeHighestTag

    private val _lifeHighestTagValue = MutableLiveData(0.0)
    val lifeHighestTagValue: LiveData<Double> = _lifeHighestTagValue

    init {
        viewModelScope.launch {
            _symbol.value = Currencies.from(SettingsUtil.getCurrency().first()).toSymbol()
        }

        viewModelScope.launch {
            monthExpensesFlow.collect {
                _monthExpenses.value = it
                _monthSummary.value = _monthIncomes.value!!.minus(it)
            }
        }
        viewModelScope.launch {
            monthIncomesFlow.collect {
                _monthIncomes.value = it
                _monthSummary.value = it.minus(_monthExpenses.value!!)
            }
        }

        for (i: Int in life.indices) {
            viewModelScope.launch {
                months[i].collect {
                    if (it > _monthHighestTagValue.value!!) {
                        _monthHighestTagValue.value = it
                        _monthHighestTag.value = tagRepository.getById(i + FIRST_TAG_ID)
                    }
                }
            }
        }

        viewModelScope.launch {
            yearExpensesFlow.collect {
                _yearExpenses.value = it
                _yearSummary.value = _yearIncomes.value!!.minus(it)
            }
        }
        viewModelScope.launch {
            yearIncomesFlow.collect {
                _yearIncomes.value = it
                _yearSummary.value = it.minus(_yearExpenses.value!!)
            }
        }
        for (i: Int in life.indices) {
            viewModelScope.launch {
                years[i].collect {
                    if (it > _yearHighestTagValue.value!!) {
                        _yearHighestTagValue.value = it
                        _yearHighestTag.value = tagRepository.getById(i + FIRST_TAG_ID)
                    }
                }
            }
        }

        viewModelScope.launch {
            lifeExpensesFlow.collect {
                _lifeExpenses.value = it
                _lifeNetWorth.value = _lifeIncomes.value!!.minus(it)
            }
        }
        viewModelScope.launch {
            lifeIncomesFlow.collect {
                _lifeIncomes.value = it
                _lifeNetWorth.value = it.minus(_lifeExpenses.value!!)
            }
        }
        for (i: Int in life.indices) {
            viewModelScope.launch {
                life[i].collect {
                    if (it > lifeHighestTagValue.value!!) {
                        _lifeHighestTagValue.value = it
                        _lifeHighestTag.value = tagRepository.getById(i + FIRST_TAG_ID)
                    }
                }
            }
        }
    }

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
}
