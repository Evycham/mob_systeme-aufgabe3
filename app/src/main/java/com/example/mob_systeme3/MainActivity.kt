package com.example.mob_systeme3

import android.content.Intent
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
    private lateinit var downloadProgress: ProgressBar

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
            val intent = Intent(this, DownloadService::class.java)

            startService(intent)
        }
    }

    fun bindView(){
        btnDownload = findViewById<Button>(R.id.downloadButton)
        urlInput = findViewById<EditText>(R.id.urlEditText)
        downloadProgress = findViewById<ProgressBar>(R.id.downloadProgressBar)
    }
}