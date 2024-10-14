package com.devforce.inventory.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.devforce.inventory.config.DevForceConfig
import com.devforce.inventory.models.UpdateStatus
import com.devforce.inventory.utils.LocaleSwitcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.File
import java.io.FileOutputStream

@SuppressLint("SetWorldReadable")
@Suppress("DEPRECATION")
suspend fun downloadFileAndInstall(requireContext: Context): Any? {
    return try {
        withContext(Dispatchers.IO) {
            val protocol = DevForceConfig.Update.PROTOCOL
            val ip = DevForceConfig.Update.IP_ADDRESS
            val port = DevForceConfig.Update.PORT

            val projectID = DevForceConfig.Basics.PROJECT_ID

            val retrofit = Retrofit.Builder()
                .baseUrl("$protocol://$ip:$port")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create<IApiService>()
            val response = apiService.downloadFile(projectID)

            if (response.isSuccessful) {
                val apkBytes = response.body()?.byteStream()?.readBytes()

                if (apkBytes != null) {
                    val apkFile = File(requireContext.getExternalFilesDir(null), "app-release.apk")
                    val outputStream = FileOutputStream(apkFile)
                    outputStream.write(apkBytes)
                    outputStream.close()

                    if (apkFile.exists()) {
                        apkFile.setReadable(true, false)
                        val apkUri = FileProvider.getUriForFile(requireContext, "${requireContext.packageName}.fileprovider", apkFile)

                        Log.d("URL", apkUri.toString())
                        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
                        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                        requireContext.startActivity(intent)

                        ""
                    } else {
                        Log.d("FileNotFound", "APK file not found")

                        null
                    }
                } else {
                    Log.d("FAIL", "APK bytes are null")
                    null
                }
            } else {
                Log.d("FAIL", "Response was not success")
                null
            }
        }
    } catch (e: Exception) {
        Log.d("FAIL", "$e")
        null
    }
}


suspend fun fetchUpdate(requireContext: Context): UpdateStatus? {
    return try {
        withContext(Dispatchers.IO) {
            val protocol = DevForceConfig.Update.PROTOCOL
            val ip = DevForceConfig.Update.IP_ADDRESS
            val port = DevForceConfig.Update.PORT

            val clientVersion = DevForceConfig.Basics.VERSION
            val projectID = DevForceConfig.Basics.PROJECT_ID
            val currentLanguage = LocaleSwitcher.getCurrentLanguage(requireContext)

            val retrofit = Retrofit.Builder()
                .baseUrl("$protocol://$ip:$port")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create<IApiService>()
            val response = apiService.checkUpdate(clientVersion, projectID, currentLanguage)

            if (response.isSuccessful) {
                val updateStatus = response.body()

                if (updateStatus != null) {
                    val newVersion = updateStatus.newVersion
                    val updateLog = updateStatus.updateLog

                    if(updateLog == "") {
                        null
                    } else {
                        UpdateStatus(newVersion, updateLog)
                    }
                } else {
                    null
                }
            } else {
                Log.d("FAIL", "Response was not success")
                null
            }
        }
    } catch (e: Exception) {
        Log.d("FAIL", "$e")
        null
    }
}