package com.example.data.database

import androidx.room.*
import com.example.data.model.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE isArchived = 0 ORDER BY timestamp DESC")
    fun getAllActiveNotifications(): Flow<List<Notification>>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0 AND isArchived = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: Notification)

    @Update
    suspend fun update(notification: Notification)

    @Delete
    suspend fun delete(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0 AND isArchived = 0")
    suspend fun markAllAsRead()

    @Query("UPDATE notifications SET isArchived = 1 WHERE isArchived = 0")
    suspend fun archiveAll()
}
