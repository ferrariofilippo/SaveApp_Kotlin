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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val saveAppApplication = application as SaveAppApplication

    private val tagRepository: TagRepository = saveAppApplication.tagRepository

    private val months: Map<Int, MutableLiveData<Double>> = StatsUtil.monthTags
    private val years: Map<Int, MutableLiveData<Double>> = StatsUtil.yearTags
    private val life: Map<Int, MutableLiveData<Double>> = StatsUtil.lifeTags

    private val defaultTag: Tag = Tag(0, "", R.color.emerald_500)

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
            _symbol.value = Currencies.from(SettingsUtil.getCurrency().first()).toSymbol()
        }

        setupMonth()
        setupYear()
        setupLife()

        observeTags()
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

    private fun setupMonth() {
        monthExpenses.observeForever {
            _monthSummary.value = monthIncomes.value!! - it
        }
        monthIncomes.observeForever {
            _monthSummary.value = it - monthExpenses.value!!
        }

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
        yearExpenses.observeForever {
            _yearSummary.value = yearIncomes.value!! - it
        }
        yearIncomes.observeForever {
            _yearSummary.value = it - yearExpenses.value!!
        }

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
        lifeExpenses.observeForever {
            _lifeNetWorth.value = lifeIncomes.value!! - it
        }
        lifeIncomes.observeForever {
            _lifeNetWorth.value = it - lifeExpenses.value!!
        }

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

    private fun observeTags() {
        for (i: Int in months.keys) {
            months[i]!!.observeForever {
                if (it > _monthHighestTagValue.value!!) {
                    _monthHighestTag.value = runBlocking { tagRepository.getById(i) }
                    _monthHighestTagValue.value = it
                }
            }
        }
        for (i: Int in years.keys) {
            years[i]!!.observeForever {
                if (it > _yearHighestTagValue.value!!) {
                    _yearHighestTag.value = runBlocking { tagRepository.getById(i) }
                    _yearHighestTagValue.value = it
                }
            }
        }
        for (i: Int in life.keys) {
            life[i]!!.observeForever {
                if (it > lifeHighestTagValue.value!!) {
                    _lifeHighestTag.value = runBlocking { tagRepository.getById(i) }
                    _lifeHighestTagValue.value = it
                }
            }
        }
    }
}
