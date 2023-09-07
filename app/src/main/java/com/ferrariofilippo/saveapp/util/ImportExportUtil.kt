package com.ferrariofilippo.saveapp.util

import com.ferrariofilippo.saveapp.MainActivity
import com.ferrariofilippo.saveapp.R
import com.ferrariofilippo.saveapp.SaveAppApplication
import com.ferrariofilippo.saveapp.model.entities.Budget
import com.ferrariofilippo.saveapp.model.entities.Movement
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.util.StringUtil.toBudgetOrNull
import com.ferrariofilippo.saveapp.util.StringUtil.toMovementOrNull
import com.ferrariofilippo.saveapp.util.StringUtil.toSubscriptionOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDate

object ImportExportUtil {
    const val CREATE_MOVEMENTS_FILE: Int = 2
    const val OPEN_MOVEMENTS_FILE: Int = 3
    const val CREATE_MOVEMENTS_TEMPLATE = 5

    const val CREATE_SUBSCRIPTIONS_FILE: Int = 7
    const val OPEN_SUBSCRIPTIONS_FILE: Int = 11
    const val CREATE_SUBSCRIPTIONS_TEMPLATE: Int = 13

    const val CREATE_BUDGETS_FILE: Int = 17
    const val OPEN_BUDGETS_FILE: Int = 19
    const val CREATE_BUDGETS_TEMPLATE = 23

    fun createExportFile(dataType: Int, activity: MainActivity) {
        val today = LocalDate.now().toString()
        when (dataType) {
            CREATE_MOVEMENTS_FILE -> {
                activity.exportMovements.launch(
                    String.format(
                        activity.getString(R.string.movements_file_name),
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

            CREATE_MOVEMENTS_TEMPLATE -> {
                activity.createMovementsTemplate.launch(
                    activity.getString(R.string.movements_template)
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
            CREATE_MOVEMENTS_FILE -> exportMovements(writer, app)
            CREATE_SUBSCRIPTIONS_FILE -> exportSubscriptions(writer, app)
            CREATE_BUDGETS_FILE -> exportBudgets(writer, app)
        }

        writer.flush()
        outputStream.flush()
        outputStream.close()
    }

    fun writeTemplate(type: Int, outputStream: FileOutputStream, app: SaveAppApplication) {
        val header = when (type) {
            CREATE_MOVEMENTS_TEMPLATE -> app.getString(R.string.movements_export_header)
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
            OPEN_MOVEMENTS_FILE -> activity.importMovements.launch(filter)
            OPEN_SUBSCRIPTIONS_FILE -> activity.importSubscriptions.launch(filter)
            OPEN_BUDGETS_FILE -> activity.importBudgets.launch(filter)
        }
    }

    fun import(type: Int, inputStream: FileInputStream, app: SaveAppApplication) {
        val reader = inputStream.bufferedReader()

        // Skip header line
        reader.readLine()
        when (type) {
            OPEN_MOVEMENTS_FILE -> importMovements(reader, app)
            OPEN_SUBSCRIPTIONS_FILE -> importSubscriptions(reader, app)
            OPEN_BUDGETS_FILE -> importBudgets(reader, app)
        }

        reader.close()
        inputStream.close()
    }

    private fun exportMovements(writer: BufferedWriter, app: SaveAppApplication) {
        val movements = runBlocking { app.movementRepository.allTaggedMovements.first() }

        writer.write(app.getString(R.string.movements_export_header))
        writer.newLine()
        for (m in movements) {
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

    private fun importMovements(reader: BufferedReader, app: SaveAppApplication) {
        val addMovements: MutableList<Movement> = mutableListOf()
        val updateMovements: MutableList<Movement> = mutableListOf()

        try {
            reader.lines().forEach {
                if (it != null) {
                    val m = it.toMovementOrNull()
                    if (m != null) {
                        if (m.id == 0)
                            addMovements.add(m)
                        else
                            updateMovements.add(m)
                    }
                }
            }
            app.applicationScope.launch {
                val tags = app.tagRepository.allTags.first()
                addMovements.forEach {
                    BudgetUtil.tryAddMovementToBudget(it)
                    app.movementRepository.insert(it)
                    StatsUtil.addMovementToStat(
                        it,
                        tags.firstOrNull { tag -> tag.id == it.tagId }?.name
                    )
                }
                updateMovements.forEach {
                    val oldMovement = app.movementRepository.getById(it.id)
                    if (oldMovement != null) {
                        if (it.budgetId != 0) {
                            BudgetUtil.removeMovementFromBudget(oldMovement)
                            BudgetUtil.tryAddMovementToBudget(it)
                        }

                        oldMovement.amount *= -1
                        StatsUtil.addMovementToStat(
                            oldMovement,
                            tags.firstOrNull { tag -> tag.id == oldMovement.tagId }?.name
                        )
                    }

                    app.movementRepository.update(it)
                    StatsUtil.addMovementToStat(
                        it,
                        tags.firstOrNull { tag -> tag.id == it.tagId }?.name
                    )
                }
            }
        } catch (_: Exception) {
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
                        if (s.id == 0)
                            addSubscriptions.add(s)
                        else
                            updateSubscriptions.add(s)
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
        } catch (_: Exception) {
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
                        if (b.id == 0)
                            addBudgets.add(b)
                        else
                            updateBudgets.add(b)
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
        } catch (_: Exception) {
        }
    }
}
