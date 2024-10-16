// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import android.content.Context
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.SummaryStatistics
import com.ferrariofilippo.saveapp.model.entities.Transaction
import com.ferrariofilippo.saveapp.workers.statistics.StatisticsIntegrityCheckerWorker
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object StatsUtil {
    private const val CLASS_NAME = "StatsUtil"

    const val PERIODIC_INTEGRITY_CHECK_TAG = "periodic_statistics_integrity_check"
    const val ONE_TIME_INTEGRITY_CHECK_TAG = "one_time_statistics_integrity_check"

    private val _summary = SummaryStatistics()

    private val _mapsChangedFlipFlop = MutableLiveData(false)
    val mapsChangedFlipFlop get(): LiveData<Boolean> = _mapsChangedFlipFlop

    private val _monthExpenses = MutableLiveData(0.0)
    val monthExpenses get(): LiveData<Double> = _monthExpenses

    private val _monthIncomes = MutableLiveData(0.0)
    val monthIncomes get(): LiveData<Double> = _monthIncomes

    private val _yearExpenses = MutableLiveData(0.0)
    val yearExpenses get(): LiveData<Double> = _yearExpenses

    private val _yearIncomes = MutableLiveData(0.0)
    val yearIncomes get(): LiveData<Double> = _yearIncomes

    private val _lifeExpenses = MutableLiveData(0.0)
    val lifeExpenses get(): LiveData<Double> = _lifeExpenses

    private val _lifeIncomes = MutableLiveData(0.0)
    val lifeIncomes get(): LiveData<Double> = _lifeIncomes

    var monthTags: Map<Int, MutableLiveData<Double>> = mapOf()

    var yearTags: Map<Int, MutableLiveData<Double>> = mapOf()

    var lifeTags: Map<Int, MutableLiveData<Double>> = mapOf()

    fun init(application: SaveAppApplication) {
        _summary.loadData(application, ::updateMaps)
        importFromSummary()
    }

    fun applyRateToAll(context: Context, rate: Double) {
        _monthExpenses.value = _monthExpenses.value!! * rate
        _monthIncomes.value = _monthIncomes.value!! * rate
        _yearExpenses.value = _yearExpenses.value!! * rate
        _yearIncomes.value = _yearIncomes.value!! * rate
        _lifeExpenses.value = _lifeExpenses.value!! * rate
        _lifeIncomes.value = _lifeIncomes.value!! * rate

        monthTags.keys.forEach { monthTags[it]!!.value = monthTags[it]!!.value!! * rate }
        yearTags.keys.forEach { monthTags[it]!!.value = yearTags[it]!!.value!! * rate }
        lifeTags.keys.forEach { lifeTags[it]!!.value = lifeTags[it]!!.value!! * rate }

        saveChanges(context)
    }

    fun addTransactionToStat(context: Context, tr: Transaction) {
        val isSameYear = LocalDate.now().year == tr.date.year
        val isSameMonth = LocalDate.now().month == tr.date.month

        Handler(context.mainLooper).post {
            if (TagUtil.incomeTagIds.contains(tr.tagId)) {
                if (isSameYear) {
                    _yearIncomes.value = _yearIncomes.value!! + tr.amount

                    if (isSameMonth) {
                        _monthIncomes.value = _monthIncomes.value!! + tr.amount
                    }
                }

                _lifeIncomes.value = _lifeIncomes.value!! + tr.amount
            } else {
                val rootTagId = TagUtil.getTagRootId(tr.tagId)
                if (isSameYear) {
                    _yearExpenses.value = _yearExpenses.value!! + tr.amount
                    yearTags[rootTagId]!!.value = yearTags[rootTagId]!!.value!! + tr.amount

                    if (isSameMonth) {
                        _monthExpenses.value = _monthExpenses.value!! + tr.amount
                        monthTags[rootTagId]!!.value = monthTags[rootTagId]!!.value!! + tr.amount
                    }
                }

                _lifeExpenses.value = _lifeExpenses.value!! + tr.amount
                lifeTags[rootTagId]!!.value = lifeTags[rootTagId]!!.value!! + tr.amount
            }

            saveChanges(context)
        }
    }

    suspend fun startIntegrityCheckInterval(app: SaveAppApplication) {
        val wManager = WorkManager.getInstance(app)
        try {
            if (wManager.getWorkInfosByTagFlow(PERIODIC_INTEGRITY_CHECK_TAG).first() == null) {
                updateIntegrityCheckInterval(app)
            }
        } catch (_: NoSuchElementException) {
            updateIntegrityCheckInterval(app)
        }
    }

    suspend fun updateIntegrityCheckInterval(app: SaveAppApplication) {
        val wManager = WorkManager.getInstance(app)
        wManager.cancelAllWorkByTag(PERIODIC_INTEGRITY_CHECK_TAG).await()
        wManager.pruneWork().await()

        val constraints = Constraints
            .Builder()
            .setRequiresDeviceIdle(true)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<StatisticsIntegrityCheckerWorker>()
            .addTag(PERIODIC_INTEGRITY_CHECK_TAG)
            .setConstraints(constraints)
            .setInitialDelay(TimeUtil.getInitialDelay(3), TimeUnit.MINUTES)
            .build()

        wManager.enqueue(workRequest)

        LogUtil.logInfo(
            CLASS_NAME,
            ::updateIntegrityCheckInterval.name,
            "Enqueued statistics worker: ${workRequest.id}"
        )
    }

    suspend fun runIntegrityCheckNow(app: SaveAppApplication) {
        val wManager = WorkManager.getInstance(app)
        val workRequest = OneTimeWorkRequestBuilder<StatisticsIntegrityCheckerWorker>()
            .addTag(ONE_TIME_INTEGRITY_CHECK_TAG)
            .build()

        wManager.pruneWork().await()
        wManager.enqueue(workRequest)
    }

    private fun saveChanges(ctx: Context) {
        _summary.monthExpenses = _monthExpenses.value!!
        _summary.monthIncomes = _monthIncomes.value!!
        _summary.yearExpenses = _yearExpenses.value!!
        _summary.yearIncomes = _yearIncomes.value!!
        _summary.lifeExpenses = _lifeExpenses.value!!
        _summary.lifeIncomes = _lifeIncomes.value!!

        _summary.monthTags.clear()
        _summary.yearTags.clear()
        _summary.lifeTags.clear()
        monthTags.keys.forEach { _summary.monthTags[it] = monthTags[it]!!.value!! }
        yearTags.keys.forEach { _summary.yearTags[it] = yearTags[it]!!.value!! }
        lifeTags.keys.forEach { _summary.lifeTags[it] = lifeTags[it]!!.value!! }

        _summary.saveChanges(ctx)
    }

    private fun importFromSummary() {
        _monthExpenses.value = _summary.monthExpenses
        _monthIncomes.value = _summary.monthIncomes
        _yearExpenses.value = _summary.yearExpenses
        _yearIncomes.value = _summary.yearIncomes
        _lifeExpenses.value = _summary.lifeExpenses
        _lifeIncomes.value = _summary.lifeIncomes

        updateMaps()
    }

    private fun updateMaps() {
        val monthMap = mutableMapOf<Int, MutableLiveData<Double>>()
        val yearMap = mutableMapOf<Int, MutableLiveData<Double>>()
        val lifeMap = mutableMapOf<Int, MutableLiveData<Double>>()

        _summary.monthTags.keys.forEach { monthMap[it] = MutableLiveData(_summary.monthTags[it]) }
        _summary.yearTags.keys.forEach { yearMap[it] = MutableLiveData(_summary.yearTags[it]) }
        _summary.lifeTags.keys.forEach { lifeMap[it] = MutableLiveData(_summary.lifeTags[it]) }

        monthTags = monthMap
        yearTags = yearMap
        lifeTags = lifeMap

        _mapsChangedFlipFlop.value = !(_mapsChangedFlipFlop.value ?: false)
    }
}
