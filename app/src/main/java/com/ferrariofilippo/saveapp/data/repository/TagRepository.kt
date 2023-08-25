package com.ferrariofilippo.saveapp.data.repository

import androidx.annotation.WorkerThread
import com.ferrariofilippo.saveapp.data.dao.TagDao
import com.ferrariofilippo.saveapp.model.entities.Tag
import kotlinx.coroutines.flow.Flow

class TagRepository(private val tagDao: TagDao) {
    val allTags: Flow<List<Tag>> = tagDao.getAll();

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getById(id: Int): Tag? {
        return tagDao.getById(id);
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(tag: Tag) {
        tagDao.insert(tag);
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(tag: Tag) {
        tagDao.update(tag);
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(tag: Tag) {
        tagDao.delete(tag);
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        tagDao.deleteAll();
    }
}
