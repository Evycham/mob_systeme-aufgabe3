package com.example.mob_systeme3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

class DownloadService : Service() {

    companion object {
        const val ACTION_DOWNLOAD_PROGRESS = "com.example.mob_systeme3.DOWNLOAD_PROGRESS"
        const val EXTRA_PROGRESS = "progress"
        const val EXTRA_IS_DOWNLOADING = "is_downloading"
        const val EXTRA_STATE = "state"
        const val EXTRA_URL = "url"
        const val PREFS_NAME = "download_data"
        const val PREF_PROGRESS = "progress"
        const val PREF_IS_ACTIVE = "active"
        const val STATE_PROGRESS = "progress"
        const val STATE_DONE = "done"
        const val STATE_ERROR = "error"
    }

    private val channelId = "download_channel"
    private val notificationId = 1
    private var isDownloading = false
    private lateinit var prefs: SharedPreferences

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Download Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isDownloading) return START_NOT_STICKY

        val urlText = intent?.getStringExtra(EXTRA_URL)
        if (urlText.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Download läuft")
            .setContentText("Datei wird heruntergeladen...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()

        startForeground(notificationId, notification)
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        processingProgress(0, true, STATE_PROGRESS)

        Thread{
            setConnection(urlText)
        }.start()


        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun parseTitle(input: String): String {
        val segment = URI(input).path?.substringAfterLast('/')?.trim().orEmpty()
        return if (segment.isBlank()) "download.bin" else segment
    }

    private fun setConnection(link: String?) {
        if (link.isNullOrBlank()) {
            finishDownload()
            return
        }

        var connection: HttpURLConnection? = null
        try {
            val url = URL(link)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.requestMethod = "GET"

            if(connection.responseCode == 200){
                val size = connection.contentLength
                val file = fileBuild(link)
                val ba = ByteArray(8 * 1024)

                var downloadedBytes = 0
                var bytesRead: Int

                isDownloading = true
                BufferedInputStream(connection.inputStream).use { inputStream ->
                    BufferedOutputStream(file.outputStream()).use { outputStream ->
                        while(inputStream.read(ba).also { bytesRead = it } != -1){
                            outputStream.write(ba, 0, bytesRead)
                            downloadedBytes += bytesRead

                            if (size > 0) {
                                val progress = (downloadedBytes * 100) / size
                                processingProgress(progress, true, STATE_PROGRESS)
                                updateNotification(progress)
                            }
                        }
                    }
                }
                processingProgress(100, false, STATE_DONE)
                showFinishedNotification(file)
                openDownloadedFile(file)
            } else throw Exception("Die Verbindung ist Fehlgeschlagen!")

        } catch (e: Exception){
            Log.e("DownloadService", "Download fehlgeschlagen", e)
            processingProgress(0, false, STATE_ERROR)
        } finally {
            connection?.disconnect()
            finishDownload()
        }
    }


    private fun fileBuild(input: String): File {
        val directory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: throw Exception("Keinen passenden Ordner!")
        val dateName = parseTitle(input)
        return File(directory, dateName)
    }

    private fun processingProgress(progress: Int, isDownloading: Boolean, state: String){
        prefs.edit()
            .putInt(PREF_PROGRESS, progress)
            .putBoolean(PREF_IS_ACTIVE, isDownloading)
            .apply()

        val progressIntent = Intent(ACTION_DOWNLOAD_PROGRESS)
        progressIntent.putExtra(EXTRA_PROGRESS, progress)
        progressIntent.putExtra(EXTRA_IS_DOWNLOADING, isDownloading)
        progressIntent.putExtra(EXTRA_STATE, state)
        sendBroadcast(progressIntent)
    }

    private fun updateNotification(progress: Int) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Download läuft")
            .setContentText("$progress%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    private fun showFinishedNotification(file: File) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Download fertig")
            .setContentText(file.name)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId + 1, notification)
    }

    private fun openDownloadedFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val extension = file.extension.lowercase()
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                ?: "application/octet-stream"

            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(openIntent)
        } catch (e: Exception) {
            Log.e("DownloadService", "Datei konnte nicht geöffnet werden", e)
        }
    }

    private fun finishDownload() {
        isDownloading = false
        if (::prefs.isInitialized) {
            prefs.edit().putBoolean(PREF_IS_ACTIVE, false).apply()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}
