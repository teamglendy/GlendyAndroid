package com.amir.test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MultiPlayerActivity : AppCompatActivity() {
    lateinit var hostname: EditText
    lateinit var port: EditText
    lateinit var connect: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_multi_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        hostname = findViewById(R.id.hostname)
        port = findViewById(R.id.port)
        connect = findViewById(R.id.connect)

        connect.setOnClickListener {
            val host = hostname.text.toString()
            val p = port.text.toString()

            if (host.isEmpty()) {
                hostname.error = "HostName cannot be empty"
                return@setOnClickListener
            }
            if (p.isEmpty()) {
                port.error = "Port cannot be empty"
                return@setOnClickListener
            }

            val intent=Intent(this@MultiPlayerActivity,MultiPlayerGameActivity::class.java)
            intent.putExtra("hostname",host)
            intent.putExtra("port",p.toInt())
            startActivity(intent)
        }

    }
}

