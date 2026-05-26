package com.example.mob_systeme3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class DownloadService : Service() {

    private val CHANNEL_ID = "download_channel"

    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Download Service",
            NotificationManager.IMPORTANCE_LOW
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Download läuft")
            .setContentText("Datei wird heruntergeladen...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)




        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}