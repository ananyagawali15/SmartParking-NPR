package com.example.smartparking

data class SlotResponse(
    val status: String,
    val vehicle: String?,
    val end_time: String?
)