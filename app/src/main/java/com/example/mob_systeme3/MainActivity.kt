package com.example.mob_systeme3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnDownload: Button
    private lateinit var urlInput: EditText
    private lateinit var downloadBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        bindView()

        btnDownload.setOnClickListener {
            val urlText = urlInput.text.toString().trim()
            if (!Patterns.WEB_URL.matcher(urlText).matches()) {
                Toast.makeText(this, "Bitte eine gültige URL eingeben", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DownloadService::class.java)
            intent.putExtra(DownloadService.EXTRA_URL, urlText)

            startForegroundService(intent)
        }
    }

    fun bindView(){
        btnDownload = findViewById<Button>(R.id.downloadButton)
        urlInput = findViewById<EditText>(R.id.urlEditText)
        downloadBar = findViewById<ProgressBar>(R.id.downloadProgressBar)
    }

    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val progress = intent?.getIntExtra(DownloadService.EXTRA_PROGRESS, 0) ?: 0

            downloadBar.progress = progress
        }
    }

    override fun onStart() {
        super.onStart()

        val filter = IntentFilter(DownloadService.ACTION_DOWNLOAD_PROGRESS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(progressReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(progressReceiver, filter)
        }

        val prefs = getSharedPreferences(DownloadService.PREFS_NAME, MODE_PRIVATE)
        val savedProgress = prefs.getInt(DownloadService.PREF_PROGRESS, 0)
        downloadBar.progress = savedProgress
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(progressReceiver)
    }
}
