package com.amir.test

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MultiPlayerGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_multi_player_game)
        /*        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }*/

        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val hostname = intent.extras!!.getString("hostname")
        val port = intent.extras!!.getInt("port")
        val nickName = intent.extras!!.getString("nameString")
        val game = intent.extras!!.getInt("game")
        val side = intent.extras!!.getInt("side")
        val opts = intent.extras!!.getInt("opts")

        setContentView(Playground().MultiPlayer(this, hostname!!, port, nickName!!, game, side, opts))

    }
}