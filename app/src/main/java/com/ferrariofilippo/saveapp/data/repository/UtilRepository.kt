// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.data.repository

import androidx.annotation.WorkerThread
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ferrariofilippo.saveapp.data.dao.UtilDao

class UtilRepository(private val utilDao: UtilDao) {
    @WorkerThread
    fun checkpoint() {
        utilDao.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL);"))
        utilDao.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE);"))
    }
}
