package com.example.quickpark.data.model

data class Parking(
    val id: String,              // map backend _id to id
    val title: String,
    val address: String?,
    val lat: Double,
    val lng: Double,
    val price: Int,
    val distanceMeters: Int?,
    val availableInMinutes: Int?,
    val seller: String?,
    val imageUrl: String?
)
