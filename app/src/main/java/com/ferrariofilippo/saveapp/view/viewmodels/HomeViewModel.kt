package com.ferrariofilippo.saveapp.view.viewmodels

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.data.repository.TagRepository
import com.ferrariofilippo.saveapp.model.entities.Tag
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.StatsUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class HomeViewModel(private val application: SaveAppApplication) : AndroidViewModel(application) {
    private val tagRepository: TagRepository = application.tagRepository

    private val months: Array<LiveData<Double>>
    private val years: Array<LiveData<Double>>
    private val life: Array<LiveData<Double>>

    val symbol: String =
        runBlocking { Currencies.from(SettingsUtil.getCurrency().first()) }.toSymbol()

    val currentMonth: LiveData<String> = MutableLiveData(getMonth())
    val monthSummary: MutableLiveData<Double> = MutableLiveData(0.0)
    val monthExpenses: LiveData<Double>
    val monthIncomes: LiveData<Double>
    val monthHighestTag: MutableLiveData<Tag?> = MutableLiveData(Tag(0, "", R.color.emerald_500))
    val monthHighestTagValue: MutableLiveData<Double> = MutableLiveData(0.0)

    val currentYear: LiveData<String> = MutableLiveData(LocalDate.now().year.toString())
    val yearSummary: MutableLiveData<Double> = MutableLiveData(0.0)
    val yearExpenses: LiveData<Double>
    val yearIncomes: LiveData<Double>
    val yearHighestTag: MutableLiveData<Tag?> = MutableLiveData(Tag(0, "", R.color.emerald_500))
    val yearHighestTagValue: MutableLiveData<Double> = MutableLiveData(0.0)

    val lifeNetWorth: MutableLiveData<Double> = MutableLiveData(0.0)
    val lifeExpenses: LiveData<Double>
    val lifeIncomes: LiveData<Double>
    val lifeHighestTag: MutableLiveData<Tag?> = MutableLiveData(Tag(0, "", R.color.emerald_500))
    val lifeHighestTagValue: MutableLiveData<Double> = MutableLiveData(0.0)

    init {
        months = StatsUtil.getMonthStats()
        years = StatsUtil.getYearStats()
        life = StatsUtil.getLifeStats()

        monthExpenses = months[0]
        monthIncomes = months[1]

        yearExpenses = years[0]
        yearIncomes = years[1]

        lifeExpenses = life[0]
        lifeIncomes = life[1]
    }

    suspend fun init(owner: LifecycleOwner) {
        monthExpenses.observe(owner, Observer {
            val income = months[1].value ?: 0.0
            monthSummary.value = income.minus(it)
        })
        monthIncomes.observe(owner, Observer {
            monthSummary.value = it.minus(months[0].value ?: 0.0)
        })
        for (i: Int in 2 until months.size) {
            months[i].observe(owner, Observer {
                if (it > monthHighestTagValue.value!!) {
                    monthHighestTagValue.value = it
                    monthHighestTag.value = runBlocking { tagRepository.getById(i - 1) }
                }
            })
        }

        years[0].observe(owner, Observer {
            val income = years[1].value ?: 0.0
            yearSummary.value = income.minus(it)
        })
        years[1].observe(owner, Observer {
            yearSummary.value = it.minus(years[0].value ?: 0.0)
        })
        for (i: Int in 2 until years.size) {
            years[i].observe(owner, Observer {
                if (it > yearHighestTagValue.value!!) {
                    yearHighestTagValue.value = it
                    yearHighestTag.value = runBlocking { tagRepository.getById(i - 1) }
                }
            })
        }

        life[0].observe(owner, Observer {
            val income = life[1].value ?: 0.0
            lifeNetWorth.value = income.minus(it)
        })
        life[1].observe(owner, Observer {
            lifeNetWorth.value = it.minus(life[0].value ?: 0.0)
        })
        for (i: Int in 2 until life.size) {
            life[i].observe(owner, Observer {
                if (it > lifeHighestTagValue.value!!) {
                    lifeHighestTagValue.value = it
                    lifeHighestTag.value = runBlocking { tagRepository.getById(i - 1) }
                }
            })
        }
    }

    private fun getMonth(): String {
        return when (LocalDate.now().monthValue) {
            1 -> application.getString(R.string.january)
            2 -> application.getString(R.string.february)
            3 -> application.getString(R.string.march)
            4 -> application.getString(R.string.april)
            5 -> application.getString(R.string.may)
            6 -> application.getString(R.string.june)
            7 -> application.getString(R.string.july)
            8 -> application.getString(R.string.august)
            9 -> application.getString(R.string.september)
            10 -> application.getString(R.string.october)
            11 -> application.getString(R.string.november)
            else -> application.getString(R.string.december)
        }
    }
}
