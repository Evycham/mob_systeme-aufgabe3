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
import java.net.URL
import java.net.URLConnection

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

    private fun parseTitle(input: String): String? {
        if(input.isBlank()) return null

        val tags = input.split("/")

        if(tags.last().isBlank()){
            if(tags.size >= 2){
                tags.dropLast(1)
                return tags.last()
            }
            return null
        }
        return tags.last()
    }

    private fun setConnection(link: String) {
        if (link.isBlank()) return

        try {
            val url = URL(link)
            val connection = url.openConnection()
            val inputStream = connection.getInputStream()

            val size = connection.contentLength
            var readyBytes = 0
            var progress = 0

            do {
                val downloaded = inputStream.read()

                readyBytes += downloaded

                progress = readyBytes / size * 100

            } while (readyBytes != size)

            connection.contentType
        } catch (e: Exception){
            Log.d("Error", e.toString())
        }

    }

}