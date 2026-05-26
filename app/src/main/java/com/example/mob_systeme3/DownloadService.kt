package com.example.mob_systeme3

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class DownloadService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("Test", "Service test")

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}