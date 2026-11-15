package com.pizza5ps.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.pizza5ps.app.MainActivity
import com.pizza5ps.app.R
import com.pizza5ps.app.model.Order
import com.pizza5ps.app.model.OrderStatus

/**
 * Order-progress notifications.
 *
 * Delivery tracking is the one part of this app the user is expected to *leave* —
 * you place an order and put the phone down. Notifications are what make the stage
 * changes reach them there, and they also mean the demo can be shown from the
 * notification shade rather than only from the tracking screen.
 *
 * All four stages reuse a single notification id, so the shade shows one live order
 * that updates in place instead of a stack of four.
 */
object OrderNotifier {

    private const val CHANNEL_ID = "order_updates"
    private const val NOTIFICATION_ID = 5001

    /**
     * Creates the channel. Safe to call repeatedly — re-creating an existing channel
     * is a no-op, and the channel must exist before any notification is posted.
     */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Order updates",
            // High importance so stage changes surface as heads-up alerts; this is
            // the user's own food order, which is exactly what that is for.
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Live updates while your pizza is being prepared and delivered"
            enableLights(true)
            enableVibration(true)
        }

        ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    /** True once the user has granted notification access (always true pre-API 33). */
    fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** The stage's headline and body as they appear in the shade. */
    private fun copyFor(status: OrderStatus): Pair<String, String> = when (status) {
        OrderStatus.CONFIRMED ->
            "Order confirmed" to "The kitchen has your order. Hang tight."
        OrderStatus.PREPARING ->
            "Into the oven" to "Hand-tossed, topped and baking right now."
        OrderStatus.OUT_FOR_DELIVERY ->
            "Out for delivery" to "Vikram S. is on the way with your pizza."
        OrderStatus.DELIVERED ->
            "Delivered — enjoy!" to "Your pizza has arrived. Buon appetito."
    }

    fun notifyStatus(context: Context, order: Order, status: OrderStatus) {
        if (!hasPermission(context)) return

        val (title, body) = copyFor(status)

        // Tapping the notification reopens the app rather than starting a second copy.
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isComplete = status == OrderStatus.DELIVERED

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText("#${order.id}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openApp)
            .setAutoCancel(isComplete)
            // While in progress the notification is ongoing (not swipeable) and shows
            // a step bar; once delivered it becomes a normal dismissible notification.
            .setOngoing(!isComplete)
            .setOnlyAlertOnce(false)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .apply {
                if (isComplete) {
                    setProgress(0, 0, false)
                } else {
                    setProgress(OrderStatus.values().size, status.stepNumber, false)
                }
            }
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (securityError: SecurityException) {
            // Permission can be revoked between the check above and this call.
        }
    }

    /** Clears the live order notification, e.g. when the order is dismissed. */
    fun clear(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}
