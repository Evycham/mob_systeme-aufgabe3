package com.example.mob_systeme3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

class DownloadService : Service() {

    private val CHANNEL_ID = "download_channel"
    private var urlText: String? = null

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

        urlText = intent?.getStringExtra("url")

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Download läuft")
            .setContentText("Datei wird heruntergeladen...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)

        setConnection(urlText)


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

    private fun setConnection(link: String?) {
        if (link.isNullOrBlank()) return

        try {
            val url = URL(link)
            val connection = url.openConnection() as HttpURLConnection

            if(connection.responseCode == 200){
                val inputStream = connection.inputStream
                val size = connection.contentLength

                val file = fileBuild(link)

                val fos = FileOutputStream(file)
                val ba = ByteArray(4096)

                var downloadedBytes = 0
                var progress = 0

                var bytesRead: Int

                while(inputStream.read(ba).also { bytesRead = it } != -1){
                    // ba - Buffer, 0 - ab welchem Index. bytesRead - wie viel gültige Bytes
                    fos.write(ba, 0, bytesRead)
                    downloadedBytes += bytesRead

                    progress = (downloadedBytes * 100) / size
                }
            } else throw Exception("Die Verbindung ist Fehlgeschlagen!")

        } catch (e: Exception){
            Log.d("Error", e.toString())
        }
    }


    private fun fileBuild(input: String): File {
        val directory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: throw Exception("Keinen passenden Ordner!")
        val dateName = parseTitle(input) ?: throw Exception("Die Link ist falsch!")
        return File(directory, dateName)
    }
}
