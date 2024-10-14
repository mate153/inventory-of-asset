package com.devforce.inventory.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import java.io.IOException
import java.io.File
import android.os.Environment
import android.os.Handler
import androidx.annotation.RequiresApi
import com.devforce.inventory.R
import com.devforce.inventory.config.DevForceConfig
import com.devforce.inventory.utils.ShowDialog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.concurrent.thread

@Suppress("DEPRECATION")
class ExportCsv(private var rowData: List<List<String>>, private val context: Context) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun startExportProcess(callback: (Boolean) -> Unit) {
        val showDialog = ShowDialog(context)
        val handler = Handler()

        showDialog.loading(context.getString(R.string.dialog_export_text))
        thread {
            try {
                val exportFile = getExportFileName()

                val documentsDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                val file = File(documentsDirectory, exportFile)

                rowData = fillCsvWithDate(rowData)

                file.bufferedWriter(charset = Charsets.UTF_8).use { bufferedWriter ->
                    rowData.forEach { row ->
                        val formattedRow = DevForceConfig.Template.EXPORT_TEMPLATE.map { index -> row.getOrNull(index) ?: "" }
                        bufferedWriter.write(formattedRow.joinToString(separator = ";"))
                        bufferedWriter.newLine()
                    }
                }

                deleteJson(context)

                handler.postDelayed({
                    showDialog.dismissLoading("export") { success ->
                        if(success) {
                            callback(true)
                        }
                    }
                }, DevForceConfig.Export.EXPORT_PROCESS_DELAY)

                println("Export process completed successfully.")
            } catch (e: IOException) {
                handler.postDelayed({
                    showDialog.dismissLoading("export") { result ->
                        if(result) {
                            callback(true)
                        }
                    }
                }, DevForceConfig.Export.EXPORT_PROCESS_DELAY)

                println("An error occurred during export process: ${e.message}")
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun fillCsvWithDate(rowData: List<List<String>>): ArrayList<List<String>> {
        val updatedRows = ArrayList(rowData)

        for (row in updatedRows) {
            var updatedRow = row
            updatedRow = updatedRow.toMutableList().apply {
                this[size - 2] = SimpleDateFormat("yyyyMMdd").format(Date())
            }.toList()
            val index = rowData.indexOf(row)
            updatedRows[index] = updatedRow
        }

        return updatedRows
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getExportFileName(): String {
        return if (DevForceConfig.Export.DATE_IN_FILE_NAME) {
            val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmSS")
            val current = LocalDateTime.now().format(formatter)

            "${DevForceConfig.Export.OUTPUT_REGULAR_FILE_NAME}_$current.${DevForceConfig.Export.OUTPUT_FILE_EXTENSION}"
        } else {
            "${DevForceConfig.Export.OUTPUT_REGULAR_FILE_NAME}.${DevForceConfig.Export.OUTPUT_FILE_EXTENSION}"
        }
    }
}