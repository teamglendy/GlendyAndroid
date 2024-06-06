package com.amir.test

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class EnterActivity : AppCompatActivity() {

    lateinit var single: Button
    lateinit var multi: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_enter)

        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        single = findViewById(R.id.single)
        multi = findViewById(R.id.multi)

        single.setOnClickListener {
            val intent=Intent(this@EnterActivity,DifficultyActivity::class.java)
            startActivity(intent)
        }
        multi.setOnClickListener {
            val intent=Intent(this@EnterActivity,MultiPlayerActivity::class.java)
            startActivity(intent)
        }
    }
}
