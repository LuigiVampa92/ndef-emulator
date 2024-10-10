package com.luigivampa92.ndefemulation

import android.util.Log

internal object Logger {

    internal fun v(message: String?) {
        logMessage(message, Log.VERBOSE)
    }

    internal fun d(message: String?) {
        logMessage(message, Log.DEBUG)
    }

    internal fun d(message: String?, exception: Throwable) {
        logMessageWithException(message, Log.DEBUG, exception)
    }

    internal fun i(message: String?) {
        logMessage(message, Log.INFO)
    }

    internal fun w(message: String?) {
        logMessage(message, Log.WARN)
    }

    internal fun e(message: String?) {
        logMessage(message, Log.ERROR)
    }

    internal fun e(message: String?, exception: Throwable) {
        logMessageWithException(message, Log.ERROR, exception)
    }

    private fun logMessage(message: String?, logLevel: Int) {
        if (logLevel >= BuildConfig.LOG_LEVEL) {
            printLogMessage(message)
        }
    }

    private fun logMessageWithException(message: String?, logLevel: Int, exception: Throwable) {
        if (logLevel >= BuildConfig.LOG_LEVEL) {
            printLogMessageWithException(message, logLevel, exception)
        }
    }

    private fun printLogMessage(message: String?) {
        if (BuildConfig.LOGS_ENABLED) {
            if (!message.isNullOrBlank()) {
                Log.d(BuildConfig.LOG_TAG, message)
            }
        }
    }

    private fun printLogMessageWithException(message: String?, logLevel: Int, exception: Throwable) {
        if (BuildConfig.LOGS_ENABLED) {
            if (!message.isNullOrBlank()) {
                if (logLevel == Log.ERROR) {
                    Log.e(BuildConfig.LOG_TAG, message, exception)
                } else {
                    Log.d(BuildConfig.LOG_TAG, message, exception)
                }
            }
        }
    }
}
