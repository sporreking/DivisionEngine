package io

import java.text.SimpleDateFormat
import java.util.Date

enum class LogLevel {
    DEBUG, INFO, WARNING, ERROR,
}

/** Used to log things to the console. */
class Logger {
    companion object {
        /** The current logger level. */
        var level: LogLevel = LogLevel.INFO

        /** The date and time format. */
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

        /** Logs on debug level. */
        fun debug(vararg o: Any?) = this.getLevelString(LogLevel.DEBUG, *o)?.let(::println)

        /** Logs on info level. */
        fun info(vararg o: Any?) = this.getLevelString(LogLevel.INFO, *o)?.let(::println)

        /** Logs on warn level. */
        fun warn(vararg o: Any?) = this.getLevelString(LogLevel.WARNING, *o)?.let(::println)

        /** Logs on error level. */
        fun error(vararg o: Any?) = this.getLevelString(LogLevel.ERROR, *o)?.let(::println)
    }
}