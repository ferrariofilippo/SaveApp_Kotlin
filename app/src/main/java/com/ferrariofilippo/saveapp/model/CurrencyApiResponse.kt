// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model

data class CurrencyApiResponse(
    val amount: Int,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
