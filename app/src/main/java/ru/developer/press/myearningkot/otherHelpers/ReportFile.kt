package ru.developer.press.myearningkot.otherHelpers

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


/*
 * Copyright 2011 Oleg Elifantiev
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/*
 * Copyright 2011 Oleg Elifantiev
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */



/**
 * Simple error reporting facility.
 * Saves stacktraces and exception information to external storage (if mounted and writable)
 * Files are saved to folder Android/data/your.package.name/files/stacktrace-dd-MM-YY.txt
 *
 * To apply error reporting simply do the following
 * RoboErrorReporter.bindReporter(yourContext);
 */
object RoboErrorReporter {
    /**
     * Apply error reporting to a specified application context
     * @param context context for which errors are reported (used to get package name)
     */
    fun bindReporter(context: Context?) {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler.inContext(
                context!!
            )
        )
    }

    fun reportError(context: Context?, error: Throwable?) {
        ExceptionHandler.reportOnlyHandler(context!!)
            .uncaughtException(Thread.currentThread(), error!!)
    }
}


internal class ExceptionHandler private constructor(
    context: Context,
    chained: Boolean
) :
    Thread.UncaughtExceptionHandler {
    @SuppressLint("SimpleDateFormat")
    private val formatter: DateFormat = SimpleDateFormat("dd.MM.yy HH:mm")
    @SuppressLint("SimpleDateFormat")
    private val fileFormatter: DateFormat = SimpleDateFormat("dd-MM-yy")
    private var versionName = "0"
    private var versionCode = 0
    private val stacktraceDir: String
    private var previousHandler: Thread.UncaughtExceptionHandler? = null
    override fun uncaughtException(
        thread: Thread,
        exception: Throwable
    ) {
        val state = Environment.getExternalStorageState()
        val dumpDate = Date(System.currentTimeMillis())
        if (Environment.MEDIA_MOUNTED == state) {
            val reportBuilder = StringBuilder()
            reportBuilder
                .append("\n\n\n")
                .append(formatter.format(dumpDate)).append("\n")
                .append(String.format("Version: %s (%d)\n", versionName, versionCode))
                .append(thread.toString()).append("\n")
            processThrowable(exception, reportBuilder)
            val sd = Environment.getExternalStorageDirectory()
            val stacktrace = File(
                sd.path + stacktraceDir, String.format(
                    "stacktrace-%s.txt",
                    fileFormatter.format(dumpDate)
                )
            )
            val dumpdir = stacktrace.parentFile
            val dirReady = dumpdir.isDirectory || dumpdir.mkdirs()
            if (dirReady) {
                var writer: FileWriter? = null
                try {
                    writer = FileWriter(stacktrace, true)
                    writer.write(reportBuilder.toString())
                } catch (e: IOException) { // ignore
                } finally {
                    try {
                        writer?.close()
                    } catch (e: IOException) { // ignore
                    }
                }
            }
        }
        if (previousHandler != null) previousHandler!!.uncaughtException(thread, exception)
    }

    private fun processThrowable(
        exception: Throwable?,
        builder: StringBuilder
    ) {
        if (exception == null) return
        val stackTraceElements =
            exception.stackTrace
        builder
            .append("Exception: ").append(exception.javaClass.name).append("\n")
            .append("Message: ").append(exception.message).append("\nStacktrace:\n")
        for (element in stackTraceElements) {
            builder.append("\t").append(element.toString()).append("\n")
        }
        processThrowable(exception.cause, builder)
    }

    companion object {
        fun inContext(context: Context): ExceptionHandler {
            return ExceptionHandler(context, true)
        }

        fun reportOnlyHandler(context: Context): ExceptionHandler {
            return ExceptionHandler(context, false)
        }
    }

    init {
        val mPackManager = context.packageManager
        val mPackInfo: PackageInfo
        try {
            mPackInfo = mPackManager.getPackageInfo(context.packageName, 0)
            versionName = mPackInfo.versionName
            versionCode = mPackInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) { // ignore
        }
        previousHandler = if (chained) Thread.getDefaultUncaughtExceptionHandler() else null
        stacktraceDir = String.format("/Android/data/%s/files/", context.packageName)
    }
}

class CustomExceptionHandler(private val localPath: String?) :
    Thread.UncaughtExceptionHandler {
    private val defaultUEH
            = Thread.getDefaultUncaughtExceptionHandler()
    override fun uncaughtException(t: Thread?, e: Throwable) {
        val timestamp: String = Calendar.getInstance().timeInMillis.toString()
        val result: Writer = StringWriter()
        val printWriter = PrintWriter(result)
        e.printStackTrace(printWriter)
        val stacktrace: String = result.toString()
        printWriter.close()
        val filename = "$timestamp.stacktrace"
        if (localPath != null) {
            writeToFile(stacktrace, filename)
        }

        defaultUEH?.uncaughtException(t!!, e)
    }

    private fun writeToFile(stacktrace: String, filename: String) {
        try {
            val bos = BufferedWriter(
                FileWriter(
                    "$localPath/$filename"
                )
            )
            bos.write(stacktrace)
            bos.flush()
            bos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}