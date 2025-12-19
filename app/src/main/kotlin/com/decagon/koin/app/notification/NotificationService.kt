package com.koin.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.decagon.R

class NotificationService (
    private val context: Context
) {

    companion object {
        const val CHANNEL_ID = "coin_purchase_channel"
        const val CHANNEL_NAME = "Coin Purchase Notifications"
        const val COIN_PURCHASE_NOTIFICATION_ID = 1
        const val WATCHLIST_NOTIFICATION_ID = 2
        const val PRICE_ALERT_NOTIFICATION_ID = 3
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new coin purchases"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showCoinPurchaseNotification(coinName: String, amount: Double) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setContentTitle("Coin Purchased!")
            .setContentText("You have successfully purchased $amount of $coinName.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()


        notificationManager.notify(COIN_PURCHASE_NOTIFICATION_ID, notification)
    }

    fun showWatchlistNotification(coinName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setContentTitle("Watchlist Updated!")
            .setContentText("$coinName has been added to your watchlist.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(WATCHLIST_NOTIFICATION_ID, notification)
    }

    fun showCoinSoldNotification(coinName: String, amount: Double) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setContentTitle("Coin Sold!")
            .setContentText("You have successfully sold $amount of $coinName.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(COIN_PURCHASE_NOTIFICATION_ID, notification)
    }

    fun showPriceAlertNotification(coinName: String, targetPrice: Double, currentPrice: Double, alertType: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val title = "Price Alert for $coinName!"
        val message = when (alertType) {
            "above" -> "$coinName has reached or exceeded your target price of $%.2f. Current price: $%.2f".format(targetPrice, currentPrice)
            "below" -> "$coinName has fallen to or below your target price of $%.2f. Current price: $%.2f".format(targetPrice, currentPrice)
            else -> "Price alert for $coinName triggered. Target: $%.2f, Current: $%.2f".format(targetPrice, currentPrice)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(PRICE_ALERT_NOTIFICATION_ID, notification)
    }
}
