package com.amir.test

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class MultiPlayerPlayground (context: Context?,hostname:String,port:Int) : SurfaceView(context), View.OnTouchListener {

    lateinit var input: BufferedReader
    lateinit var output: PrintWriter
    lateinit var socket: Socket

    private val matrix: Array<Array<Dot?>>
    private var glenda: Dot? = null

    private var WIDTH = 40
    private val COL = 11
    private val ROW = 11


    var callback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            // redraw()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            WIDTH = width / (COL + 1)
            // redraw()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {}
    }
    init {
        holder.addCallback(callback)
        matrix = Array(ROW) { arrayOfNulls(COL) }
        for (i in 0 until ROW) {
            for (j in 0 until COL) {
                matrix[i][j] = Dot(j, i)
            }
        }
        setOnTouchListener(this)
        connect(hostname,port)
        initGame()

    }

    private fun getDot(x: Int, y: Int): Dot? {
        return matrix[y][x]
    }


    fun read(): String? {
        var r = input.readLine()

        return r
    }
    fun connect(hostName: String, port: Int) {

        CoroutineScope(Dispatchers.Main).launch {

            val textFromServer = withContext(Dispatchers.IO) {
                socket = Socket(hostName, port)

                input = BufferedReader(InputStreamReader(socket.getInputStream()))
                output = PrintWriter(socket.getOutputStream())
            }

            Log.d("Glendy", "Connected to server")
        }
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

            else -> {// To do replacement for finish()
                Toast.makeText(context, "parse(): got null", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun parseInit() {
        var buff: String?
        var buff2: String
        while (true) {
            buff = read()
            if (buff!!.contains(' ')) {
                buff = buff.split(" ")[0]
                buff2 = buff.split(" ")[1]
            }
            when (buff) {
                "w" -> { Log.d("Glendy", buff)  ;  }
                "g" -> Log.d("Salam", buff)
                "SENT" -> {
                    Log.d("Salam", buff); return
                }

                else -> {// to do replacement for finish
                    Toast.makeText(context, "parseInit(): got null", Toast.LENGTH_SHORT)
                        .show()
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

    fun initGame() {
        for (i in 0 until ROW) {
            for (j in 0 until COL) {
                matrix[i][j]!!.status = Dot.STATUS_OFF
            }
        }
    }

    fun putWall(x: Int, y: Int) {
        if (x < 0 || y < 0) {
            Log.d("Glendy", "putMessage(" + x.toString() + ", " + y.toString() + ")")
            return
        }
    }

    fun putGlenda(x: Int, y: Int) {
        if (x < 0 || y < 0) {
            Log.d("Glendy", "putMessage(" + x.toString() + ", " + y.toString() + ")")
            return
        }
        if (glenda != null) {
            glenda!!.status = Dot.STATUS_OFF
        }
        getDot(x, y)!!.status = Dot.STATUS_IN
        glenda!!.setXY(x, y)
    }

    // "xx yy\n"
    fun parsePosition(str:String):Dot?{
        var xPos:String
        var yPos:String

        xPos=str.split("")[0]
        yPos=str.split("")[1]

        return Dot(xPos.toInt(), yPos.toInt())
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        TODO("Not yet implemented")
    }
}