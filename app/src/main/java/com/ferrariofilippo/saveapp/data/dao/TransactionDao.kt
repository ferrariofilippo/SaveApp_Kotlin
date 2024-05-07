// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ferrariofilippo.saveapp.model.entities.Transaction
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<Transaction>

    @Query(
        "SELECT t.id, t.amount, t.description, t.date, t.tagId, t.budgetId, " +
                "tag.name AS tagName, tag.color AS tagColor " +
                "FROM transactions AS t JOIN tags as tag ON t.tagId = tag.id ORDER BY t.date DESC"
    )
    fun getAllTagged(): Flow<List<TaggedTransaction>>

    @Query(
        "SELECT t.id, t.amount, t.description, t.date, t.tagId, t.budgetId, tag.name AS tagName, " +
                "tag.color AS tagColor FROM transactions AS t JOIN tags as tag ON t.tagId = tag.id " +
                "WHERE strftime(\"%Y\", t.date) = :year"
    )
    suspend fun getAllTaggedByYear(year: String): List<TaggedTransaction>

    @Query(
        "SELECT t.id, t.amount, t.description, t.date, t.tagId, t.budgetId, tag.name AS tagName, " +
                "tag.color AS tagColor FROM transactions AS t JOIN tags as tag ON t.tagId = tag.id " +
                "WHERE strftime(\"%Y\", t.date) = :year ORDER BY t.date DESC"
    )
    suspend fun getAllTaggedByYearSorted(year: String): List<TaggedTransaction>

    @Query(
        "SELECT id, amount, description, date, tagId, budgetId FROM transactions " +
                "WHERE LOWER(description) LIKE :d AND (date BETWEEN :minDate AND :maxDate)"
    )
    suspend fun getByDescriptionWithinOneYear(d: String, minDate: String, maxDate: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Transaction?

    @Query(
        "SELECT * FROM transactions WHERE tagId = :id LIMIT 1"
    )
    suspend fun getFirstWithTag(id: Int): Transaction?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)
}
