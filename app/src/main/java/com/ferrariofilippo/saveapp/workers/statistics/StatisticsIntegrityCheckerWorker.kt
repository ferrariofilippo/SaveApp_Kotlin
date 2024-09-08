// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.workers.statistics

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.data.converters.DateConverter
import com.ferrariofilippo.saveapp.model.SummaryStatistics
import com.ferrariofilippo.saveapp.model.entities.Transaction
import com.ferrariofilippo.saveapp.util.LogUtil
import com.ferrariofilippo.saveapp.util.StatsUtil
import com.ferrariofilippo.saveapp.util.TagUtil
import java.time.LocalDate

class StatisticsIntegrityCheckerWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    private val CLASS_NAME = javaClass.kotlin.simpleName ?: "StatisticsIntegrityCheckerWorker"

    override suspend fun doWork(): Result {
        LogUtil.logInfo(CLASS_NAME, "doWork", "Beginning summary integrity check")
        val application = context.applicationContext as SaveAppApplication
        TagUtil.updateNow(application)
        if (TagUtil.tagsCount == 0 || TagUtil.incomeTagIds.size == 0) {
            return Result.failure()
        }

        val stats = analyzeData(application)
        stats.saveChanges(context)
        StatsUtil.init(application)
        LogUtil.logInfo(CLASS_NAME, "doWork", "Summary integrity check ended")

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
}
