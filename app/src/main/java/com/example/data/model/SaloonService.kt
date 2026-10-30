package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saloon_services")
data class SaloonService(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double,
    val iconEmoji: String = "✂️",
    val isPopular: Boolean = true,
    val sortOrder: Int = 0
)
