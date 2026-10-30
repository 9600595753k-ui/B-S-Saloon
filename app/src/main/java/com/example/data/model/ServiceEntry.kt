package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_entries")
data class ServiceEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workerId: Long = 0,
    val workerName: String,
    val date: String, // Format "dd.MM.yyyy" e.g., "01.10.2026"
    val time: String = "9:00 am", // e.g. "9:11 am"
    val jobNumber: Int, // 1, 2, 3...
    val serviceName: String = "Service Work",
    val clientName: String = "General Client",
    val amount: Double = 0.0,
    val paymentMethod: String = "GPAY", // "GPAY" or "CASH"
    val isPaid: Boolean = true,
    val notes: String = "",
    val invoiceNumber: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
