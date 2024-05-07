// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ferrariofilippo.saveapp.data.converters.DateConverter
import java.time.LocalDate

@Entity(tableName = "transactions", indices = [Index(value = ["date"])])
@TypeConverters(DateConverter::class)
data class Transaction(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var amount: Double,
    var description: String,
    var date: LocalDate,
    var tagId: Int,
    var budgetId: Int?
)
