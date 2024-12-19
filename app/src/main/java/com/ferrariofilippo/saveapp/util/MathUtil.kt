// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

object MathUtil {
    @Throws(ArithmeticException::class)
    fun solve(expr: String): Double {
        val opsQueue = ArrayDeque<Char>()
        val nQueue = ArrayDeque<Double>()
        val opsStack = ArrayDeque<Char>()
        val nStack = ArrayDeque<Double>()
        var previousOp = '+'
        var currentOp: Char

        if (!parseExpression(expr, opsQueue, nQueue) || nQueue.isEmpty()) {
            throw ArithmeticException("Expression parsing failed")
        }

        while (nQueue.isNotEmpty() && opsQueue.isNotEmpty()) {
            currentOp = opsQueue.removeFirst()
            if (isOperator(currentOp) && currentOp != '(' && previousOp != ')') {
                nStack.addFirst(nQueue.removeFirst())
            }

            while (opsStack.isNotEmpty() && !nextHasPriority(currentOp, opsStack.first())) {
                previousOp = opsStack.removeFirst()
                nStack.addFirst(compute(nStack.removeFirst(), nStack.removeFirst(), previousOp))
            }

            if (previousOp == '(' && currentOp == ')') {
                opsStack.removeFirst()
                previousOp = currentOp
            } else {
                opsStack.addFirst(currentOp)
            }
        }

        if (nQueue.isNotEmpty()) {
            nStack.addFirst(nQueue.removeFirst())
        }

        // Copy remaining operators
        while (opsQueue.isNotEmpty()) {
            opsStack.addFirst(opsQueue.removeFirst())
        }

        while (nStack.size > 1 && opsStack.isNotEmpty()) {
            val operator = opsStack.removeFirst()
            if (operator != '(' && operator != ')') {
                nStack.addFirst(compute(nStack.removeFirst(), nStack.removeFirst(), operator))
            }
        }

        return nStack.firstOrNull() ?: 0.0
    }

    private fun parseExpression(
        expr: String,
        ops: ArrayDeque<Char>,
        values: ArrayDeque<Double>
    ): Boolean {
        var currentValue = 0.0
        var isDecimal = false
        var anyNumber = false
        var divider = 1
        var openedBracketsCount = 0
        var closedBracketCount = 0

        // Start from 1 to skip the initial '='
        for (i in 1 until expr.length) {
            if (isOperator(expr[i])) {
                if (anyNumber) {
                    values.addLast(currentValue / divider)

                    // Reset
                    currentValue = 0.0
                    divider = 1
                    isDecimal = false
                    anyNumber = false
                } else if (ops.isEmpty() && expr[i] != '(') {
                    values.addLast(0.0)
                }

                ops.addLast(expr[i])
                openedBracketsCount += if (expr[i] == '(') 1 else 0
                closedBracketCount += if (expr[i] == ')') 1 else 0
            } else if (expr[i] != ' ') {
                if (expr[i] == '.' || expr[i] == ',') {
                    isDecimal = true
                } else if (expr[i] < '0' || expr[i] > '9') {
                    return false
                } else {
                    currentValue *= 10
                    currentValue += expr[i].code - '0'.code

                    if (isDecimal) {
                        divider *= 10
                    }
                }

                anyNumber = true
            }
        }

        if (anyNumber) {
            values.addLast(currentValue / divider)
        }

        return openedBracketsCount == closedBracketCount
    }

    private fun nextHasPriority(next: Char, prev: Char): Boolean {
        return next == '*' || next == '/' || next == '(' || prev == '('
    }

    private fun isOperator(c: Char): Boolean {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')'
    }

    @Throws(ArithmeticException::class)
    private fun compute(y: Double, x: Double, operator: Char): Double {
        return when (operator) {
            '+' -> x + y
            '-' -> x - y
            '*' -> x * y
            else -> if (y != 0.0) x / y else throw ArithmeticException()
        }
    }
}
