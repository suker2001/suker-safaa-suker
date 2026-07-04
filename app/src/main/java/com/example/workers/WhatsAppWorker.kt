package com.example.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.database.AppDatabase
import com.example.data.model.SyncStatus
import com.example.utils.PhoneNumberHelper
import kotlinx.coroutines.delay

class WhatsAppWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(context)
        val dao = database.pendingWhatsAppMessageDao()
        
        val pendingMessages = dao.getPendingMessages()
        if (pendingMessages.isEmpty()) {
            return Result.success()
        }

        var hasFailures = false

        for (msg in pendingMessages) {
            try {
                // Validate phone number
                if (!PhoneNumberHelper.isValidPhoneNumber(msg.phone)) {
                    // Invalid number, fail permanently
                    dao.updateMessage(msg.copy(status = SyncStatus.FAILED, retryCount = msg.retryCount + 1))
                    continue
                }

                // Simulate network delay and WhatsApp API call
                delay(2000)

                // In a real scenario, this would call WhatsApp Business API
                // For demonstration, we mark it as SYNCED to simulate successful delivery
                dao.updateMessage(msg.copy(status = SyncStatus.SYNCED))
            } catch (e: Exception) {
                e.printStackTrace()
                // Update retry count and leave as PENDING if we want to retry, or fail if max retries
                if (msg.retryCount >= 3) {
                    dao.updateMessage(msg.copy(status = SyncStatus.FAILED, retryCount = msg.retryCount + 1))
                } else {
                    dao.updateMessage(msg.copy(retryCount = msg.retryCount + 1))
                    hasFailures = true
                }
            }
        }

        return if (hasFailures) {
            Result.retry()
        } else {
            Result.success()
        }
    }
}
