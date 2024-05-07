// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import com.ferrariofilippo.saveapp.converters.Converters
import com.ferrariofilippo.saveapp.model.entities.Budget
import com.ferrariofilippo.saveapp.model.entities.Transaction
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.util.DateUtil.toLocalDateOrNull
import java.time.LocalDate

object StringUtil {
    fun String.toTransactionOrNull(): Transaction? {
        val fields: List<String> = this.split(',')
        if (fields.size != 6) {
            return null
        }

        val id: Int = fields[0].toIntOrNull() ?: return null
        val amount: Double = fields[1].toDoubleOrNull() ?: return null
        val description = fields[2]
        val date: LocalDate = fields[3].toLocalDateOrNull() ?: return null
        val tagId = fields[4].toIntOrNull() ?: 9
        val budgetId: Int = fields[5].toIntOrNull() ?: 0

        return Transaction(
            id, amount, description, date, tagId, budgetId
        )
    }

    fun String.toSubscriptionOrNull(): Subscription? {
        val fields: List<String> = this.split(',')
        if (fields.size != 9) {
            return null
        }

        val id: Int = fields[0].toIntOrNull() ?: return null
        val amount: Double = fields[1].toDoubleOrNull() ?: return null
        val description = fields[2]
        val renewalType = Converters.stringToRenewal(fields[3])
        val creationDate: LocalDate = fields[4].toLocalDateOrNull() ?: return null
        val lastPaid: LocalDate = fields[5].toLocalDateOrNull() ?: return null
        val nextRenewal: LocalDate = fields[6].toLocalDateOrNull() ?: return null
        val tagId = fields[7].toIntOrNull() ?: 9
        val budgetId: Int = fields[8].toIntOrNull() ?: 0

        return Subscription(
            id,
            amount,
            description,
            renewalType,
            creationDate,
            lastPaid,
            nextRenewal,
            tagId,
            budgetId
        )
    }

    fun String.toBudgetOrNull(): Budget? {
        val fields: List<String> = this.split(',')
        if (fields.size != 7) {
            return null
        }

        val id: Int = fields[0].toIntOrNull() ?: return null
        val max: Double = fields[1].toDoubleOrNull() ?: return null
        val used: Double = fields[2].toDoubleOrNull() ?: return null
        val name = fields[3]
        val from: LocalDate = fields[4].toLocalDateOrNull() ?: return null
        val to: LocalDate = fields[5].toLocalDateOrNull() ?: return null
        val tagId = fields[6].toIntOrNull() ?: 9

        return Budget(
            id, max, used, name, from, to, tagId
        )
    }
}
