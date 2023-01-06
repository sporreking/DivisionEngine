package io

import java.text.SimpleDateFormat
import java.util.Date

enum class LogLevel {
    DEBUG, INFO, WARNING, ERROR,
}

class Logger {
    companion object {
        var level: LogLevel = LogLevel.INFO

        val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

        private fun getSimpleMessage(vararg o: Any?) = o.joinToString()

        private fun getLevelString(level: LogLevel, vararg o: Any?): String? {

            // Check debug level
            if (level.ordinal < this.level.ordinal)
                return null

            // Extract caller
            val ste = Thread.currentThread().stackTrace[3]
            val scope = ste.className + if (level == LogLevel.DEBUG) "::${ste.methodName}" else ""

            // Derive message
            return "${level.name}: ${DATE_FORMAT.format(Date())} [$scope] ${getSimpleMessage(*o)}"
        }

        fun debug(vararg o: Any?) = this.getLevelString(LogLevel.DEBUG, *o)?.let(::println)
        fun info(vararg o: Any?) = this.getLevelString(LogLevel.INFO, *o)?.let(::println)
        fun warn(vararg o: Any?) = this.getLevelString(LogLevel.WARNING, *o)?.let(::println)
        fun error(vararg o: Any?) = this.getLevelString(LogLevel.ERROR, *o)?.let(::println)
    }
}