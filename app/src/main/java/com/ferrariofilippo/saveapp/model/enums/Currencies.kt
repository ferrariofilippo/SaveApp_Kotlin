// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.model.enums

enum class Currencies(val id: Int) {
    EUR(0),
    USD(1),
    AUD(2),
    CAD(3),
    GBP(4),
    CHF(5),
    JPY(6),
    CNY(7);

    companion object {
        private val map = entries.associateBy { it.id }
        fun from(id: Int) : Currencies = map[id] ?: EUR
    }

    fun toSymbol() : String {
        return when (this) {
            EUR -> "€"
            GBP -> "£"
            CHF -> "Fr"
            JPY -> "¥"
            CNY -> "¥"
            else -> "$"
        }
    }
}