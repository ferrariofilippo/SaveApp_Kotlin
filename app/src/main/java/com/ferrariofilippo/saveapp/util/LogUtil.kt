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

    fun setLogFilePath(path: String) {
        _logFilePath = "$path/saveapp.log"
    }

    fun logException(e: Throwable, className: String, methodName: String) {
        if (_logFilePath.isEmpty()) {
            return
        }

        try {
            val timeStamp = LocalDateTime
                .now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val formattedClassName = className.padEnd(30, ' ').substring(0, 20)
            val formattedMethodName = methodName.padEnd(30, ' ').substring(0, 20)
            FileWriter(_logFilePath, true).use {
                it.write("$timeStamp|$formattedClassName|$formattedMethodName|${e.message}\n")
                it.write(e.stackTraceToString())
                it.flush()
            }
        } catch (_: IOException) {
        }
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
