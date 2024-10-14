package com.devforce.inventory.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Environment
import android.util.Log
import com.devforce.inventory.config.DevForceConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Date

fun jsonController(requireContext: Context, csvContent: String, isBuildInventory: Boolean): Boolean {
    val documentsDirectory = requireContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val saveFolder = File(documentsDirectory, "save")

    val jsonFile = if (isBuildInventory) {
        DevForceConfig.Save.Build.JSON_FILE_NAME + "." + DevForceConfig.Save.Build.JSON_FILE_EXTENSION
    } else {
        DevForceConfig.Save.Regular.JSON_FILE_NAME + "." + DevForceConfig.Save.Regular.JSON_FILE_EXTENSION
    }

    if (!saveFolder.exists()) {
        val folderCreated = saveFolder.mkdirs()
        if (folderCreated) {
            println("Folder 'save' created in ${documentsDirectory?.absolutePath}")
        } else {
            println("Failed to create folder 'save'")
            return false
        }
    }

    val file = File(saveFolder, jsonFile)
    if (!file.exists()) {
        try {
            val fileCreated = file.createNewFile()
            return if (fileCreated) {
                println("$jsonFile created in ${saveFolder.absolutePath}")
                if (isBuildInventory) {
                    createEmptyJson(file)
                } else {
                    fillJsonWithData(csvContent, file)
                }
            } else {
                println("Failed to create $jsonFile")
                false
            }
        } catch (e: IOException) {
            println("Error creating $jsonFile: ${e.message}")
            return false
        }
    } else {
        println("$jsonFile already exists in ${saveFolder.absolutePath}")
        return if (isBuildInventory) {
            createEmptyJson(file)
        } else {
            fillJsonWithData(csvContent, file)
        }
    }
}

fun jsonReader(requireContext: Context): MutableList<List<String>> {
    val documentsDirectory = requireContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val saveFolder = File(documentsDirectory, "save")
    val jsonFile = DevForceConfig.Save.Build.JSON_FILE_NAME + "." + DevForceConfig.Save.Build.JSON_FILE_EXTENSION
    val filePath = File(saveFolder, jsonFile)

    val jsonString = filePath.readText()
    val jsonObject = JSONObject(jsonString)

    val dataArray = jsonObject.getJSONArray("data")
    val data = mutableListOf<List<String>>()

    for (i in 0 until dataArray.length()) {
        val itemArray = dataArray.getJSONArray(i)
        if(itemArray.length() > 2) {
            val itemList = mutableListOf<String>()
            for (j in 0 until itemArray.length()) {
                itemList.add(itemArray.getString(j))
            }
            data.add(itemList)
        }
    }

    return data
}

fun deleteJson(requireContext: Context) {
    val documentsDirectory = requireContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val saveFolder = File(documentsDirectory, "save")
    val jsonFile = DevForceConfig.Save.Regular.JSON_FILE_NAME + "." + DevForceConfig.Save.Regular.JSON_FILE_EXTENSION
    val filePath = File(saveFolder, jsonFile)

    if (filePath.exists()) {
        val deleted = filePath.delete()
        if (deleted) {
            Log.d("DeleteJson", "JSON file successfully deleted.")
        } else {
            Log.d("DeleteJson", "Failed to delete the JSON file.")
        }
    } else {
        Log.d("DeleteJson", "The JSON file does not exist.")
    }
}

@SuppressLint("SimpleDateFormat")
fun saveToJson(requireContext: Context, updatedRow: List<String>, rowIndex: Int) {
    val documentsDirectory = requireContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val saveFolder = File(documentsDirectory, "save")
    val jsonFile = DevForceConfig.Save.Regular.JSON_FILE_NAME + "." + DevForceConfig.Save.Regular.JSON_FILE_EXTENSION
    val filePath = File(saveFolder, jsonFile)

    try {
        val jsonString = filePath.readText()
        val jsonObject = JSONObject(jsonString)

        val data = jsonObject.optJSONArray("data")
        if (data != null && rowIndex >= 0 && rowIndex < data.length()) {
            val jsonArray = JSONArray(updatedRow)
            data.put(rowIndex, jsonArray)
        }

        val metadata = jsonObject.optJSONObject("metadata")
        if (metadata != null) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            metadata.put("lastSave", currentDate)
        }

        filePath.writeText(jsonObject.toString(4))
    } catch(e: IOException) {
        Log.e("error", e.toString())
    }
}

@SuppressLint("SimpleDateFormat")
fun updateJsonRow(requireContext: Context, updatedRow: List<String>, rowIndex: Int) {
    val documentsDirectory = requireContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val saveFolder = File(documentsDirectory, "save")
    val jsonFile = DevForceConfig.Save.Regular.JSON_FILE_NAME + "." + DevForceConfig.Save.Regular.JSON_FILE_EXTENSION
    val filePath = File(saveFolder, jsonFile)

    try {
        val jsonString = filePath.readText()
        val jsonObject = JSONObject(jsonString)

        val data = jsonObject.optJSONArray("data")
        if (data != null && rowIndex >= 0 && rowIndex < data.length()) {
            val jsonArray = JSONArray(updatedRow)
            data.put(rowIndex, jsonArray)
        }

        val metadata = jsonObject.optJSONObject("metadata")
        if (metadata != null) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            metadata.put("lastSave", currentDate)
        }

        filePath.writeText(jsonObject.toString(4))
    } catch(e: IOException) {
        Log.e("error", e.toString())
    }
}

@SuppressLint("SimpleDateFormat")
fun createJsonRow(requireContext: Context, newRow: List<String>) {
    val documentsDirectory = requireContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val saveFolder = File(documentsDirectory, "save")
    val jsonFile = DevForceConfig.Save.Regular.JSON_FILE_NAME + "." + DevForceConfig.Save.Regular.JSON_FILE_EXTENSION
    val filePath = File(saveFolder, jsonFile)

    try {
        val jsonString = filePath.readText()
        val jsonObject = JSONObject(jsonString)

        val data = jsonObject.optJSONArray("data")
        if (data != null) {
            val jsonArray = JSONArray(newRow)
            data.put(jsonArray)
        }

        val metadata = jsonObject.optJSONObject("metadata")
        if (metadata != null) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            metadata.put("lastSave", currentDate)
        }

        filePath.writeText(jsonObject.toString(4))
    } catch(e: IOException) {
        Log.e("error", e.toString())
    }
}

@SuppressLint("SimpleDateFormat")
private fun fillJsonWithData(csvContent: String, file: File): Boolean {
    val csvRows = csvContent.lines()
    val csvData = csvRows.map { it.split(";") }

    Log.d("JsonController", "CSV Data: $csvData")

    val jsonArray = JSONArray()
    for (row in csvData) {
        jsonArray.put(JSONArray(row))
    }

    val jsonObject = JSONObject()
    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
    jsonObject.put("data", jsonArray)
    jsonObject.put("metadata", JSONObject(mapOf(
        "lastSave" to currentDate
    )))

    return try {
        FileWriter(file).use { writer ->
            writer.write(jsonObject.toString(4))
        }
        println("JSON data written to ${file.absolutePath}")
        Log.d("JsonController", "JSON data: ${jsonObject.toString(4)}")
        true
    } catch (e: Exception) {
        println("Error writing JSON data to file: ${e.message}")
        false
    }
}

@SuppressLint("SimpleDateFormat")
private fun createEmptyJson(file: File): Boolean {
    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
    val jsonObject = JSONObject()
    jsonObject.put("data", JSONArray())
    jsonObject.put("metadata", JSONObject(mapOf(
        "lastSave" to currentDate
    )))

    return try {
        FileWriter(file).use { writer ->
            writer.write(jsonObject.toString(4))
        }
        println("Empty JSON created at ${file.absolutePath}")
        true
    } catch (e: Exception) {
        println("Error creating empty JSON: ${e.message}")
        false
    }
}
