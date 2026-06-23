package com.petfindercr.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.petfindercr.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "nearby_reports"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Reportes Cercanos",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificaciones de mascotas perdidas o encontradas cerca de ti"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun sendNearbyReportNotification(
        context: Context,
        title: String,
        body: String,
        notifId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }
}
