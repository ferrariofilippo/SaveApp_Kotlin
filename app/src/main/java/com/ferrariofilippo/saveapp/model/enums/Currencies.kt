// Copyright (c) 2024 Filippo Ferrario
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
    CNY(7),
    BGN(8),
    BRL(9),
    CZK(10),
    DKK(11),
    HKD(12),
    HUF(13),
    IDR(14),
    ILS(15),
    INR(16),
    ISK(17),
    KRW(18),
    MXN(19),
    MYR(20),
    NOK(21),
    NZD(22),
    PHP(23),
    PLN(24),
    RON(25),
    SEK(26),
    SGD(27),
    THB(28),
    TRY(29),
    ZAR(30);

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
            BGN -> "лв"
            BRL -> "R\$"
            CZK -> "Kč"
            DKK -> "kr"
            HUF -> "Ft"
            IDR -> "Rp"
            ILS -> "₪"
            INR -> "₹"
            ISK -> "kr"
            KRW -> "₩"
            MYR -> "RM"
            NOK -> "kr"
            PHP -> "₱"
            PLN -> "zł"
            RON -> "lei"
            SEK -> "kr"
            THB -> "฿"
            TRY -> "₺"
            ZAR -> "R"
            else -> "$"
        }
    }
}