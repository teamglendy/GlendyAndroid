package com.amir.test

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DifficultyActivity : AppCompatActivity() {

    lateinit var easy: Button
    lateinit var medium: Button
    lateinit var hard: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_difficulty)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        easy = findViewById(R.id.easy)
        medium = findViewById(R.id.medium)
        hard = findViewById(R.id.hard)

        easy.setOnClickListener {
            val intent = Intent(this@DifficultyActivity, MainActivity::class.java)
            intent.putExtra("blockNumber", 25)
            startActivity(intent)
        }
        medium.setOnClickListener {
            val intent = Intent(this@DifficultyActivity, MainActivity::class.java)
            intent.putExtra("blockNumber", 15)
            startActivity(intent)
        }
        hard.setOnClickListener {
            val intent = Intent(this@DifficultyActivity, MainActivity::class.java)
            intent.putExtra("blockNumber", 5)
            startActivity(intent)
        }
    }
}