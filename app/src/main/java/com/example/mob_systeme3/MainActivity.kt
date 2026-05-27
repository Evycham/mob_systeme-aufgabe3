package com.example.mob_systeme3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
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

            val intent = Intent(this, DownloadService::class.java)
            intent.putExtra("url", urlText)

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
            val progress = intent?.getIntExtra("progress", 0) ?: 0

            downloadBar.progress = progress
        }
    }

    override fun onStart() {
        super.onStart()

        registerReceiver(
            progressReceiver,
            IntentFilter("DOWNLOAD_PROGRESS")
        )

        val prefs = getSharedPreferences("download_data", MODE_PRIVATE)
        val savedProgress = prefs.getInt("progress", 0)
        downloadBar.progress = savedProgress
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(progressReceiver)
    }
}