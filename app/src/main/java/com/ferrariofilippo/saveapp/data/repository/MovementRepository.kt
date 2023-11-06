package com.ferrariofilippo.saveapp.data.repository

import androidx.annotation.WorkerThread
import com.ferrariofilippo.saveapp.data.dao.MovementDao
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedMovement
import kotlinx.coroutines.flow.Flow

class MovementRepository(private val movementDao: MovementDao) {
    val allTaggedMovements: Flow<List<TaggedMovement>> = movementDao.getAllTagged()

    @WorkerThread
    suspend fun getAll(): List<Movement> {
        return movementDao.getAll()
    }

    @WorkerThread
    suspend fun getAllTaggedByYear(year: String): List<TaggedMovement> {
        return movementDao.getAllTaggedByYear(year)
    }

    @WorkerThread
    suspend fun getAllTaggedByYearSorted(year: String): List<TaggedMovement> {
        return movementDao.getAllTaggedByYearSorted(year)
    }

    @WorkerThread
    suspend fun getById(id: Int): Movement? {
        return movementDao.getById(id)
    }

    @WorkerThread
    suspend fun getFirstWithTag(id: Int): Movement? {
        return movementDao.getFirstWithTag(id)
    }

    @WorkerThread
    suspend fun insert(movement: Movement) {
        movementDao.insert(movement)
    }

    @WorkerThread
    suspend fun update(movement: Movement) {
        movementDao.update(movement)
    }

    @WorkerThread
    suspend fun delete(movement: Movement) {
        movementDao.delete(movement)
    }
}
