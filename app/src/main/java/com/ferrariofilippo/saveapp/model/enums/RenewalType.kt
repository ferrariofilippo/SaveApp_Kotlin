// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model.enums

enum class RenewalType(val multiplier: Int) {
    WEEKLY(52),
    MONTHLY(12),
    BIMONTHLY(6),
    QUARTERLY(3),
    SEMIANNUALLY(2),
    YEARLY(1)
}
