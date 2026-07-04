package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        Subscriber::class,
        FinancialTransaction::class,
        ActivityLog::class,
        UserAccount::class,
        AppSettings::class,
        SyncLog::class,
        PendingWhatsAppMessage::class,
        Notification::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriberDao(): SubscriberDao
    abstract fun transactionDao(): TransactionDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun syncLogDao(): SyncLogDao
    abstract fun pendingWhatsAppMessageDao(): PendingWhatsAppMessageDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "debt_manager_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
