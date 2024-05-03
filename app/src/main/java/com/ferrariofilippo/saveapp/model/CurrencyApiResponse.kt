// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model

import com.squareup.moshi.Json

data class CurrencyApiResponse(
    @Json(name="amount") val amount: Int,
    @Json(name="base") val base: String,
    @Json(name="date") val date: String,
    @Json(name="rates") val rates: Map<String, Double>
)
