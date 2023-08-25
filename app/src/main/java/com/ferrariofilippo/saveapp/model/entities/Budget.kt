package com.ferrariofilippo.saveapp.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ferrariofilippo.saveapp.data.converters.DateConverter
import java.time.LocalDate

@Entity(tableName = "budgets")
@TypeConverters(DateConverter::class)
data class Budget(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var max: Double,
    var used: Double,
    var name: String,
    var from: LocalDate,
    var to: LocalDate,
    var tagId: Int
);
