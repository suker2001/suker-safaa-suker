package com.example.data.repository

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(private val db: AppDatabase) {

    private val firestoreService = FirestoreService()
    private val subscriberDao = db.subscriberDao()
    private val transactionDao = db.transactionDao()
    private val activityLogDao = db.activityLogDao()
    private val userAccountDao = db.userAccountDao()
    private val appSettingsDao = db.appSettingsDao()
    private val syncLogDao = db.syncLogDao()
    private val pendingWhatsAppMessageDao = db.pendingWhatsAppMessageDao()
    private val notificationDao = db.notificationDao()

    // --- Notifications ---
    val allActiveNotifications = notificationDao.getAllActiveNotifications()
    val allNotifications = notificationDao.getAllNotifications()
    val unreadNotificationCount = notificationDao.getUnreadCount()

    suspend fun addNotification(notification: com.example.data.model.Notification) {
        notificationDao.insert(notification)
    }

    suspend fun updateNotification(notification: com.example.data.model.Notification) {
        notificationDao.update(notification)
    }

    suspend fun deleteNotification(notification: com.example.data.model.Notification) {
        notificationDao.delete(notification)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun archiveAllNotifications() {
        notificationDao.archiveAll()
    }

    // Moshi for Backup/Restore and Sync
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // --- Sync & Offline Mode ---
    val allSyncLogs: Flow<List<SyncLog>> = syncLogDao.getAllSyncLogs()

    suspend fun logSyncOperation(type: String, entityId: Int, payloadObj: Any) {
        try {
            val json = moshi.adapter(Any::class.java).toJson(payloadObj)
            syncLogDao.insertSyncLog(
                SyncLog(
                    operationType = type,
                    entityId = entityId,
                    payload = json
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getPendingSyncLogs() = syncLogDao.getSyncLogsByStatus(SyncStatus.PENDING)

    suspend fun markSyncLogAs(logId: Int, status: SyncStatus, errorMessage: String = "") {
        // Find and update
        val logs = syncLogDao.getAllSyncLogs().firstOrNull() ?: emptyList()
        val log = logs.find { it.id == logId }
        if (log != null) {
            syncLogDao.updateSyncLog(log.copy(status = status, errorMessage = errorMessage))
        }
    }
    
    suspend fun queueWhatsAppMessage(subscriberName: String, phone: String, message: String, transactionId: Int) {
        pendingWhatsAppMessageDao.insertMessage(
            PendingWhatsAppMessage(
                subscriberName = subscriberName,
                phone = phone,
                message = message,
                transactionId = transactionId
            )
        )
    }

    suspend fun getPendingWhatsAppMessages() = pendingWhatsAppMessageDao.getPendingMessages()
    fun getAllWhatsAppMessages() = pendingWhatsAppMessageDao.getAllMessages()

    suspend fun markWhatsAppMessageSynced(msgId: Int) {
        val msgs = pendingWhatsAppMessageDao.getPendingMessages()
        val msg = msgs.find { it.id == msgId }
        if (msg != null) {
            pendingWhatsAppMessageDao.updateMessage(msg.copy(status = SyncStatus.SYNCED))
        }
    }

    // Seeding default configuration and accounts
    suspend fun seedDatabaseIfNeeded() {
        // 1. Seed Settings
        val currentSettings = appSettingsDao.getSettingsOneShot()
        if (currentSettings == null) {
            appSettingsDao.insertOrUpdateSettings(
                AppSettings(
                    id = 1,
                    isPinEnabled = true,
                    isWhatsAppEnabled = false,
                    whatsAppApiType = "MANUAL"
                )
            )
        }

        // 2. Seed Users
        val users = userAccountDao.getAllUserAccounts().firstOrNull() ?: emptyList()
        if (users.isEmpty()) {
            userAccountDao.insertUserAccount(
                UserAccount(
                    username = "admin",
                    fullName = "المدير العام (أدمن)",
                    role = "ADMIN",
                    pinCode = "1111",
                    isEnabled = true
                )
            )
            userAccountDao.insertUserAccount(
                UserAccount(
                    username = "employee",
                    fullName = "الموظف المالي",
                    role = "EMPLOYEE",
                    pinCode = "2222",
                    isEnabled = true
                )
            )
            logActivity("system", "SYSTEM", "تم تهيئة قاعدة البيانات وإضافة حسابات مستخدمين افتراضية")
        }
        
        // Ensure DEVELOPER account always exists
        val currentUsers = userAccountDao.getAllUserAccounts().firstOrNull() ?: emptyList()
        val devExists = currentUsers.any { it.role == "DEVELOPER" }
        if (!devExists) {
            userAccountDao.insertUserAccount(
                UserAccount(
                    username = "developer",
                    fullName = "المطور",
                    role = "DEVELOPER",
                    pinCode = "0000",
                    isEnabled = true
                )
            )
            logActivity("system", "SYSTEM", "تم إنشاء حساب المطور التلقائي")
        }
    }

    // --- Activity Logs ---
    val allActivityLogs: Flow<List<ActivityLog>> = activityLogDao.getAllActivityLogs()

    suspend fun logActivity(username: String, role: String, action: String) {
        activityLogDao.insertActivityLog(
            ActivityLog(
                username = username,
                role = role,
                action = action
            )
        )
    }

    suspend fun clearActivityLogs(actor: String, role: String) {
        activityLogDao.clearAllLogs()
        logActivity(actor, role, "تم مسح سجل النشاطات بالكامل")
    }

    // --- Subscribers ---
    val allSubscribers: Flow<List<Subscriber>> = subscriberDao.getAllSubscribers()

    fun searchSubscribers(query: String): Flow<List<Subscriber>> {
        return subscriberDao.searchSubscribers(query)
    }

    suspend fun getSubscriberById(id: Int): Subscriber? {
        return subscriberDao.getSubscriberByIdOneShot(id)
    }

    fun observeSubscriber(id: Int): Flow<Subscriber?> {
        return subscriberDao.getSubscriberById(id)
    }

    suspend fun addSubscriber(
        name: String,
        phone: String,
        address: String,
        notes: String,
        avatarEmoji: String,
        status: String,
        dueDate: Long?,
        actor: String,
        actorRole: String
    ): Long {
        // 1. Insert temporary subscriber to get generated ID
        val tempSub = Subscriber(
            uniqueCode = "TEMP",
            name = name,
            phone = phone,
            address = address,
            notes = notes,
            avatarEmoji = avatarEmoji,
            status = status,
            nextDueDate = dueDate
        )
        val id = subscriberDao.insertSubscriber(tempSub)

        // 2. Update sequential code based on ID
        val uniqueCode = "CLI-${1000 + id}"
        val finalSub = tempSub.copy(id = id.toInt(), uniqueCode = uniqueCode)
        subscriberDao.updateSubscriber(finalSub)
        
        // 3. Sync with Firestore
        try {
            firestoreService.addSubscriber(finalSub)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        logSyncOperation("ADD_SUBSCRIBER", id.toInt(), finalSub)
        logActivity(actor, actorRole, "تم إضافة مشترك جديد: $name (رمز: $uniqueCode)")
        return id
    }

    suspend fun updateSubscriber(
        subscriber: Subscriber,
        actor: String,
        actorRole: String
    ) {
        subscriberDao.updateSubscriber(subscriber)
        try {
            firestoreService.updateSubscriber(subscriber)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        logSyncOperation("UPDATE_SUBSCRIBER", subscriber.id, subscriber)
        logActivity(actor, actorRole, "تم تعديل بيانات المشترك: ${subscriber.name} (${subscriber.uniqueCode})")
    }

    suspend fun deleteSubscriber(
        subscriber: Subscriber,
        actor: String,
        actorRole: String
    ) {
        // Delete all transactions first
        transactionDao.deleteTransactionsBySubscriber(subscriber.id)
        subscriberDao.deleteSubscriber(subscriber)
        logSyncOperation("DELETE_SUBSCRIBER", subscriber.id, subscriber)
        logActivity(actor, actorRole, "تم حذف المشترك وحساباته بالكامل: ${subscriber.name}")
    }

    // --- Transactions ---
    fun observeTransactions(subscriberId: Int): Flow<List<FinancialTransaction>> {
        return transactionDao.getTransactionsForSubscriber(subscriberId)
    }

    val allTransactions: Flow<List<FinancialTransaction>> = transactionDao.getAllTransactions()

    suspend fun addTransaction(
        subscriberId: Int,
        type: String, // "DEBT" or "PAYMENT"
        amount: Double,
        notes: String,
        actor: String,
        actorRole: String
    ): FinancialTransaction? {
        val subscriber = subscriberDao.getSubscriberByIdOneShot(subscriberId) ?: return null

        val transaction = FinancialTransaction(
            subscriberId = subscriberId,
            type = type,
            amount = amount,
            notes = notes,
            createdBy = actor,
            createdByRole = actorRole
        )

        val txId = transactionDao.insertTransaction(transaction)
        val finalTransaction = transaction.copy(id = txId.toInt())
        try {
            firestoreService.addTransaction(finalTransaction)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Update subscriber balances
        val updatedSubscriber = if (type == "DEBT") {
            subscriber.copy(totalDebt = subscriber.totalDebt + amount)
        } else {
            subscriber.copy(totalPaid = subscriber.totalPaid + amount)
        }
        subscriberDao.updateSubscriber(updatedSubscriber)
        try {
            firestoreService.updateSubscriber(updatedSubscriber)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val amountFormatted = com.example.utils.FormatHelper.formatCurrency(amount)
        
        val typeText = if (type == "DEBT") "دين" else "تسديد دفعة"
        val finalTx = transaction.copy(id = txId.toInt())
        logSyncOperation("ADD_TRANSACTION", finalTx.id, finalTx)
        logActivity(actor, actorRole, "تم تسجيل عملية $typeText بقيمة $amountFormatted للمشترك ${subscriber.name}")

        // Queue WhatsApp Message
        val now = System.currentTimeMillis()
        
        val messageText = if (type == "DEBT") {
            """
            مرحباً ${subscriber.name}
            
            تمت إضافة دين جديد:
            
            المبلغ المضاف: ${com.example.utils.FormatHelper.formatCurrency(amount)}
            المبلغ المتبقي: ${com.example.utils.FormatHelper.formatCurrency(updatedSubscriber.remainingDebt)}
            
            تاريخ العملية: ${com.example.utils.FormatHelper.formatDate(now)}
            
            شكراً لتعاونكم.
            """.trimIndent()
        } else {
            """
            مرحباً ${subscriber.name}
            
            تم استلام دفعة جديدة:
            
            المبلغ المسدد: ${com.example.utils.FormatHelper.formatCurrency(amount)}
            المبلغ المتبقي: ${com.example.utils.FormatHelper.formatCurrency(updatedSubscriber.remainingDebt)}
            
            تاريخ العملية: ${com.example.utils.FormatHelper.formatDate(now)}
            
            شكراً لتعاونكم.
            """.trimIndent()
        }
        
        pendingWhatsAppMessageDao.insertMessage(
            PendingWhatsAppMessage(
                subscriberName = subscriber.name,
                phone = subscriber.phone,
                message = messageText,
                transactionId = finalTx.id
            )
        )

        return finalTx
    }

    suspend fun updateTransaction(
        txId: Int,
        newAmount: Double,
        newNotes: String,
        actor: String,
        actorRole: String
    ): Boolean {
        val tx = transactionDao.getTransactionById(txId) ?: return false
        val subscriber = subscriberDao.getSubscriberByIdOneShot(tx.subscriberId) ?: return false

        // 1. Revert old transaction amounts from subscriber balances
        var revertedDebt = subscriber.totalDebt
        var revertedPaid = subscriber.totalPaid

        if (tx.type == "DEBT") {
            revertedDebt -= tx.amount
        } else {
            revertedPaid -= tx.amount
        }

        // 2. Add new transaction amounts to subscriber balances
        val finalDebt = if (tx.type == "DEBT") revertedDebt + newAmount else revertedDebt
        val finalPaid = if (tx.type == "PAYMENT") revertedPaid + newAmount else revertedPaid

        val updatedSubscriber = subscriber.copy(totalDebt = finalDebt, totalPaid = finalPaid)
        subscriberDao.updateSubscriber(updatedSubscriber)

        // 3. Save Modification History
        val dateStr = com.example.utils.FormatHelper.formatDateTime(System.currentTimeMillis())
        val oldAmountFmt = com.example.utils.FormatHelper.formatCurrency(tx.amount)
        val newAmountFmt = com.example.utils.FormatHelper.formatCurrency(newAmount)
        val historyLog = "[تاريخ التعديل: $dateStr | القيمة السابقة: $oldAmountFmt -> الجديدة: $newAmountFmt | المعدل: $actor]\n" + tx.modificationHistory

        val updatedTx = tx.copy(
            amount = newAmount,
            notes = newNotes,
            isModified = true,
            modificationHistory = historyLog
        )
        transactionDao.updateTransaction(updatedTx)

        val typeText = if (tx.type == "DEBT") "دين" else "تسديد دفعة"
        logSyncOperation("UPDATE_TRANSACTION", updatedTx.id, updatedTx)
        logActivity(actor, actorRole, "تعديل عملية $typeText (رقم $txId) للمشترك ${subscriber.name}: بقيمة $newAmountFmt")
        return true
    }

    suspend fun deleteTransaction(
        txId: Int,
        actor: String,
        actorRole: String
    ): Boolean {
        val tx = transactionDao.getTransactionById(txId) ?: return false
        val subscriber = subscriberDao.getSubscriberByIdOneShot(tx.subscriberId) ?: return false

        // Revert totals
        val updatedSubscriber = if (tx.type == "DEBT") {
            subscriber.copy(totalDebt = (subscriber.totalDebt - tx.amount).coerceAtLeast(0.0))
        } else {
            subscriber.copy(totalPaid = (subscriber.totalPaid - tx.amount).coerceAtLeast(0.0))
        }
        subscriberDao.updateSubscriber(updatedSubscriber)

        transactionDao.deleteTransaction(tx)

        val typeText = if (tx.type == "DEBT") "دين" else "تسديد"
        logSyncOperation("DELETE_TRANSACTION", tx.id, tx)
        logActivity(actor, actorRole, "حذف عملية $typeText بقيمة ${tx.amount} للمشترك ${subscriber.name}")
        return true
    }

    // --- Settings ---
    val appSettings: Flow<AppSettings?> = appSettingsDao.getSettings()

    suspend fun getAppSettingsOneShot(): AppSettings? {
        return appSettingsDao.getSettingsOneShot()
    }

    suspend fun updateSettings(settings: AppSettings, actor: String, actorRole: String) {
        appSettingsDao.insertOrUpdateSettings(settings)
        logActivity(actor, actorRole, "تم تحديث إعدادات التطبيق")
    }

    // --- Users accounts management ---
    val allUserAccounts: Flow<List<UserAccount>> = userAccountDao.getAllUserAccounts()

    suspend fun saveUserAccount(account: UserAccount, actor: String, actorRole: String) {
        userAccountDao.insertUserAccount(account)
        logActivity(actor, actorRole, "تم تعديل/إضافة حساب مستخدم: ${account.fullName}")
    }

    suspend fun deleteUserAccount(account: UserAccount, actor: String, actorRole: String) {
        userAccountDao.deleteUserAccount(account)
        logActivity(actor, actorRole, "تم حذف حساب مستخدم: ${account.fullName}")
    }

    suspend fun verifyPinAndLogin(username: String, pin: String): UserAccount? {
        val user = userAccountDao.getUserByUsernameAndPin(username, pin)
        if (user != null) {
            logActivity(user.username, user.role, "تسجيل دخول ناجح إلى النظام")
        }
        return user
    }

    // --- Backup & Restore (JSON serialization) ---
    data class BackupPayload(
        val subscribers: List<Subscriber>,
        val transactions: List<FinancialTransaction>,
        val activityLogs: List<ActivityLog>,
        val userAccounts: List<UserAccount>,
        val settings: AppSettings
    )

    suspend fun exportBackupJson(): String {
        val subs = subscriberDao.getAllSubscribers().firstOrNull() ?: emptyList()
        val txs = transactionDao.getAllTransactions().firstOrNull() ?: emptyList()
        val logs = activityLogDao.getAllActivityLogs().firstOrNull() ?: emptyList()
        val users = userAccountDao.getAllUserAccounts().firstOrNull() ?: emptyList()
        val sets = appSettingsDao.getSettingsOneShot() ?: AppSettings()

        val payload = BackupPayload(subs, txs, logs, users, sets)
        val adapter = moshi.adapter(BackupPayload::class.java)
        return adapter.indent("  ").toJson(payload)
    }

    suspend fun importBackupJson(jsonString: String, actor: String, actorRole: String): Boolean {
        return try {
            val adapter = moshi.adapter(BackupPayload::class.java)
            val payload = adapter.fromJson(jsonString) ?: return false

            // Clear tables and insert imported data
            db.runInTransaction {
                // We run synchronously in transaction
                // Clear everything
                db.clearAllTables()
            }

            // Repopulate
            payload.subscribers.forEach { subscriberDao.insertSubscriber(it) }
            payload.transactions.forEach { transactionDao.insertTransaction(it) }
            payload.activityLogs.forEach { activityLogDao.insertActivityLog(it) }
            payload.userAccounts.forEach { userAccountDao.insertUserAccount(it) }
            appSettingsDao.insertOrUpdateSettings(payload.settings)

            logActivity(actor, actorRole, "تم استعادة نسخة احتياطية للبيانات بنجاح")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            logActivity(actor, actorRole, "فشل في استعادة النسخة الاحتياطية للبيانات: ${e.localizedMessage}")
            false
        }
    }
}
