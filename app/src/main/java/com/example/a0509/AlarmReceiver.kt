package com.example.a0509

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val title = intent.getStringExtra("title")
        val date = intent.getStringExtra("date")
        val time = intent.getStringExtra("time")

        val multiLineContent = "$title\n $date\n $time"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "schedule_channel"
            val channelName = "Schedule Notifications"
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }


        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            "schedule_channel"
        } else {
            "default_channel"
        }
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("일정 알림")
            .setStyle(NotificationCompat.BigTextStyle().bigText(multiLineContent))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)


        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(0, notificationBuilder.build())

        val message = "일정 알림이 도착했습니다.\n$title"
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    }
}