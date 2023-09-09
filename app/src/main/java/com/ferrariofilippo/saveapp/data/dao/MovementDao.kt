package com.ferrariofilippo.saveapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement
import kotlinx.coroutines.flow.Flow

@Dao
interface MovementDao {
    @Query("SELECT * FROM movements")
    suspend fun getAll(): List<Movement>

    @Query(
        "SELECT m.id, m.amount, m.description, m.date, m.tagId, m.budgetId, " +
                "t.name AS tagName, t.color AS tagColor " +
                "FROM movements AS m JOIN tags as t ON m.tagId = t.id ORDER BY m.date DESC"
    )
    fun getAllTagged(): Flow<List<TaggedMovement>>

    @Query(
        "SELECT m.id, m.amount, m.description, m.date, m.tagId, m.budgetId, t.name AS tagName, " +
                "t.color AS tagColor FROM movements AS m JOIN tags as t ON m.tagId = t.id " +
                "WHERE strftime(\"%Y\", m.date) = :year"
    )
    suspend fun getAllTaggedByYear(year: String): List<TaggedMovement>

    @Query(
        "SELECT m.id, m.amount, m.description, m.date, m.tagId, m.budgetId, t.name AS tagName, " +
                "t.color AS tagColor FROM movements AS m JOIN tags as t ON m.tagId = t.id " +
                "WHERE strftime(\"%Y\", m.date) = :year ORDER BY m.date DESC"
    )
    suspend fun getAllTaggedByYearSorted(year: String): List<TaggedMovement>

    @Query("SELECT * FROM movements WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Movement?

    @Query(
        "SELECT m.id, m.amount, m.description, m.date, m.tagId, m.budgetId, " +
                "t.name AS tagName, t.color AS tagColor " +
                "FROM movements AS m JOIN tags as t ON m.tagId = t.id " +
                "WHERE m.id = :id LIMIT 1"
    )
    suspend fun getTaggedById(id: Int): TaggedMovement?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(movement: Movement)

    @Update
    suspend fun update(movement: Movement)

    @Delete
    suspend fun delete(movement: Movement)
}
