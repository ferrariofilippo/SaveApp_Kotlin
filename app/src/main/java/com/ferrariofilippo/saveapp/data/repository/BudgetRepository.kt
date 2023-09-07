package com.ferrariofilippo.saveapp.data.repository

import androidx.annotation.WorkerThread
import com.ferrariofilippo.saveapp.data.dao.BudgetDao
import com.ferrariofilippo.saveapp.model.entities.Budget
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedBudget
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {
    val allBudgets: Flow<List<TaggedBudget>> = budgetDao.getAllTagged();

    @WorkerThread
    suspend fun getById(id: Int) : Budget? {
        return budgetDao.getById(id)
    }

    @WorkerThread
    suspend fun getTaggedById(id: Int): TaggedBudget? {
        return budgetDao.getTaggedById(id);
    }

    @WorkerThread
    suspend fun insert(budget: Budget) {
        budgetDao.insert(budget);
    }

    @WorkerThread
    suspend fun update(budget: Budget) {
        budgetDao.update(budget);
    }

    @WorkerThread
    suspend fun delete(budget: Budget) {
        budgetDao.delete(budget);
    }
}
