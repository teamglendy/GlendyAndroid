package com.amir.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity() : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main);

        val blockNumber = intent.extras!!.getInt("blockNumber")

        setContentView(Playground().SinglePlayer(this, blockNumber))
    }

}