package com.ferrariofilippo.saveapp.util_tests

import com.ferrariofilippo.saveapp.model.entities.Budget
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import com.ferrariofilippo.saveapp.util.StringUtil.toBudgetOrNull
import com.ferrariofilippo.saveapp.util.StringUtil.toMovementOrNull
import com.ferrariofilippo.saveapp.util.StringUtil.toSubscriptionOrNull
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class StringUtilUnitTest {
    @Test
    fun toMovementOrNull_isCorrect() {
        val date = LocalDate.now()
        val m = Movement(1, 12.9, "Test movement", date, 2, 3)
        val movementStr = "${m.id},${m.amount},${m.description},${date},${m.tagId},${m.budgetId}";
        val parsedMovement = movementStr.toMovementOrNull()

        assertNotNull(parsedMovement)
        assertEquals(parsedMovement!!.id, m.id)
        assertEquals(parsedMovement.amount, m.amount, Double.MIN_VALUE)
        assertEquals(parsedMovement.description, m.description)
        assertEquals(parsedMovement.date, m.date)
        assertEquals(parsedMovement.tagId, m.tagId)
        assertEquals(parsedMovement.budgetId, m.budgetId)

        // Default values
        assertNotNull("1,${10.0},description,${date},NOT_AN_INT,3".toMovementOrNull())
        assertNotNull("1,${10.0},description,${date},2,NOT_AN_INT".toMovementOrNull())
    }

    @Test
    fun toMovementOrNull_null_isCorrect() {
        val d = LocalDate.now()

        assertNull("".toMovementOrNull())
        assertNull(",,,,,".toMovementOrNull())
        assertNull("NOT_AN_INT,${10.0},description,${d},2,3".toMovementOrNull())
        assertNull("${1.1},${10.0},description,${d},2,3".toMovementOrNull())
        assertNull("1,NOT_A_DOUBLE,description,${d},2,3".toMovementOrNull())
        assertNull("1,${10.0},description,NOT_A_DATE,2,3".toMovementOrNull())
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
}
