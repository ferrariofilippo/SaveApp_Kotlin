package com.ferrariofilippo.saveapp.model.taggeditems

import androidx.room.TypeConverters
import com.ferrariofilippo.saveapp.data.converters.DateConverter
import java.time.LocalDate

@TypeConverters(DateConverter::class)
data class TaggedBudget(
    var budgetId: Int,
    var max: Double,
    var used: Double,
    var name: String,
    var from: LocalDate,
    var to: LocalDate,
    var tagId: Int,
    var tagName: String,
    var tagColor: Int,
);
