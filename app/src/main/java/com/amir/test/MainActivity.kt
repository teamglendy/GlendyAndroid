package com.amir.test

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket


class MainActivity() : AppCompatActivity() {

    lateinit var input: BufferedReader
    lateinit var output: PrintWriter
    lateinit var socket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main);

        val blockNumber = intent.extras!!.getInt("blockNumber")

        // connect()
        //foo()

        setContentView(Playground(this, blockNumber))


    }

    fun connect() {

        CoroutineScope(Dispatchers.Main).launch {

            val textFromServer = withContext(Dispatchers.IO) {
                socket = Socket("ir.cloud9p.org", 1768)

                input = BufferedReader(InputStreamReader(socket.getInputStream()))
                output = PrintWriter(socket.getOutputStream())

                output.write("Hello Server")
                output.flush()

                while (true){
                    var r = read()
                    parse(r!!)
                }


            }

            Log.d("Hello", textFromServer.toString())
        }
    }

    fun read(): String? {
        var r = input.readLine()
        Log.d("Salam", r)

        return r
    }

    fun parse(input:String?){
        var r:String
        if (input!!.contains(' ')) {
            r = input.split(" ")[0]
        }
        else
            r = input

        when (r) {
            "CONN" -> Log.d("Salam", r)
            "WAIT" -> Log.d("Salam", r)
            "INIT" -> Log.d("Salam", r)
            "SENT" -> Log.d("Salam", r)
            "TURN" -> Log.d("Salam", r)
            "WALL" -> Log.d("Salam", r)
            "GLND" -> Log.d("Salam", r)
            "WON" -> Log.d("Salam", r)
            "LOST" -> Log.d("Salam", r)
            "ERR" -> Log.d("Salam", r)
            "DIE" -> Log.d("Salam", r)

            else -> {
                Toast.makeText(this@MainActivity, "parse(): got null", Toast.LENGTH_SHORT)
                    .show()
                MainActivity().finish()
            }
        }
    }
    fun parseInit(){
        var buff:String?
        while (true){
            buff=read()
            if (buff!!.contains(' ')) {
                buff = buff.split(" ")[0]
            }
            when(buff){

                "w"-> println()
                "g"-> println()
                "SENT"-> println()

                else -> {
                    Toast.makeText(this@MainActivity, "parseInit(): got null", Toast.LENGTH_SHORT)
                        .show()
                    MainActivity().finish()
                }
            }

        }
    }

}