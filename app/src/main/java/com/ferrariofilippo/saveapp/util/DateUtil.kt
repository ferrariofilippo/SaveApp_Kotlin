// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.Locale

object DateUtil {
    private val _formatters: MutableMap<Int, DateTimeFormatter> = mutableMapOf()

    fun String.toLocalDateOrNull(): LocalDate? {
        try {
            return LocalDate.parse(this)
        } catch (e: DateTimeParseException) {
        }

        return null
    }

    fun getFormatter(locale: Locale): DateTimeFormatter {
        if (!_formatters.containsKey(locale.hashCode()))
            _formatters[locale.hashCode()] = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(locale)

        return _formatters[locale.hashCode()] ?: DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
    }
}
