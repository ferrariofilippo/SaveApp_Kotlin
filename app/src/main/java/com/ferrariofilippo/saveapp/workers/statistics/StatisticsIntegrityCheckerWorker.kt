// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.workers.statistics

import android.app.PendingIntent.CanceledException
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.data.converters.DateConverter
import com.ferrariofilippo.saveapp.model.SummaryStatistics
import com.ferrariofilippo.saveapp.model.entities.Transaction
import com.ferrariofilippo.saveapp.model.enums.PeriodicInterval
import com.ferrariofilippo.saveapp.util.LogUtil
import com.ferrariofilippo.saveapp.util.SettingsUtil
import com.ferrariofilippo.saveapp.util.StatsUtil
import com.ferrariofilippo.saveapp.util.TagUtil
import com.ferrariofilippo.saveapp.util.TimeUtil
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class StatisticsIntegrityCheckerWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    private val CLASS_NAME = javaClass.kotlin.simpleName ?: "StatisticsIntegrityCheckerWorker"

    override suspend fun doWork(): Result {
        LogUtil.logInfo(CLASS_NAME, ::doWork.name, "${this.id} Beginning summary integrity check")
        val application = context.applicationContext as SaveAppApplication
        TagUtil.updateNow(application)
        if (TagUtil.tagsCount == 0 || TagUtil.incomeTagIds.size == 0) {
            LogUtil.logInfo(
                CLASS_NAME,
                ::doWork.name,
                "Summary integrity check failed - no tag found."
            )

            return Result.failure()
        }

        try {
            val stats = analyzeData(application)
            stats.saveChanges(context)
            Handler(Looper.getMainLooper()).post {
                StatsUtil.init(application)
            }
            LogUtil.logInfo(CLASS_NAME, ::doWork.name, "Summary integrity check ended")
        } catch (e: CanceledException) {
            LogUtil.logInfo(CLASS_NAME, ::doWork.name, "${this.id} canceled")
        }

        if (this.tags.contains(StatsUtil.PERIODIC_INTEGRITY_CHECK_TAG)) {
            scheduleNextCheck()
        }

        return Result.success()
    }

    private suspend fun analyzeData(app: SaveAppApplication): SummaryStatistics {
        val statistics = SummaryStatistics()
        val dateConverter = DateConverter()
        var transactions: List<Transaction>
        var rowsCount = 100
        var date = dateConverter.toString(LocalDate.MIN)

        TagUtil.updateAll(app)
        while (rowsCount == 100) {
            transactions = app.transactionRepository.getWindowAfterDate(date)

            transactions.forEach {
                statistics.addTransaction(it)
            }

            rowsCount = transactions.size
            if (rowsCount > 0) {
                date = dateConverter.toString(transactions[rowsCount - 1].date)
            }
        }

        return statistics
    }

    private suspend fun scheduleNextCheck() {
        SettingsUtil.setStore(context.applicationContext as SaveAppApplication)
        val interval = SettingsUtil.getSummaryIntegrityCheckInterval().first().toLong()
        val delay = TimeUtil.getInitialDelay(3, interval - PeriodicInterval.DAILY.minutes)
        val wManager = WorkManager.getInstance(context)
        val constraints = Constraints
            .Builder()
            .setRequiresDeviceIdle(true)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<StatisticsIntegrityCheckerWorker>()
            .addTag(StatsUtil.PERIODIC_INTEGRITY_CHECK_TAG)
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .build()

        wManager.pruneWork().await()
        wManager.enqueue(workRequest)

        LogUtil.logInfo(
            CLASS_NAME,
            ::scheduleNextCheck.name,
            "Next check scheduled in $delay minutes"
        )
    }
}
