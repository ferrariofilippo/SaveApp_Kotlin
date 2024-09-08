// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

object TimeUtil {
    fun getInitialDelay(hour: Int): Long {
        val now = LocalDateTime.now()
        val targetTime = LocalDateTime.of(
            now.toLocalDate().plusDays(if (now.hour >= hour) 1 else 0),
            LocalTime.of(hour, 0)
        )

        return now.until(targetTime, ChronoUnit.MINUTES)
    }
}
