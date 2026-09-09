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
        
        freshItems.forEach { item ->
            val diff = item.expiryDate - now
            val daysLeft = (diff / dayInMillis).toInt()
            
            val milestone = when {
                diff < 0 -> -1 // Expired
                diff <= TimeUnit.HOURS.toMillis(1) -> 0 // Today
                daysLeft == 1 -> 1 // Tomorrow
                daysLeft == 3 -> 3 // 3 days
                else -> null
            }

            if (milestone != null && milestone != item.lastNotificationMilestone) {
                val message = when (milestone) {
                    -1 -> "${item.name} has expired."
                    0 -> "${item.name} expires today! Use it now."
                    1 -> "${item.name} expires tomorrow. Use it soon."
                    3 -> "${item.name} expires in 3 days."
                    else -> ""
                }
                
                if (message.isNotEmpty()) {
                    sendNotification(item.id, "Food Expiry", message)
                    foodDao.update(item.copy(lastNotificationMilestone = milestone))
                }
            }
        }

        return Result.success()
    }

    private fun sendNotification(id: Int, title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "food_expiry_channel_v3"

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
