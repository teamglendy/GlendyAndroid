package com.amir.test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MultiPlayerActivity : AppCompatActivity() {
    lateinit var hostname: EditText
    lateinit var port: EditText
    lateinit var connect: Button
    lateinit var name: EditText
    lateinit var random: RadioButton
    lateinit var trapper: RadioButton
    lateinit var glenda: RadioButton
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
        name = findViewById(R.id.name)
        random = findViewById(R.id.random)
        trapper = findViewById(R.id.trapper)
        glenda = findViewById(R.id.glenda)



        connect.setOnClickListener {
            val host = hostname.text.toString()
            val p = port.text.toString()
            var nameString = name.text.toString()


            val game = 0
            val opts = 0
            var side = 2

            if (host.isEmpty()) {
                hostname.error = "HostName cannot be empty"
                return@setOnClickListener
            }
            if (p.isEmpty()) {
                port.error = "Port cannot be empty"
                return@setOnClickListener
            }
            if (nameString.length > 8) {
                name.error = "name too long"
            } else if (nameString.isNullOrEmpty()) {
                nameString = "@"
            }

            if(trapper.isChecked){
                side=0
            }
            else if(glenda.isChecked){
                side=1
            }
            else if(random.isChecked){
                side=2
            }



            val intent = Intent(this@MultiPlayerActivity, MultiPlayerGameActivity::class.java)
            intent.putExtra("hostname", host)
            intent.putExtra("port", p.toInt())
            intent.putExtra("nameString",nameString)
            intent.putExtra("game",game)
            intent.putExtra("side",side)
            intent.putExtra("opts",opts)
            startActivity(intent)

        }

    }
}

