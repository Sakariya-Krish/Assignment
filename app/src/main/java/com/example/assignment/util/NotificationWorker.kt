package com.example.assignment.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.assignment.data.FoodDatabase
import com.example.assignment.data.FoodStatus
import com.example.assignment.data.SettingsManager
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val settingsManager = SettingsManager(applicationContext)
        if (!settingsManager.notificationsEnabled.first()) return Result.success()

        val database = FoodDatabase.getDatabase(applicationContext)
        val foodDao = database.foodDao()
        val items = foodDao.getAllItems().first()

        val now = System.currentTimeMillis()
        val dayInMillis = TimeUnit.DAYS.toMillis(1)

        val freshItems = items.filter { it.status == FoodStatus.FRESH }
        
        val expiringToday = freshItems.filter { item ->
            val diff = item.expiryDate - now
            diff in 0..TimeUnit.HOURS.toMillis(1) // Roughly today
        }
        
        val expiringSoon = freshItems.filter { item ->
            val diff = item.expiryDate - now
            diff in 0..(3 * dayInMillis)
        }

        if (expiringToday.isNotEmpty()) {
            val names = expiringToday.joinToString(", ") { it.name }
            sendNotification(999, "Use Today!", "Use your $names today to reduce food waste.")
        } else if (expiringSoon.size >= 3) {
            sendNotification(888, "Expiring Soon", "You have ${expiringSoon.size} foods expiring soon.")
        }

        expiringSoon.forEach { item ->
            val diff = item.expiryDate - now
            val message = when {
                diff in 0..dayInMillis -> "${item.name} expires tomorrow. Use it soon."
                diff in (2 * dayInMillis)..(3 * dayInMillis) -> "${item.name} expires in 3 days."
                else -> null
            }
            if (message != null) {
                sendNotification(item.id, "Food Expiry", message)
            }
        }

        return Result.success()
    }

    private fun sendNotification(id: Int, title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "food_expiry_channel_v2"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Food Expiry Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
