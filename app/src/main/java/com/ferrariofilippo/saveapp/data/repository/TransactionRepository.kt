// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.data.repository

import androidx.annotation.WorkerThread
import com.ferrariofilippo.saveapp.data.dao.TransactionDao
import com.ferrariofilippo.saveapp.model.entities.Transaction
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedTransaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTaggedTransactions: Flow<List<TaggedTransaction>> = transactionDao.getAllTagged()

    @WorkerThread
    suspend fun getAll(): List<Transaction> {
        return transactionDao.getAll()
    }

    @WorkerThread
    suspend fun getAllTaggedByYear(year: String): List<TaggedTransaction> {
        return transactionDao.getAllTaggedByYear(year)
    }

    @WorkerThread
    suspend fun getAllTaggedByYearSorted(year: String): List<TaggedTransaction> {
        return transactionDao.getAllTaggedByYearSorted(year)
    }

    @WorkerThread
    suspend fun getByDescriptionWithinOneYear(d: String): List<Transaction> {
        val today = LocalDate.now()
        return transactionDao.getByDescriptionWithinOneYear(
            "%${d.lowercase()}%",
            today.minusYears(1).toString(),
            today.toString()
        )
    }

    @WorkerThread
    suspend fun getById(id: Int): Transaction? {
        return transactionDao.getById(id)
    }

    @WorkerThread
    suspend fun getFirstWithTag(id: Int): Transaction? {
        return transactionDao.getFirstWithTag(id)
    }

    @WorkerThread
    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    @WorkerThread
    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    @WorkerThread
    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }
}
