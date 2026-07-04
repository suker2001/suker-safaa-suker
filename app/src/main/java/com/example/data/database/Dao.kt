package com.example.data.database

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriberDao {
    @Query("SELECT * FROM subscribers ORDER BY name ASC")
    fun getAllSubscribers(): Flow<List<Subscriber>>

    @Query("SELECT * FROM subscribers WHERE id = :id")
    fun getSubscriberById(id: Int): Flow<Subscriber?>

    @Query("SELECT * FROM subscribers WHERE id = :id")
    suspend fun getSubscriberByIdOneShot(id: Int): Subscriber?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriber(subscriber: Subscriber): Long

    @Update
    suspend fun updateSubscriber(subscriber: Subscriber)

    @Delete
    suspend fun deleteSubscriber(subscriber: Subscriber)

    @Query("SELECT * FROM subscribers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR uniqueCode LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' OR CAST(totalDebt AS TEXT) LIKE '%' || :query || '%' OR CAST((totalDebt - totalPaid) AS TEXT) LIKE '%' || :query || '%'")
    fun searchSubscribers(query: String): Flow<List<Subscriber>>
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE subscriberId = :subscriberId ORDER BY timestamp DESC")
    fun getTransactionsForSubscriber(subscriberId: Int): Flow<List<FinancialTransaction>>

    @Query("SELECT * FROM transactions WHERE subscriberId = :subscriberId ORDER BY timestamp DESC")
    suspend fun getTransactionsForSubscriberOneShot(subscriberId: Int): List<FinancialTransaction>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): FinancialTransaction?

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<FinancialTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinancialTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: FinancialTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: FinancialTransaction)

    @Query("DELETE FROM transactions WHERE subscriberId = :subscriberId")
    suspend fun deleteTransactionsBySubscriber(subscriberId: Int)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllActivityLogs(): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLog)

    @Query("DELETE FROM activity_logs")
    suspend fun clearAllLogs()
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY role ASC, username ASC")
    fun getAllUserAccounts(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts WHERE id = :id")
    suspend fun getUserAccountById(id: Int): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE username = :username AND pinCode = :pin AND isEnabled = 1")
    suspend fun getUserByUsernameAndPin(username: String, pin: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(account: UserAccount): Long

    @Update
    suspend fun updateUserAccount(account: UserAccount)

    @Delete
    suspend fun deleteUserAccount(account: UserAccount)
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsOneShot(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: AppSettings)
}

@Dao
interface SyncLogDao {
    @Query("SELECT * FROM sync_logs ORDER BY timestamp DESC")
    fun getAllSyncLogs(): Flow<List<SyncLog>>

    @Query("SELECT * FROM sync_logs WHERE status = :status ORDER BY timestamp ASC")
    suspend fun getSyncLogsByStatus(status: SyncStatus): List<SyncLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncLog(log: SyncLog)

    @Update
    suspend fun updateSyncLog(log: SyncLog)

    @Delete
    suspend fun deleteSyncLog(log: SyncLog)
}

@Dao
interface PendingWhatsAppMessageDao {
    @Query("SELECT * FROM pending_whatsapp_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<PendingWhatsAppMessage>>

    @Query("SELECT * FROM pending_whatsapp_messages WHERE status = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingMessages(): List<PendingWhatsAppMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: PendingWhatsAppMessage)

    @Update
    suspend fun updateMessage(message: PendingWhatsAppMessage)

    @Delete
    suspend fun deleteMessage(message: PendingWhatsAppMessage)
}
