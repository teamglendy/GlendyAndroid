package com.amir.test

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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

class MultiPlayerPlayground(context: Context?, hostname: String, port: Int) : SurfaceView(context),
    View.OnTouchListener {

    lateinit var input: BufferedReader
    lateinit var output: PrintWriter
    lateinit var socket: Socket

    lateinit var matrix: Array<Array<Dot?>>
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
        connect(hostname, port)
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
                socket = Socket("45.94.213.254", 1768) //todo fix

                input = BufferedReader(InputStreamReader(socket.getInputStream()))
                output = PrintWriter(socket.getOutputStream())

                while (true) {// reads a line and passes it to parse the parse passes it to parseinit and then parseinit passes it to parseposition
                    var r = read()
                    parse(r!!)
                }
            }

            Log.d("Glendy", "Connected to server")
        }
    }

    fun parse(input: String?) {//checks different states
        var r: String
        if (input!!.contains(' ')) {
            r = input.split(" ")[0]
        } else
            r = input

        when (r) {
            "CONN" -> Log.d("Glendy", r)
            "WAIT" -> Log.d("Glendy", r)
            "INIT" -> {
                Log.d("Glendy", r)
                parseInit()
            }

            "SENT" -> Log.d("Glendy", r)
            "TURN" -> Log.d("Glendy", r)
            "WALL" -> Log.d("Glendy", r)
            "GLND" -> Log.d("Glendy", r)
            "WON" -> Log.d("Glendy", r)
            "LOST" -> Log.d("Glendy", r)
            "ERR" -> Log.d("Glendy", r)
            "DIE" -> Log.d("Glendy", r)

            else -> {// To do replacement for finish()
                Toast.makeText(context, "parse(): got null", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun parseInit() { // sets the walls if the message is init
        var buff: String?
        var pos: String? = null
        var tmpDot: Dot? = null

        while (true) {
            buff = read()
            if (buff!!.contains(' ')) {
                //"[0], [1]"
                //"w 0 1"
                // plasht hack
                pos = buff.split(" ")[1]  + " " + buff.split(" ")[2]
                buff = buff.split(" ")[0]
            }
            // else
            //todo: exit the program here
            when (buff) {
                "w" -> {
                    Log.d("Glendy", "parseInit():" + buff)
                    tmpDot = parsePosition(pos)
                    putWall(tmpDot!!.y, tmpDot!!.x)
                } // pos is the string position of the wall and then putWall converts this string to int x and int y
                "g" -> Log.d("Glendy", buff)
                "SENT" -> {
                    redraw()
                    break
                }

                else -> {// to do replacement for finish
                   // Toast.makeText(context, "parseInit(): got null", Toast.LENGTH_SHORT)
                       // .show()
                    Log.d("Glendy", "ParseInit(): Odd buff: "  + buff)
                }
            }

        }
    }

    fun putMessage(x: Int, y: Int) {// says to server to set a wall
        if (x < 0 || y < 0) {
            Log.d("Glendy", "putMessage(" + x.toString() + ", " + y.toString() + ")")
            return
        }
        output.println("p " + x.toString() + " " + y.toString())
    }

    fun moveMessage(dir: Int) { // says to server that witch direction is glenda moving to

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

    fun putWall(x: Int, y: Int) { //sets a wall on the phone screen
        if (x < 0 || y < 0) {
            Log.d("Glendy", "putMessage(" + x.toString() + ", " + y.toString() + ")")
            return
        }
        matrix[x][y]!!.status = Dot.STATUS_ON
    }

    fun putGlenda(x: Int, y: Int) { //sets a glenda on the screen
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
    fun parsePosition(str: String?): Dot? { //gets a string position from server and changes it to an int position
        var xPos: String
        var yPos: String

        if (str == null) {
            Log.d("Glendy", "parsePosition(null)")
            return null
        }
        xPos = str.split(" ")[0]
        yPos = str.split(" ")[1]

        Log.d("Glendy", "parsePostion(\"" + str + "\")")

        return Dot(xPos.toInt(), yPos.toInt())
    }

    private fun redraw() {
        val c = holder.lockCanvas()
        c.drawColor(Color.LTGRAY)
        val paint = Paint()
        paint.flags = Paint.ANTI_ALIAS_FLAG
        for (i in 0 until ROW) {
            var offset = 0
            if (i % 2 != 0) {
                offset = WIDTH / 2
            }
            for (j in 0 until COL) {
                val one = getDot(j, i)
                when (one!!.status) {
                    Dot.STATUS_OFF -> paint.color = -0x111112
                    Dot.STATUS_ON -> paint.color = -0x5600
                    Dot.STATUS_IN -> paint.color = -0x10000
                    else -> {}
                }
                c.drawOval(
                    RectF(
                        (one.x * WIDTH + offset).toFloat(),
                        (one.y * WIDTH).toFloat(),
                        ((one.x + 1) * WIDTH + offset).toFloat(),
                        ((one.y + 1) * WIDTH).toFloat()
                    ), paint
                )
            }
        }
        holder.unlockCanvasAndPost(c)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
/*        if (event.action == MotionEvent.ACTION_UP) {
//            Toast.makeText(getContext(),event.getX()+":"+ event.getY(),Toast.LENGTH_SHORT).show();
            val x: Int
            val y: Int
            y = event.y.toInt() / WIDTH
            x = if (y % 2 == 0) {
                (event.x / WIDTH).toInt()
            } else {
                ((event.x - WIDTH / 2) / WIDTH).toInt()
            }
            if (x + 1 > COL || y + 1 > ROW) {
                initGame()
                //                System.out.println("----------------------------");
//                for (int i = 1;i<7;i++){
//                    System.out.println(i+"@"+getDistance(cat,i));
//                }
            } else if (getDot(x, y)!!.status == Dot.STATUS_OFF) {
                getDot(x, y)!!.status = Dot.STATUS_ON
                move()
            }*/
            redraw()
        //}
        return true
    }
}