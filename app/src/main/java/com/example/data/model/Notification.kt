package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val type: String, // e.g., PAYMENT, DUE_DATE, NEW_SUBSCRIBER, SECURITY, REPORT, SYSTEM
    val priority: String, // CRITICAL, HIGH, MEDIUM, LOW
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isArchived: Boolean = false,
    val relatedSubscriberId: Int? = null,
    val relatedTransactionId: Int? = null
)
