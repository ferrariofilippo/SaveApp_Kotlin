// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Budget
import com.ferrariofilippo.saveapp.model.entities.Transaction
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.util.StringUtil.toBudgetOrNull
import com.ferrariofilippo.saveapp.util.StringUtil.toSubscriptionOrNull
import com.ferrariofilippo.saveapp.util.StringUtil.toTransactionOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDate

object ImportExportUtil {
    const val CREATE_TRANSACTIONS_FILE: Int = 2
    const val OPEN_TRANSACTIONS_FILE: Int = 3
    const val CREATE_TRANSACTIONS_TEMPLATE = 5

    const val CREATE_SUBSCRIPTIONS_FILE: Int = 7
    const val OPEN_SUBSCRIPTIONS_FILE: Int = 11
    const val CREATE_SUBSCRIPTIONS_TEMPLATE: Int = 13

    const val CREATE_BUDGETS_FILE: Int = 17
    const val OPEN_BUDGETS_FILE: Int = 19
    const val CREATE_BUDGETS_TEMPLATE = 23

    fun createExportFile(dataType: Int, activity: MainActivity) {
        val today = LocalDate.now().toString()
        when (dataType) {
            CREATE_TRANSACTIONS_FILE -> {
                activity.exportTransactions.launch(
                    String.format(
                        activity.getString(R.string.transactions_file_name),
                        today
                    )
                )
            }

            CREATE_SUBSCRIPTIONS_FILE -> {
                activity.exportSubscriptions.launch(
                    String.format(
                        activity.getString(R.string.subscriptions_file_name),
                        today
                    )
                )
            }

            CREATE_BUDGETS_FILE -> {
                activity.exportBudgets.launch(
                    String.format(
                        activity.getString(R.string.budgets_file_name),
                        today
                    )
                )
            }

            CREATE_TRANSACTIONS_TEMPLATE -> {
                activity.createTransactionsTemplate.launch(
                    activity.getString(R.string.transactions_template)
                )
            }

            CREATE_SUBSCRIPTIONS_TEMPLATE -> {
                activity.createSubscriptionsTemplate.launch(
                    activity.getString(R.string.subscriptions_template)
                )
            }

            else -> {
                activity.createBudgetsTemplate.launch(
                    activity.getString(R.string.budgets_template)
                )
            }
        }
    }

    fun export(type: Int, outputStream: FileOutputStream, app: SaveAppApplication) {
        val writer = outputStream.bufferedWriter()
        when (type) {
            CREATE_TRANSACTIONS_FILE -> exportTransactions(writer, app)
            CREATE_SUBSCRIPTIONS_FILE -> exportSubscriptions(writer, app)
            CREATE_BUDGETS_FILE -> exportBudgets(writer, app)
        }

        writer.flush()
        outputStream.flush()
        outputStream.close()
    }

    fun writeTemplate(type: Int, outputStream: FileOutputStream, app: SaveAppApplication) {
        val header = when (type) {
            CREATE_TRANSACTIONS_TEMPLATE -> app.getString(R.string.transactions_export_header)
            CREATE_SUBSCRIPTIONS_TEMPLATE -> app.getString(R.string.subscriptions_export_header)
            else -> app.getString(R.string.budgets_export_header)
        }

        outputStream.write(header.toByteArray())

        outputStream.flush()
        outputStream.close()
    }

    fun getFromFile(dataType: Int, activity: MainActivity) {
        val filter = "text/comma-separated-values"
        when (dataType) {
            OPEN_TRANSACTIONS_FILE -> activity.importTransactions.launch(filter)
            OPEN_SUBSCRIPTIONS_FILE -> activity.importSubscriptions.launch(filter)
            OPEN_BUDGETS_FILE -> activity.importBudgets.launch(filter)
        }
    }

    fun import(type: Int, inputStream: FileInputStream, app: SaveAppApplication) {
        val reader = inputStream.bufferedReader()

        // Skip header line
        reader.readLine()
        when (type) {
            OPEN_TRANSACTIONS_FILE -> importTransactions(reader, app)
            OPEN_SUBSCRIPTIONS_FILE -> importSubscriptions(reader, app)
            OPEN_BUDGETS_FILE -> importBudgets(reader, app)
        }

        reader.close()
        inputStream.close()
    }

    private fun exportTransactions(writer: BufferedWriter, app: SaveAppApplication) {
        val transactions = runBlocking { app.transactionRepository.allTaggedTransactions.first() }

        writer.write(app.getString(R.string.transactions_export_header))
        writer.newLine()
        for (m in transactions) {
            writer.write("${m.id},${m.amount},${m.description},${m.date},${m.tagId},${m.budgetId}")
            writer.newLine()
        }
    }

    private fun exportSubscriptions(writer: BufferedWriter, app: SaveAppApplication) {
        val subscriptions =
            runBlocking { app.subscriptionRepository.allTaggedSubscriptions.first() }

        writer.write(app.getString(R.string.subscriptions_export_header))
        writer.newLine()
        for (s in subscriptions) {
            writer.write(
                "${s.id},${s.amount},${s.description},${s.renewalType}," +
                        "${s.creationDate},${s.lastPaid},${s.nextRenewal},${s.tagId},${s.budgetId}"
            )
            writer.newLine()
        }
    }

    private fun exportBudgets(writer: BufferedWriter, app: SaveAppApplication) {
        val budgets = runBlocking { app.budgetRepository.allBudgets.first() }

        writer.write(app.getString(R.string.budgets_export_header))
        writer.newLine()
        for (b in budgets) {
            writer.write("${b.budgetId},${b.max},${b.used},${b.name},${b.from},${b.to},${b.tagId}")
            writer.newLine()
        }
    }

    private fun importTransactions(reader: BufferedReader, app: SaveAppApplication) {
        val addTransactions: MutableList<Transaction> = mutableListOf()
        val updateTransactions: MutableList<Transaction> = mutableListOf()

        try {
            reader.lines().forEach {
                if (it != null) {
                    val m = it.toTransactionOrNull()
                    if (m != null) {
                        if (m.id == 0) {
                            addTransactions.add(m)
                        } else {
                            updateTransactions.add(m)
                        }
                    }
                }
            }
            app.applicationScope.launch {
                addTransactions.forEach {
                    BudgetUtil.tryAddTransactionToBudget(it)
                    app.transactionRepository.insert(it)
                    StatsUtil.addTransactionToStat(app, it)
                }
                updateTransactions.forEach {
                    val oldTransaction = app.transactionRepository.getById(it.id)
                    if (oldTransaction != null) {
                        if (it.budgetId != 0) {
                            BudgetUtil.removeTransactionFromBudget(oldTransaction)
                            BudgetUtil.tryAddTransactionToBudget(it)
                        }

                        oldTransaction.amount *= -1
                        StatsUtil.addTransactionToStat(app, oldTransaction)
                    }

                    app.transactionRepository.update(it)
                    StatsUtil.addTransactionToStat(app, it)
                }
            }
        } catch (e: Exception) {
            LogUtil.logException(e, javaClass.kotlin.simpleName ?: "", "importTransactions")
        }
    }

    private fun importSubscriptions(reader: BufferedReader, app: SaveAppApplication) {
        val addSubscriptions: MutableList<Subscription> = mutableListOf()
        val updateSubscriptions: MutableList<Subscription> = mutableListOf()

        try {
            reader.lines().forEach {
                if (it != null) {
                    val s = it.toSubscriptionOrNull()
                    if (s != null) {
                        if (s.id == 0) {
                            addSubscriptions.add(s)
                        } else {
                            updateSubscriptions.add(s)
                        }
                    }
                }
            }
            app.applicationScope.launch {
                addSubscriptions.forEach {
                    app.subscriptionRepository.insert(it)
                }
                updateSubscriptions.forEach {
                    app.subscriptionRepository.update(it)
                }
            }
        } catch (e: Exception) {
            LogUtil.logException(e, javaClass.kotlin.simpleName ?: "", "importSubscriptions")
        }
    }

    private fun importBudgets(reader: BufferedReader, app: SaveAppApplication) {
        val addBudgets: MutableList<Budget> = mutableListOf()
        val updateBudgets: MutableList<Budget> = mutableListOf()

        try {
            reader.lines().forEach {
                if (it != null) {
                    val b = it.toBudgetOrNull()
                    if (b != null) {
                        if (b.id == 0) {
                            addBudgets.add(b)
                        } else {
                            updateBudgets.add(b)
                        }
                    }
                }
            }
            app.applicationScope.launch {
                addBudgets.forEach {
                    app.budgetRepository.insert(it)
                }
                updateBudgets.forEach {
                    app.budgetRepository.update(it)
                }
            }
        } catch (e: Exception) {
            LogUtil.logException(e, javaClass.kotlin.simpleName ?: "", "importBudgets")
        }
    }
}
