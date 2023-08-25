package com.ferrariofilippo.saveapp.util

import com.ferrariofilippo.saveapp.model.entities.Budget
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.enums.AddToBudgetResult

object BudgetUtil {
    fun tryAddMovementToBudget(m: Movement): AddToBudgetResult {
        if (m.budgetId == 0)
            return AddToBudgetResult.SUCCEEDED;

        val budget: Budget? = null;
        if (budget == null) {
            m.budgetId = 0;
            return AddToBudgetResult.NOT_EXISTS;
        }

        if (m.date.isBefore(budget.from) || m.date.isAfter(budget.to))
            return AddToBudgetResult.DATE_OUT_OF_RANGE;

        if (budget.used >= budget.max)
            return AddToBudgetResult.BUDGET_EMPTY;

        budget.used += m.amount;

        if (budget.used >= budget.max) {
            // TODO: Send notification
        }

        return AddToBudgetResult.SUCCEEDED;
    }

    fun removeMovementFromBudget(m: Movement) {
        if (m.budgetId == 0)
            return;

        val budget: Budget? = null;
        budget ?: return;

        budget.used -= m.amount;
    }

    fun validateBudgets() {
        // TODO
    }
}