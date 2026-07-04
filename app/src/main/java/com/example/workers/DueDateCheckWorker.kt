package com.example.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.database.AppDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class DueDateCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(context)
            val subscriberDao = database.subscriberDao()
            val subscribers = subscriberDao.getAllSubscribers().firstOrNull() ?: emptyList()

            val currentTime = System.currentTimeMillis()
            // We consider "approaching" if due date is within the next 3 days
            val threeDaysInMillis = 3L * 24 * 60 * 60 * 1000
            
            subscribers.forEach { subscriber ->
                val dueDate = subscriber.nextDueDate
                if (dueDate != null && subscriber.remainingDebt > 0) {
                    val timeDiff = dueDate - currentTime
                    if (timeDiff in 0..threeDaysInMillis) {
                        sendNotification(subscriber)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun sendNotification(subscriber: com.example.data.model.Subscriber) {
        val channelId = "due_dates_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "تذكير بمواعيد الاستحقاق"
            val descriptionText = "إشعارات بمواعيد الديون المقتربة"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            subscriber.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            // Use standard icon for now
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("اقتراب موعد سداد")
            .setContentText("المشترك ${subscriber.name} لديه موعد سداد مستحق قريباً لمبلغ ${com.example.utils.FormatHelper.formatCurrency(subscriber.remainingDebt)}.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // Need permission on Android 13+
            try {
                notify(subscriber.id, builder.build())
            } catch (e: SecurityException) {
                // Permission not granted
            }
        }
    }
}
