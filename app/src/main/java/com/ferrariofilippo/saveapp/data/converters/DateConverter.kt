// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.data.converters

import androidx.room.TypeConverter
import java.time.LocalDate

class DateConverter {
    @TypeConverter
    fun toDate(date: String): LocalDate {
        return LocalDate.parse(date)
    }

    @TypeConverter
    fun toString(date: LocalDate): String {
        return date.toString()
    }
}
