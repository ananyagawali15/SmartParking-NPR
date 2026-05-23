package com.example.smartparking


import com.google.gson.annotations.SerializedName

data class PlateResponse(
    @SerializedName("plate_number")
    val plateNumber: String
)
data class BookRequest(
    val slot: String,
    val vehicle: String
)

data class BookResponse(
    val message: String,
    val end_time: String
)