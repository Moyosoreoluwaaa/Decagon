
package com.koin.data.notification

import androidx.room.Database
import androidx.room.RoomDatabase
import com.koin.domain.notification.Notification

@Database(entities = [Notification::class], version = 1, exportSchema = false)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}
