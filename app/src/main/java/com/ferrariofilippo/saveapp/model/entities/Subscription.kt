// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ferrariofilippo.saveapp.data.converters.DateConverter
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import java.time.LocalDate

@Entity(tableName = "subscriptions")
@TypeConverters(DateConverter::class)
data class Subscription(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var amount: Double,
    var description: String,
    @ColumnInfo(name = "renewal_type") var renewalType: RenewalType,
    @ColumnInfo(name = "creation_date") var creationDate: LocalDate,
    @ColumnInfo(name = "last_paid") var lastPaid: LocalDate?,
    @ColumnInfo(name = "next_renewal") var nextRenewal: LocalDate,
    var tagId: Int,
    var budgetId: Int?
)
