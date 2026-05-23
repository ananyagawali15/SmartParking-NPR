package com.example.smartparking

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.GET


interface ApiService {

    @Multipart
    @POST("detect")
    suspend fun detectPlate(
        @Part image: MultipartBody.Part
    ): Response<PlateResponse>

    // 🅿️ GET ALL SLOTS (NEW)
    @GET("slots")
    suspend fun getSlots(): Map<String, SlotResponse>

    @POST("book")
    suspend fun bookSlot(
        @Body request: BookRequest
    ): Response<BookResponse>

    @POST("release")
    suspend fun releaseSlot(
        @Body request: ReleaseRequest
    ): Response<Map<String, String>>
}