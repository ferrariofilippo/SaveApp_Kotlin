// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model.statsitems

data class TagTransactionsSum(
    val tagId: Int,
    val name: String,
    val color: Int,
    val sum: Double,
    val percentage: Double
)
