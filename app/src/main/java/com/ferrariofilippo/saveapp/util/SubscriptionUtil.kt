// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Transaction
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import java.time.LocalDate

object SubscriptionUtil {
    private fun updateNextRenewal(s: Subscription) {
        s.nextRenewal = when (s.renewalType) {
            RenewalType.WEEKLY -> s.nextRenewal.plusDays(7)
            RenewalType.MONTHLY -> s.nextRenewal.plusMonths(1)
            RenewalType.BIMONTHLY -> s.nextRenewal.plusMonths(2)
            RenewalType.QUARTERLY -> s.nextRenewal.plusMonths(3)
            RenewalType.SEMIANNUALLY -> s.nextRenewal.plusMonths(6)
            else -> s.nextRenewal.plusYears(1)
        }
    }

    fun getTransactionFromSub(s: Subscription, description: String): Transaction? {
        if (s.nextRenewal.isAfter(LocalDate.now())) {
            return null
        }

        s.lastPaid = s.nextRenewal
        updateNextRenewal(s)
        return Transaction(
            0,
            s.amount,
            String.format(description, s.description, s.lastPaid.toString()),
            s.lastPaid!!,
            s.tagId,
            s.budgetId
        )
    }

    suspend fun validateSubscriptions(application: SaveAppApplication) {
        val subscriptions = application.subscriptionRepository.getAll()
        val description = application.getString(R.string.payment_of)

        for (subscription in subscriptions) {
            var transaction = getTransactionFromSub(subscription, description)
            while (transaction != null) {
                BudgetUtil.tryAddTransactionToBudget(transaction)
                application.transactionRepository.insert(transaction)
                StatsUtil.addTransactionToStat(application, transaction)
                transaction = getTransactionFromSub(subscription, description)
            }
            application.subscriptionRepository.update(subscription)
        }
    }
}
