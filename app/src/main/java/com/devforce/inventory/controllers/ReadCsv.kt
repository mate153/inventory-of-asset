package com.devforce.inventory.controllers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.devforce.inventory.config.DevForceConfig
import com.devforce.inventory.config.DevForceSave
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

class ReadCsv(private val requireContext: Context) {
    fun checkFileExtension(selectedFileUri: Uri): Boolean {
        val documentFile = DocumentFile.fromSingleUri(requireContext, selectedFileUri)
        val fileExtension = documentFile?.name?.substringAfterLast(".", "")
        return fileExtension.equals(DevForceConfig.Basics.ACCEPTED_IMPORT_EXTENSION, ignoreCase = true)
    }

    fun readCSV(inputStream: InputStream): String {
        val result = StringBuilder()
        try {
            BufferedReader(InputStreamReader(inputStream, Charset.forName("ISO-8859-1"))).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val lineWithPlusData = "$line;;S;${DevForceSave.Auth.LOGGED_IN_AS};;false"
                    result.append(lineWithPlusData).append("\n")
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error occurred while opening file: ${e.message}")
        }
        return result.toString()
    }
}