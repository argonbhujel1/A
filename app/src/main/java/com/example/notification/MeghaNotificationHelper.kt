package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.AppLanguage
import com.example.model.Order
import com.example.model.OrderStatus

class MeghaNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_ORDERS = "megha_orders_channel"
        const val CHANNEL_NAME_ORDERS = "Megha Hotel Orders & Delivery"
        const val CHANNEL_DESC_ORDERS = "Real-time updates on orders, kitchen status, and rider delivery"

        private var instance: MeghaNotificationHelper? = null
        fun getInstance(context: Context): MeghaNotificationHelper {
            return instance ?: synchronized(this) {
                instance ?: MeghaNotificationHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val orderChannel = NotificationChannel(
                CHANNEL_ID_ORDERS,
                CHANNEL_NAME_ORDERS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_ORDERS
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(orderChannel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun showOrderPlacedNotification(order: Order, language: AppLanguage) {
        val title = if (language == AppLanguage.NEPALI) {
            "मेघा होटल • अर्डर प्राप्त भयो! #${order.orderNumber}"
        } else {
            "Megha Hotel • Order Confirmed! #${order.orderNumber}"
        }

        val message = if (language == AppLanguage.NEPALI) {
            "धन्यवाद ${order.customerName}! तपाईंको रु. ${order.totalPayableNpr.toInt()} को अर्डर भान्छामा पुगिसकेको छ।"
        } else {
            "Thank you ${order.customerName}! Your order (Rs. ${order.totalPayableNpr.toInt()}) has been sent to our kitchen."
        }

        sendNotification(
            notificationId = order.orderNumber.hashCode(),
            title = title,
            message = message
        )
    }

    fun showOrderStatusNotification(order: Order, language: AppLanguage) {
        val statusText = when (order.orderStatus) {
            OrderStatus.PENDING -> if (language == AppLanguage.NEPALI) "अर्डर प्राप्त भयो" else "Order Received"
            OrderStatus.CONFIRMED -> if (language == AppLanguage.NEPALI) "अर्डर स्वीकृत भयो" else "Order Confirmed"
            OrderStatus.PREPARING -> if (language == AppLanguage.NEPALI) "भान्छामा खाना बन्दैछ 🍲" else "Kitchen is preparing your food 🍲"
            OrderStatus.READY -> if (language == AppLanguage.NEPALI) "खाना प्याक भएर तयार भयो ✓" else "Food is packed and ready for delivery ✓"
            OrderStatus.ASSIGNED -> if (language == AppLanguage.NEPALI) "डिलिभरी राइडर ${order.assignedRiderName ?: ""} तोकियो 🛵" else "Rider ${order.assignedRiderName ?: ""} assigned 🛵"
            OrderStatus.ON_THE_WAY -> if (language == AppLanguage.NEPALI) "राइडर बाटोमा छ, छिट्टै पुग्नेछ 🚀" else "Rider is on the way to ${order.deliveryAddress} 🚀"
            OrderStatus.DELIVERED -> if (language == AppLanguage.NEPALI) "अर्डर सफलतापूर्वक डिलिभर भयो! खानाको मज्जा लिनुहोस् 🎉" else "Order Delivered! Enjoy your fresh meal from Megha Hotel 🎉"
            OrderStatus.CANCELLED -> if (language == AppLanguage.NEPALI) "अर्डर रद्द गरिएको छ।" else "Order #${order.orderNumber} has been cancelled."
        }

        val title = "Megha Hotel • #${order.orderNumber}"
        val message = "${order.customerName}: $statusText"

        sendNotification(
            notificationId = order.orderNumber.hashCode(),
            title = title,
            message = message
        )
    }

    fun showStaffOrderAlert(order: Order, roleLabel: String) {
        val title = "🔔 New Order: #${order.orderNumber}"
        val message = "New order for Rs. ${order.totalPayableNpr.toInt()} (${order.items.size} items) at ${order.locality}. Tap to review."
        sendNotification(
            notificationId = (order.orderNumber + "_staff").hashCode(),
            title = title,
            message = message
        )
    }

    fun sendTestNotification(language: AppLanguage) {
        val title = if (language == AppLanguage.NEPALI) {
            "मेघा होटल उर्लाबारी • सूचना प्रणाली सक्रिय छ!"
        } else {
            "Megha Hotel Urlabari • Notifications Active!"
        }

        val message = if (language == AppLanguage.NEPALI) {
            "तपाईंले अब अर्डर स्थिति, भान्छाको तयारी, र राइडरको प्रत्यक्ष सूचना प्राप्त गर्नुहुनेछ।"
        } else {
            "You will now receive instant alerts for order status, kitchen preparation, and delivery updates."
        }

        sendNotification(
            notificationId = 1001,
            title = title,
            message = message
        )
    }

    private fun sendNotification(notificationId: Int, title: String, message: String) {
        try {
            if (!hasNotificationPermission()) return

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val largeBitmap = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_megha_splash_logo)
            } catch (e: Exception) {
                null
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_ORDERS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .apply {
                    if (largeBitmap != null) {
                        setLargeIcon(largeBitmap)
                    }
                }
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permission denied or revoked by user
        } catch (e: Exception) {
            // Other non-fatal issues
        }
    }
}
