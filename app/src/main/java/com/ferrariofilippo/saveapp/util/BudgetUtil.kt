// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.data.repository.BudgetRepository
import com.ferrariofilippo.saveapp.model.entities.Budget
import com.ferrariofilippo.saveapp.model.entities.Transaction
import com.ferrariofilippo.saveapp.model.enums.AddToBudgetResult

object BudgetUtil {
    private lateinit var budgetsRepository: BudgetRepository

    fun init(application: SaveAppApplication) {
        budgetsRepository = application.budgetRepository
    }

    suspend fun tryAddTransactionToBudget(t: Transaction, force: Boolean = false): AddToBudgetResult {
        if (t.budgetId == null || t.budgetId == 0) {
            return AddToBudgetResult.SUCCEEDED
        }

        val budget: Budget? = budgetsRepository.getById(t.budgetId!!)
        if (budget == null) {
            t.budgetId = 0
            return if (force) AddToBudgetResult.SUCCEEDED else AddToBudgetResult.NOT_EXISTS
        }

        if (!force) {
            if (t.date.isBefore(budget.from) || t.date.isAfter(budget.to)) {
                return AddToBudgetResult.DATE_OUT_OF_RANGE
            }

            if (budget.used >= budget.max) {
                return AddToBudgetResult.BUDGET_EMPTY
            }
        }

        budget.used += t.amount
        budgetsRepository.update(budget)

        return AddToBudgetResult.SUCCEEDED
    }

    suspend fun removeTransactionFromBudget(t: Transaction) {
        if (t.budgetId == null || t.budgetId == 0) {
            return
        }

        val budget: Budget? = budgetsRepository.getById(t.budgetId!!)
        budget ?: return

        budget.used -= t.amount
        budgetsRepository.update(budget)
    }
}
