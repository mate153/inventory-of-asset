package com.devforce.inventory.service

import com.devforce.inventory.models.UpdateStatus
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface IApiService {
    @GET("/check-update")
    suspend fun checkUpdate(
        @Query("clientVersion") clientVersion: String,
        @Query("projectID") projectID: String,
        @Query("currentLanguage") currentLanguage: String
    ): Response<UpdateStatus?>

    @GET("/download-file")
    suspend fun downloadFile(
        @Query("projectID") projectID: String,
    ): Response<ResponseBody>
}