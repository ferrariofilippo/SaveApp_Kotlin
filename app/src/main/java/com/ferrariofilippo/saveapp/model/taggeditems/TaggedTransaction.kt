// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model.taggeditems

import androidx.room.TypeConverters
import com.ferrariofilippo.saveapp.data.converters.DateConverter
import java.time.LocalDate

@TypeConverters(DateConverter::class)
data class TaggedTransaction(
    var id: Int,
    var amount: Double,
    var description: String,
    var date: LocalDate,
    var tagId: Int,
    var tagName: String,
    var tagColor: Int,
    var budgetId: Int?
)
