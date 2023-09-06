package com.ferrariofilippo.saveapp.util

import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import kotlinx.coroutines.flow.first
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

    fun getMovementFromSub(s: Subscription, description: String): Movement? {
        if (s.nextRenewal.isAfter(LocalDate.now()))
            return null

        s.lastPaid = s.nextRenewal
        updateNextRenewal(s)
        return Movement(
            0,
            s.amount,
            String.format(description, s.description, s.nextRenewal.toString()),
            s.lastPaid!!,
            s.tagId,
            s.budgetId
        )
    }

    suspend fun validateSubscriptions(application: SaveAppApplication) {
        val subscriptions = application.subscriptionRepository.getAll()
        val tags = application.tagRepository.allTags.first()
        val description = application.getString(R.string.payment_of)

        for (subscription in subscriptions) {
            var movement = getMovementFromSub(subscription, description)
            while (movement != null) {
                BudgetUtil.tryAddMovementToBudget(movement)
                application.movementRepository.insert(movement)
                StatsUtil.addMovementToStat(
                    movement,
                    tags.firstOrNull { it.id == movement!!.tagId }?.name
                )
                movement = getMovementFromSub(subscription, description)
            }
            application.subscriptionRepository.update(subscription)
        }
    }
}
