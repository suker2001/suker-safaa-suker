package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscribers")
data class Subscriber(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uniqueCode: String, // e.g. CLI-1001
    val name: String,
    val phone: String,
    val address: String,
    val notes: String,
    val avatarEmoji: String = "👤", // Emoji used as a lightweight, beautiful avatar
    val status: String = "ACTIVE", // ACTIVE or SUSPENDED
    val totalDebt: Double = 0.0,
    val totalPaid: Double = 0.0,
    val nextDueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val remainingDebt: Double
        get() = totalDebt - totalPaid
}

@Entity(tableName = "transactions")
data class FinancialTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subscriberId: Int,
    val type: String, // "DEBT" or "PAYMENT"
    val amount: Double,
    val notes: String,
    val timestamp: Long = System.currentTimeMillis(),
    val createdBy: String, // Username of creator
    val createdByRole: String, // Role of creator
    val isModified: Boolean = false,
    val modificationHistory: String = "" // List of modifications formatted as text
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val username: String,
    val role: String,
    val action: String
)

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val fullName: String,
    val role: String, // "ADMIN" or "EMPLOYEE"
    val pinCode: String, // 4-digit PIN e.g. "1111"
    val isEnabled: Boolean = true
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val isPinEnabled: Boolean = false,
    val isWhatsAppEnabled: Boolean = false,
    val whatsAppApiType: String = "MANUAL", // "MANUAL" or "ULTRAMSG" or "TWILIO"
    val whatsAppInstanceId: String = "",
    val whatsAppToken: String = "",
    val whatsAppSid: String = "", // Twilio Sid
    val whatsAppTemplateDebt: String = "مرحباً {name}\n\nتمت إضافة دين جديد:\n\nالمبلغ المضاف: {amount}\nالمبلغ المتبقي: {remaining}\n\nتاريخ العملية: {date}\n\nشكراً لتعاونكم.",
    val whatsAppTemplatePayment: String = "مرحباً {name}\n\nتم استلام دفعة جديدة:\n\nالمبلغ المسدد: {amount}\nالمبلغ المتبقي: {remaining}\n\nتاريخ العملية: {date}\n\nشكراً لتعاونكم.",
    val isDarkMode: Boolean = false,
    
    // Notification Settings
    val notifyPayments: Boolean = true,
    val notifyDueDates: Boolean = true,
    val notifyWhatsApp: Boolean = true,
    val notifySecurity: Boolean = true,
    val notifyEmployeeActivities: Boolean = true,
    val notifySystem: Boolean = true,
    val pushNotificationsEnabled: Boolean = true
)

enum class SyncStatus {
    PENDING, SYNCED, FAILED
}

@Entity(tableName = "sync_logs")
data class SyncLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val operationType: String, // e.g., "ADD_SUBSCRIBER", "ADD_DEBT", "UPDATE_SUBSCRIBER"
    val entityId: Int, // The local ID of the affected entity
    val payload: String, // JSON payload representing the changes or the entity
    val timestamp: Long = System.currentTimeMillis(),
    val status: SyncStatus = SyncStatus.PENDING,
    val errorMessage: String = ""
)

@Entity(tableName = "pending_whatsapp_messages")
data class PendingWhatsAppMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subscriberName: String,
    val phone: String,
    val message: String,
    val transactionId: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: SyncStatus = SyncStatus.PENDING,
    val retryCount: Int = 0
)
