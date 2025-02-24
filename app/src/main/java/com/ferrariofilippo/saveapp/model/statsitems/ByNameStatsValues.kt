// Copyright (c) 2025 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model.statsitems

data class ByNameStatsValues(
    var weekSum: Double = .0,
    var weekCount: Int = 0,
    var weekMin: Double = Double.MAX_VALUE,
    var weekMax: Double = .0,
    var monthSum: Double = .0,
    var monthCount: Int = 0,
    var monthMin: Double = Double.MAX_VALUE,
    var monthMax: Double = .0,
    var yearSum: Double = .0,
    var yearCount: Int = 0,
    var yearMin: Double = Double.MAX_VALUE,
    var yearMax: Double = .0,
)
