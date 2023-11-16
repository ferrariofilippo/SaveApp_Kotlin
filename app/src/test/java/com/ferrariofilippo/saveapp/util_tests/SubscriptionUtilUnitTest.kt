package com.ferrariofilippo.saveapp.util_tests

import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import com.ferrariofilippo.saveapp.util.SubscriptionUtil
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class SubscriptionUtilUnitTest {
    @Test
    fun getMovementFromSubscription_isCorrect() {
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

        val movement = SubscriptionUtil.getMovementFromSub(subscription, str)
        val description =
            String.format(str, subscription.description, subscription.lastPaid.toString())

        assertNotNull(movement)
        assertNotNull(subscription.lastPaid)
        assertEquals(movement!!.id, 0)
        assertEquals(subscription.amount, movement.amount, Double.MIN_VALUE)
        assertEquals(description, movement.description)
        assertEquals(subscription.lastPaid, movement.date)
        assertEquals(subscription.budgetId, movement.budgetId)
        assertEquals(subscription.tagId, movement.tagId)

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
    fun getMovementFromSubscription_null_isCorrect() {
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

        val movement = SubscriptionUtil.getMovementFromSub(subscription, str)
        assertNull(movement)
        assertNull(subscription.lastPaid)
        assertEquals(subscription.nextRenewal, nextRenewal)
    }
}
