package com.ferrariofilippo.saveapp.util

import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.data.repository.BudgetRepository
import com.ferrariofilippo.saveapp.model.entities.Budget
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.enums.AddToBudgetResult

object BudgetUtil {
    private lateinit var budgetsRepository: BudgetRepository

    fun init(application: SaveAppApplication) {
        budgetsRepository = application.budgetRepository
    }

    suspend fun tryAddMovementToBudget(m: Movement): AddToBudgetResult {
        if (m.budgetId == null || m.budgetId == 0)
            return AddToBudgetResult.SUCCEEDED

        val budget: Budget? = budgetsRepository.getById(m.budgetId!!)
        if (budget == null) {
            m.budgetId = 0;
            return AddToBudgetResult.NOT_EXISTS
        }

        if (m.date.isBefore(budget.from) || m.date.isAfter(budget.to))
            return AddToBudgetResult.DATE_OUT_OF_RANGE

        if (budget.used >= budget.max)
            return AddToBudgetResult.BUDGET_EMPTY

        budget.used += m.amount
        budgetsRepository.update(budget)

        return AddToBudgetResult.SUCCEEDED
    }

    suspend fun removeMovementFromBudget(m: Movement) {
        if (m.budgetId == null || m.budgetId == 0)
            return

        val budget: Budget? = budgetsRepository.getById(m.budgetId!!)
        budget ?: return

        budget.used -= m.amount
        budgetsRepository.update(budget)
    }
}
