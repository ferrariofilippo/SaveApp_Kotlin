package com.ferrariofilippo.saveapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ferrariofilippo.saveapp.model.entities.Budget
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedBudget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query(
        "SELECT b.id as budgetId, b.max, b.used, b.name, b.`from`, b.`to`, b.tagId, " +
                "t.name AS tagName, t.color AS tagColor " +
                "FROM budgets AS b JOIN tags AS t ON b.tagId = t.id"
    )
    fun getAllTagged(): Flow<List<TaggedBudget>>

    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1")
   suspend fun getById(id: Int): Budget?

    @Query(
        "SELECT b.id as budgetId, b.max, b.used, b.name, b.`from`, b.`to`, b.tagId, " +
                "t.name AS tagName, t.color AS tagColor " +
                "FROM budgets AS b JOIN tags AS t ON b.tagId = t.id WHERE budgetId = :id LIMIT 1"
    )
    suspend fun getTaggedById(id: Int): TaggedBudget?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(budget: Budget)

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)
}
