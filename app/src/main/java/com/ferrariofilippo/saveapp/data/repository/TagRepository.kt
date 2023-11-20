// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.data.repository

import androidx.annotation.WorkerThread
import com.ferrariofilippo.saveapp.data.dao.TagDao
import com.ferrariofilippo.saveapp.model.entities.Tag
import kotlinx.coroutines.flow.Flow

class TagRepository(private val tagDao: TagDao) {
    val allTags: Flow<List<Tag>> = tagDao.getAll()

    @WorkerThread
    suspend fun getById(id: Int): Tag? {
        return tagDao.getById(id)
    }

    @WorkerThread
    suspend fun insert(tag: Tag) {
        tagDao.insert(tag)
    }

    @WorkerThread
    suspend fun update(tag: Tag) {
        tagDao.update(tag)
    }

    @WorkerThread
    suspend fun delete(tag: Tag) {
        tagDao.delete(tag)
    }

    @WorkerThread
    suspend fun deleteAll() {
        tagDao.deleteAll()
    }
}
