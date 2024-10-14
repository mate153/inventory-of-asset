package com.devforce.inventory.utils

import android.content.Context
import android.os.Environment
import com.devforce.inventory.config.DevForceConfig
import org.json.JSONObject
import java.io.File

fun checkJsonFile(requireContext: Context, jsonFileName: String): String? {
    val documentsDirectory = requireContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val saveFolder = File(documentsDirectory, "save")
    val jsonFile = jsonFileName + "." + DevForceConfig.Save.Regular.JSON_FILE_EXTENSION
    val filePath = File(saveFolder, jsonFile)

    return if (saveFolder.isDirectory) {
        val file = File(saveFolder, jsonFile)
        if (file.exists()) {
            val jsonString = filePath.readText()
            val jsonObject = JSONObject(jsonString)
            val metadataObject = jsonObject.getJSONObject("metadata")

            println("$jsonFile found in ${saveFolder.absolutePath}")

            return metadataObject.getString("lastSave")
        } else {
            println("$jsonFile not found in ${saveFolder.absolutePath}")
            null
        }
    } else {
        println("Save folder does not exist.")
        null
    }
}