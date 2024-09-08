// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model.enums

enum class PeriodicInterval(val minutes: Long) {
    DAILY(1440),        // 60 * 24
    TWO_DAYS(2880),     // 60 * 24 * 2
    WEEKLY(10_080),     // 60 * 24 * 7
    TWO_WEEKS(20_160),  // 60 * 24 * 14
    MONTHLY(43_829);     // 60 * 24 * 365.2425 / 12

    companion object {
        private val map = PeriodicInterval.entries.associateBy { it.minutes }
        fun from(minutes: Long): PeriodicInterval = map[minutes] ?: PeriodicInterval.WEEKLY
    }
}
