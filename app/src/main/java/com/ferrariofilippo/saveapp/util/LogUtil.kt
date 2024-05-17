// Copyright (c) 2024 Filippo Ferrario
// Licensed under the MIT License. See the LICENSE.

package com.ferrariofilippo.saveapp.util

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LogUtil {
    private var _logFilePath = ""

    private fun log(type: String, className: String, methodName: String, args: List<String>) {
        if (_logFilePath.isEmpty() || args.isEmpty()) {
            return
        }

        try {
            val timeStamp = LocalDateTime
                .now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val formattedType = type.padEnd(7, ' ').substring(0, 7)
            val formattedClassName = className.padEnd(20, ' ').substring(0, 20)
            val formattedMethodName = methodName.padEnd(20, ' ').substring(0, 20)
            FileWriter(_logFilePath, true).use {
                it.write("$timeStamp|$formattedType|$formattedClassName|$formattedMethodName|${args[0]}\n")
                for (i in 1..<args.size) {
                    it.write(args[i])
                }
                it.flush()
            }
        } catch (_: IOException) {
        }
    }

    fun setLogFilePath(path: String) {
        _logFilePath = "$path/saveapp.log"
    }

    fun logInfo(className: String, methodName: String, message: String) {
        log("Info", className, methodName, listOf(message))
    }

    fun logException(e: Throwable, className: String, methodName: String, brief: Boolean = true) {
        val args = mutableListOf(e.message ?: "")
        if (!brief) {
            args.add(e.stackTraceToString())
        }
        log(if (brief) "Warning" else "Error", className, methodName, args)
    }

    fun exportLogTo(outStream: FileOutputStream?) {
        if (outStream == null) {
            return
        }

        try {
            val writer = outStream.bufferedWriter()
            FileInputStream(_logFilePath).use { input ->
                writer.write(input.reader().readText())
            }
            writer.flush()
            outStream.flush()
            outStream.close()
        } catch (_: IOException) {
        }
    }

    fun clearLogs() {
        try {
            FileWriter(_logFilePath, false).use {
                it.write("")
                it.flush()
            }
        } catch (_: IOException) {
        }
    }
}
