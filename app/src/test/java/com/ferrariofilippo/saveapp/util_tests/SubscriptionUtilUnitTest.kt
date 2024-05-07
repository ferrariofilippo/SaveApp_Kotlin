// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util_tests

import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import com.ferrariofilippo.saveapp.util.SubscriptionUtil
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class SubscriptionUtilUnitTest {
    @Test
    fun getTransactionFromSubscription_isCorrect() {
        var date = LocalDate.now()
        val renewal = RenewalType.WEEKLY
        val str = "Payment of {0} - {1}"
        val subscription = Subscription(
            1,
            9.99,
            "Test subscription",
            renewal,
            date,
            null,
            date,
            2,
            3
        )

        val transaction = SubscriptionUtil.getTransactionFromSub(subscription, str)
        val description =
            String.format(str, subscription.description, subscription.lastPaid.toString())

        assertNotNull(transaction)
        assertNotNull(subscription.lastPaid)
        assertEquals(transaction!!.id, 0)
        assertEquals(subscription.amount, transaction.amount, Double.MIN_VALUE)
        assertEquals(description, transaction.description)
        assertEquals(subscription.lastPaid, transaction.date)
        assertEquals(subscription.budgetId, transaction.budgetId)
        assertEquals(subscription.tagId, transaction.tagId)

        date = when (renewal) {
            RenewalType.WEEKLY -> date.plusDays(7)
            RenewalType.MONTHLY -> date.plusMonths(1)
            RenewalType.BIMONTHLY -> date.plusMonths(2)
            RenewalType.QUARTERLY -> date.plusMonths(3)
            RenewalType.SEMIANNUALLY -> date.plusMonths(6)
            else -> date.plusYears(1)
        }

        assertEquals(subscription.nextRenewal, date)
    }

    @Test
    fun getTransactionFromSubscription_null_isCorrect() {
        val str = "Test payment"
        val nextRenewal = LocalDate.now().plusDays(1)
        val subscription = Subscription(
            1,
            9.99,
            "Test subscription",
            RenewalType.WEEKLY,
            LocalDate.now(),
            null,
            nextRenewal,
            2,
            3
        )

        val transaction = SubscriptionUtil.getTransactionFromSub(subscription, str)
        assertNull(transaction)
        assertNull(subscription.lastPaid)
        assertEquals(subscription.nextRenewal, nextRenewal)
    }
}
