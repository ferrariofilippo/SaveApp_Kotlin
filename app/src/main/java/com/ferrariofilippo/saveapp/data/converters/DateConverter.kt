package com.ferrariofilippo.saveapp.data.converters

import androidx.room.TypeConverter
import java.time.LocalDate

class DateConverter {
    @TypeConverter
    public fun toDate(date: String): LocalDate {
        return LocalDate.parse(date);
    }

    @TypeConverter
    public fun toString(date: LocalDate): String {
        return date.toString();
    }
}