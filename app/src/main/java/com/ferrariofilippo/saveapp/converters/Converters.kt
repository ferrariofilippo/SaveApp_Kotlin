// Copyright (c) 2023 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.converters

import androidx.databinding.InverseMethod
import com.ferrariofilippo.saveapp.model.enums.Currencies
import com.ferrariofilippo.saveapp.model.enums.RenewalType
import java.time.LocalDate

object Converters {
    @InverseMethod("tickerToCurrency")
    @JvmStatic
    fun currencyToTicker(value: Currencies?): String {
        return value?.name ?: Currencies.EUR.name
    }

    @JvmStatic
    fun tickerToCurrency(value: String): Currencies {
        return when (value) {
            "EUR" -> Currencies.EUR
            "AUD" -> Currencies.AUD
            "CAD" -> Currencies.CAD
            "GBP" -> Currencies.GBP
            "CHF" -> Currencies.CHF
            "JPY" -> Currencies.JPY
            "CNY" -> Currencies.CNY
            "BGN" -> Currencies.BGN
            "BRL" -> Currencies.BRL
            "CZK" -> Currencies.CZK
            "DKK" -> Currencies.DKK
            "HKD" -> Currencies.HKD
            "HUF" -> Currencies.HUF
            "IDR" -> Currencies.IDR
            "ILS" -> Currencies.ILS
            "INR" -> Currencies.INR
            "ISK" -> Currencies.ISK
            "KRW" -> Currencies.KRW
            "MXN" -> Currencies.MXN
            "MYR" -> Currencies.MYR
            "NOK" -> Currencies.NOK
            "NZD" -> Currencies.NZD
            "PHP" -> Currencies.PHP
            "PLN" -> Currencies.PLN
            "RON" -> Currencies.RON
            "SEK" -> Currencies.SEK
            "SGD" -> Currencies.SGD
            "THB" -> Currencies.THB
            "TRY" -> Currencies.TRY
            "ZAR" -> Currencies.ZAR
            else -> Currencies.USD
        }
    }

    @InverseMethod("stringToDate")
    @JvmStatic
    fun dateToString(value: LocalDate?): String {
        return value?.toString() ?: LocalDate.now().toString()
    }

    @JvmStatic
    fun stringToDate(value: String): LocalDate {
        return LocalDate.parse(value)
    }

    @InverseMethod("stringToRenewal")
    @JvmStatic
    fun renewalToString(value: RenewalType?): String {
        return value?.name ?: RenewalType.WEEKLY.name
    }

    @JvmStatic
    fun stringToRenewal(value: String): RenewalType {
        return when (value) {
            "WEEKLY" -> RenewalType.WEEKLY
            "MONTHLY" -> RenewalType.MONTHLY
            "BIMONTHLY" -> RenewalType.BIMONTHLY
            "QUARTERLY" -> RenewalType.QUARTERLY
            "SEMIANNUALLY" -> RenewalType.SEMIANNUALLY
            else -> RenewalType.YEARLY
        }
    }
}