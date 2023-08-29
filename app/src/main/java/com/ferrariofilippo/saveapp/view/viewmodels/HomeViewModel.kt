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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val saveAppApplication = application as SaveAppApplication

    private val tagRepository: TagRepository = saveAppApplication.tagRepository

    private val months: Array<LiveData<Double>> = StatsUtil.getTagsStats(StatsUtil.monthKeys)
    private val years: Array<LiveData<Double>> = StatsUtil.getTagsStats(StatsUtil.yearKeys)
    private val life: Array<LiveData<Double>> = StatsUtil.getTagsStats(StatsUtil.lifeKeys)

    private val defaultTag: Tag = Tag(0, "", R.color.emerald_500)

    private val _symbol = MutableLiveData(Currencies.EUR.toSymbol())
    val symbol: LiveData<String> = _symbol

    val zero = 0.0

    // Month data
    val currentMonth: LiveData<String> = MutableLiveData(setMonth())

    private val _monthSummary = MutableLiveData(0.0)
    val monthSummary: LiveData<Double> = _monthSummary

    val monthExpenses: LiveData<Double> = StatsUtil.getStat(StatsUtil.monthExpensesKey)
    val monthIncomes: LiveData<Double> = StatsUtil.getStat(StatsUtil.monthIncomesKey)

    private val _monthHighestTag = MutableLiveData(defaultTag)
    val monthHighestTag: LiveData<Tag?> = _monthHighestTag

    private val _monthHighestTagValue = MutableLiveData(0.0)
    val monthHighestTagValue: LiveData<Double> = _monthHighestTagValue

    // Year data
    val currentYear: LiveData<String> = MutableLiveData(LocalDate.now().year.toString())

    private val _yearSummary = MutableLiveData(0.0)
    val yearSummary: MutableLiveData<Double> = _yearSummary

    val yearExpenses: LiveData<Double> = StatsUtil.getStat(StatsUtil.yearExpensesKey)
    val yearIncomes: LiveData<Double> = StatsUtil.getStat(StatsUtil.yearIncomesKey)

    private val _yearHighestTag = MutableLiveData(defaultTag)
    val yearHighestTag: MutableLiveData<Tag?> = _yearHighestTag

    private val _yearHighestTagValue = MutableLiveData(0.0)
    val yearHighestTagValue: MutableLiveData<Double> = _yearHighestTagValue

    // Life data
    private val _lifeNetWorth = MutableLiveData(0.0)
    val lifeNetWorth: MutableLiveData<Double> = _lifeNetWorth

    val lifeExpenses: LiveData<Double> = StatsUtil.getStat(StatsUtil.lifeExpensesKey)
    val lifeIncomes: LiveData<Double> = StatsUtil.getStat(StatsUtil.lifeIncomesKey)

    private val _lifeHighestTag = MutableLiveData(defaultTag)
    val lifeHighestTag: MutableLiveData<Tag?> = _lifeHighestTag

    private val _lifeHighestTagValue = MutableLiveData(0.0)
    val lifeHighestTagValue: MutableLiveData<Double> = _lifeHighestTagValue

    init {
        viewModelScope.launch {
            _symbol.value = Currencies.from(SettingsUtil.getCurrency().first()).toSymbol()
        }

        monthExpenses.observeForever(Observer {
            val income = monthIncomes.value ?: 0.0
            _monthSummary.value = income.minus(it)
        })
        monthIncomes.observeForever(Observer {
            _monthSummary.value = it.minus(monthExpenses.value ?: 0.0)
        })
        for (i: Int in 2 until months.size) {
            months[i].observeForever(Observer {
                if (it > _monthHighestTagValue.value!!) {
                    _monthHighestTagValue.value = it
                    _monthHighestTag.value = runBlocking { tagRepository.getById(i - 1) }
                }
            })
        }

        yearExpenses.observeForever(Observer {
            val income = yearIncomes.value ?: 0.0
            _yearSummary.value = income.minus(it)
        })
        yearIncomes.observeForever(Observer {
            _yearSummary.value = it.minus(yearExpenses.value ?: 0.0)
        })
        for (i: Int in 2 until years.size) {
            years[i].observeForever(Observer {
                if (it > _yearHighestTagValue.value!!) {
                    _yearHighestTagValue.value = it
                    _yearHighestTag.value = runBlocking { tagRepository.getById(i - 1) }
                }
            })
        }

        lifeExpenses.observeForever(Observer {
            val income = lifeIncomes.value ?: 0.0
            _lifeNetWorth.value = income.minus(it)
        })
        lifeIncomes.observeForever(Observer {
            lifeNetWorth.value = it.minus(lifeExpenses.value ?: 0.0)
        })
        for (i: Int in 2 until life.size) {
            life[i].observeForever(Observer {
                if (it > lifeHighestTagValue.value!!) {
                    _lifeHighestTagValue.value = it
                    _lifeHighestTag.value = runBlocking { tagRepository.getById(i - 1) }
                }
            })
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
