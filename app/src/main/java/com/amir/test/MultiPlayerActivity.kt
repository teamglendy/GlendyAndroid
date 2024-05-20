package com.amir.test

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class MultiPlayerActivity : AppCompatActivity() {

    lateinit var input: BufferedReader
    lateinit var output: PrintWriter
    lateinit var socket: Socket

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

            connect(host, p.toInt())
        }

    }

    fun connect(hostName: String, port: Int) {

        CoroutineScope(Dispatchers.Main).launch {

            val textFromServer = withContext(Dispatchers.IO) {
                socket = Socket(hostName, port)

                input = BufferedReader(InputStreamReader(socket.getInputStream()))
                output = PrintWriter(socket.getOutputStream())

                output.write("Hello Server")
                output.flush()

                while (true) {
                    var r = read()
                    parse(r!!)
                }


            }

            Log.d("Hello", textFromServer.toString())
        }
    }

    fun read(): String? {
        var r = input.readLine()

        return r
    }

    fun parse(input: String?) {
        var r: String
        if (input!!.contains(' ')) {
            r = input.split(" ")[0]
        } else
            r = input

        when (r) {
            "CONN" -> Log.d("Salam", r)
            "WAIT" -> Log.d("Salam", r)
            "INIT" -> {
                Log.d("Salam", r)
                parseInit()
            }

            "SENT" -> Log.d("Salam", r)
            "TURN" -> Log.d("Salam", r)
            "WALL" -> Log.d("Salam", r)
            "GLND" -> Log.d("Salam", r)
            "WON" -> Log.d("Salam", r)
            "LOST" -> Log.d("Salam", r)
            "ERR" -> Log.d("Salam", r)
            "DIE" -> Log.d("Salam", r)

            else -> {
                Toast.makeText(this, "parse(): got null", Toast.LENGTH_SHORT)
                    .show()
                MainActivity().finish()
            }
        }
    }

    fun parseInit() {
        var buff: String?
        while (true) {
            buff = read()
            if (buff!!.contains(' ')) {
                buff = buff.split(" ")[0]
            }
            when (buff) {

                "w" -> Log.d("Salam", buff)
                "g" -> Log.d("Salam", buff)
                "SENT" -> {
                    Log.d("Salam", buff); return
                }

                else -> {
                    Toast.makeText(this, "parseInit(): got null", Toast.LENGTH_SHORT)
                        .show()
                    MainActivity().finish()
                }
            }

        }
    }

    fun putMessage(x: Int, y: Int) {
        if (x < 0 || y < 0) {
            Log.d("Glendy", "putMessage(" + x.toString() + ", " + y.toString() + ")")
            return
        }
        output.println("p " + x.toString() + " " + y.toString())
    }

    fun moveMessage(dir: Int) {

        var str: String

        when (dir) {
            1 -> str = "W"
            2 -> str = "NW"
            3 -> str = "NE"
            4 -> str = "E"
            5 -> str = "SE"
            6 -> str = "SW"
            else -> {
                Log.d("Glendy", "moveMessage(" + dir.toString() + ")")
                return
            }
        }
        output.println("m " + dir.toString())
    }

    fun parsePut() {
        // p xx yy

    }

    fun parseMove() {
        // p xx yy

    }
}