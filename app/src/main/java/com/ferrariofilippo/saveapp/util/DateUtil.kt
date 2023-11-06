package com.ferrariofilippo.saveapp.util

import java.time.LocalDate
import java.time.format.DateTimeParseException

object DateUtil {
    fun String.toLocalDateOrNull(): LocalDate? {
        try {
            return LocalDate.parse(this)
        } catch (e: DateTimeParseException) {
        }

        return null
    }
}