// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model

data class ByNameStatsData(
    var currentWeekSum: Double = 0.0,
    var currentMonthSum: Double = 0.0,
    var currentMonthFrequency: Int = 0,
    var transactionSum: Double = 0.0,
    var frequency: Int = 0
)
