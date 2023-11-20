// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model.taggeditems

import androidx.room.TypeConverters
import com.ferrariofilippo.saveapp.data.converters.DateConverter
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import java.time.LocalDate

@TypeConverters(DateConverter::class)
data class TaggedSubscription(
    var id: Int,
    var amount: Double,
    var description: String,
    var renewalType: RenewalType,
    var creationDate: LocalDate,
    var lastPaid: LocalDate?,
    var nextRenewal: LocalDate,
    var tagId: Int,
    var tagName: String,
    var tagColor: Int,
    var budgetId: Int?
)
