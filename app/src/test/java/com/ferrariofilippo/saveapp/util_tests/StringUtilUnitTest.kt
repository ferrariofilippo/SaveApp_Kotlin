// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util_tests

import com.ferrariofilippo.saveapp.model.entities.Budget
import com.ferrariofilippo.saveapp.model.entities.Transaction
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import com.ferrariofilippo.saveapp.util.StringUtil.toBudgetOrNull
import com.ferrariofilippo.saveapp.util.StringUtil.toCurrencyString
import com.ferrariofilippo.saveapp.util.StringUtil.toSubscriptionOrNull
import com.ferrariofilippo.saveapp.util.StringUtil.toTransactionOrNull
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class StringUtilUnitTest {
    @Test
    fun toTransactionOrNull_isCorrect() {
        val date = LocalDate.now()
        val t = Transaction(1, 12.9, "Test transaction", date, 2, 3)
        val transactionStr = "${t.id},${t.amount},${t.description},${date},${t.tagId},${t.budgetId}"
        val parsedTransaction = transactionStr.toTransactionOrNull()

        assertNotNull(parsedTransaction)
        assertEquals(parsedTransaction!!.id, t.id)
        assertEquals(parsedTransaction.amount, t.amount, Double.MIN_VALUE)
        assertEquals(parsedTransaction.description, t.description)
        assertEquals(parsedTransaction.date, t.date)
        assertEquals(parsedTransaction.tagId, t.tagId)
        assertEquals(parsedTransaction.budgetId, t.budgetId)

        // Default values
        assertNotNull("1,${10.0},description,${date},NOT_AN_INT,3".toTransactionOrNull())
        assertNotNull("1,${10.0},description,${date},2,NOT_AN_INT".toTransactionOrNull())
    }

    @Test
    fun toTransactionOrNull_null_isCorrect() {
        val d = LocalDate.now()

        assertNull("".toTransactionOrNull())
        assertNull(",,,,,".toTransactionOrNull())
        assertNull("NOT_AN_INT,${10.0},description,${d},2,3".toTransactionOrNull())
        assertNull("${1.1},${10.0},description,${d},2,3".toTransactionOrNull())
        assertNull("1,NOT_A_DOUBLE,description,${d},2,3".toTransactionOrNull())
        assertNull("1,${10.0},description,NOT_A_DATE,2,3".toTransactionOrNull())
    }

    @Test
    fun toSubscriptionOrNull_isCorrect() {
        val date = LocalDate.now()
        val type = RenewalType.WEEKLY
        val s =
            Subscription(1, 9.99, "Test subscription", type, date, date, date, 2, 3)
        val subscriptionStr =
            "${s.id},${s.amount},${s.description},${s.renewalType},${s.creationDate},${s.lastPaid},${s.nextRenewal},${s.tagId},${s.budgetId}"
        val parsedSubscription = subscriptionStr.toSubscriptionOrNull()

        assertNotNull(parsedSubscription)
        assertEquals(parsedSubscription!!.id, s.id)
        assertEquals(parsedSubscription.amount, s.amount, Double.MIN_VALUE)
        assertEquals(parsedSubscription.description, s.description)
        assertEquals(parsedSubscription.creationDate, s.creationDate)
        assertEquals(parsedSubscription.lastPaid, s.lastPaid)
        assertEquals(parsedSubscription.nextRenewal, s.nextRenewal)
        assertEquals(parsedSubscription.tagId, s.tagId)
        assertEquals(parsedSubscription.budgetId, s.budgetId)

        // Default values
        assertNotNull("1,${10.0},description,NOT_RENEWAL,${date},${date},${date},2,3".toSubscriptionOrNull())
        assertNotNull("1,${10.0},description,${type},${date},${date},${date},NOT_AN_INT,3".toSubscriptionOrNull())
        assertNotNull("1,${10.0},description,${type},${date},${date},${date},2,NOT_AN_INT".toSubscriptionOrNull())
    }

    @Test
    fun toSubscriptionOrNull_null_isCorrect() {
        val d = LocalDate.now()
        val type = RenewalType.WEEKLY

        assertNull("".toSubscriptionOrNull())
        assertNull(",,,,,,,,".toSubscriptionOrNull())
        assertNull("NOT_AN_INT,${10.0},description,${type},${d},${d},${d},2,3".toSubscriptionOrNull())
        assertNull("${1.1},${10.0},description,${type},${d},${d},${d},2,3".toSubscriptionOrNull())
        assertNull("1,NOT_A_DOUBLE,description,${type},${d},${d},${d},2,3".toSubscriptionOrNull())
        assertNull("1,${10.0},description,${type},NOT_A_DATE,${d},${d},2,3".toSubscriptionOrNull())
        assertNull("1,${10.0},description,${type},${d},NOT_A_DATE,${d},2,3".toSubscriptionOrNull())
        assertNull("1,${10.0},description,${type},${d},${d},NOT_A_DATE,2,3".toSubscriptionOrNull())
    }

    @Test
    fun toBudgetOrNull_isCorrect() {
        val date = LocalDate.now()
        val b = Budget(1, 11.11, 4.67, "Budget_Test", date, date, 2)
        val budgetStr = "${b.id},${b.max},${b.used},${b.name},${b.from},${b.to},${b.tagId}"
        val parsedBudget = budgetStr.toBudgetOrNull()

        assertNotNull(parsedBudget)
        assertEquals(parsedBudget!!.id, b.id)
        assertEquals(parsedBudget.max, b.max, Double.MIN_VALUE)
        assertEquals(parsedBudget.used, b.used, Double.MIN_VALUE)
        assertEquals(parsedBudget.name, b.name)
        assertEquals(parsedBudget.from, b.from)
        assertEquals(parsedBudget.to, b.to)
        assertEquals(parsedBudget.tagId, b.tagId)

        // Default values
        assertNotNull("1,${10.0},${10.0},description,${date},${date},NOT_AN_INT".toBudgetOrNull())
    }

    @Test
    fun toBudgetOrNull_null_isCorrect() {
        val date = LocalDate.now()

        assertNull("".toBudgetOrNull())
        assertNull(",,,,,,".toBudgetOrNull())
        assertNull("NOT_AN_INT,${10.0},${10.0},description,${date},${date},0".toBudgetOrNull())
        assertNull("${9.99},${10.0},${10.0},description,${date},${date},0".toBudgetOrNull())
        assertNull("1,NOT_A_DOUBLE,${10.0},description,${date},${date},0".toBudgetOrNull())
        assertNull("1,${10.0},NOT_A_DOUBLE,description,${date},${date},0".toBudgetOrNull())
        assertNull("1,${10.0},NOT_A_DOUBLE,description,NOT_A_DATE,${date},0".toBudgetOrNull())
        assertNull("1,${10.0},${10.0},description,${date},NOT_A_DATE,0".toBudgetOrNull())
    }

    @Test
    fun toCurrencyString_isCorrect() {
        val ticker = "EUR"
        val lowAmount = 111.111
        val meanAmount = 1_111_111.11
        val highAmount = 11_111_111_111.111

        assertEquals("$ticker 111.11", toCurrencyString(lowAmount, ticker).replace(',', '.'))
        assertEquals("$ticker 1111.11k", toCurrencyString(meanAmount, ticker).replace(',', '.'))
        assertEquals("$ticker 11111.11M", toCurrencyString(highAmount, ticker).replace(',', '.'))
    }
}
