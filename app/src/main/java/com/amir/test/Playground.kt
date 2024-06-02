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
import java.util.Vector

class Playground() {

    private var state = START

    private var screenHeight = 0
    private var screenWidth = 0

    private var WIDTH = 40
    private var COL = 11
    private var ROW = 11
    private var BLOCKS = 0

    private var cat: Dot? = null
    private var matrix: Array<Array<Dot?>> = Array(ROW) { arrayOfNulls(COL) }

    companion object {
        const val WIN = 0
        const val LOSE = 1
        const val START = 2
        const val PLAYING = 3

        const val GLENDA = true
        const val TRAPPER = false
    }

    private fun getDot(x: Int, y: Int): Dot? {
        return matrix[y][x]
    }

    private fun setupmatrix() {
        for (i in 0 until ROW) {
            for (j in 0 until COL) {
                matrix[i][j] = Dot(j, i)
            }
        }
    }

    private fun initGame() {
        for (i in 0 until ROW) {
            for (j in 0 until COL) {
                matrix[i][j]!!.status = Dot.STATUS_OFF
            }
        }
        cat = Dot(5, 5)
        getDot(5, 5)!!.status = Dot.STATUS_IN
        var i = 0
        while (i < BLOCKS) {
            val x = (Math.random() * 1000 % COL).toInt()
            val y = (Math.random() * 1000 % ROW).toInt()
            if (getDot(x, y)!!.status == Dot.STATUS_OFF) {
                getDot(x, y)!!.status = Dot.STATUS_ON
                i++
            }
        }
        state = PLAYING
    }

    private fun isAtEdge(d: Dot?): Boolean {
        return if (d!!.x * d.y == 0 || d.x + 1 == COL || d.y + 1 == ROW) {
            true
        } else false
    }

    private fun getNeighbor(one: Dot?, dir: Int): Dot? {
        when (dir) {
            1 -> return getDot(one!!.x - 1, one.y)
            2 -> return if (one!!.y % 2 == 0) {
                getDot(one.x - 1, one.y - 1)
            } else {
                getDot(one.x, one.y - 1)
            }

            3 -> return if (one!!.y % 2 == 0) {
                getDot(one.x, one.y - 1)
            } else {
                getDot(one.x + 1, one.y - 1)
            }

            4 -> return getDot(one!!.x + 1, one.y)
            5 -> return if (one!!.y % 2 == 0) {
                getDot(one.x, one.y + 1)
            } else {
                getDot(one.x + 1, one.y + 1)
            }

            6 -> return if (one!!.y % 2 == 0) {
                getDot(one.x - 1, one.y + 1)
            } else {
                getDot(one.x, one.y + 1)
            }

            else -> {}
        }
        return null //?
    }

    private fun getDistance(
        one: Dot?,
        dir: Int
    ): Int { //checks the status of cat's neighbor dot(neighbor dot)
        var distance = 0
        if (isAtEdge(one)) {
            return 1
        }
        var ori = one
        var next: Dot?
        while (true) {
            next = getNeighbor(ori, dir)
            if (next!!.status == Dot.STATUS_ON) {
                return distance * -1
            }
            if (isAtEdge(next)) {
                distance++
                return distance
            }
            distance++
            ori = next
        }
    }

    private fun MoveTo(one: Dot?) {
        //moves the cat to the new position on the game board (one is the next dot)

        one!!.status = Dot.STATUS_IN
        getDot(cat!!.x, cat!!.y)!!.status = Dot.STATUS_OFF
        cat!!.setXY(one.x, one.y)
    }

    private fun move() { //?
        if (isAtEdge(cat)) {
            state = LOSE
            return
        }
        val avaliable = Vector<Dot?>()
        val positive = Vector<Dot?>()
        val al = HashMap<Dot?, Int>()
        for (i in 1..6) {
            val n = getNeighbor(cat, i)
            if (n!!.status == Dot.STATUS_OFF) {
                avaliable.add(n)
                al[n] = i
                if (getDistance(n, i) > 0) {
                    positive.add(n)
                }
            }
        }
        if (avaliable.size == 0) {
            state = WIN
        } else if (avaliable.size == 1) {
            MoveTo(avaliable[0])
        } else {
            var best: Dot? = null
            if (positive.size != 0) { //Free direction exists
                var min = 999
                for (i in positive.indices) {
                    val a = getDistance(positive[i], al[positive[i]]!!)
                    if (a < min) {
                        min = a
                        best = positive[i]
                    }
                }
            } else { //All have blocks
                var max = 0
                for (i in avaliable.indices) {
                    val k = getDistance(avaliable[i], al[avaliable[i]]!!)
                    if (k <= max) {
                        max = k
                        best = avaliable[i]
                    }
                }
            }
            MoveTo(best)
        }
    }

    inner class SinglePlayer(context: Context?, blockNumber: Int) : SurfaceView(context),
        View.OnTouchListener {


        var callback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                redraw()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                WIDTH = width / (COL + 1)
                redraw()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        }

        init {
            BLOCKS = blockNumber
            holder.addCallback(callback)
            setupmatrix()
            setOnTouchListener(this)
            initGame()

        }

        private fun redraw() {
            val c = holder.lockCanvas()
            screenHeight = c.height
            screenWidth = c.width

            if (state == WIN)
                c.drawColor(Color.GREEN)
            else if (state == LOSE)
                c.drawColor(Color.RED)
            else
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
                            ((screenWidth - ROW * WIDTH) / 4 + one.x * WIDTH + offset).toFloat(),
                            ((screenHeight - COL * WIDTH) / 2 + one.y * WIDTH).toFloat(),
                            ((screenWidth - ROW * WIDTH) / 4 + (one.x + 1) * WIDTH + offset).toFloat(),
                            ((screenHeight - COL * WIDTH) / 2 + (one.y + 1) * WIDTH).toFloat()
                        ), paint
                    )
                }
            }
            holder.unlockCanvasAndPost(c)
        }

        private fun lose() {
            Toast.makeText(context, "Lose!", Toast.LENGTH_SHORT).show()
        }

        private fun win() {
            Toast.makeText(context, "You won!", Toast.LENGTH_SHORT).show()
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {

            if (event.action == MotionEvent.ACTION_UP) {
//            Toast.makeText(getContext(),event.getX()+":"+ event.getY(),Toast.LENGTH_SHORT).show();
                val x: Int
                val y: Int

                y = (event.y.toInt() - ((screenHeight - COL * WIDTH) / 2)) / WIDTH

                // zig-zag like effect
                x = if (y % 2 == 0) {
                    (event.x.toInt() - ((screenWidth - COL * WIDTH) / 4)) / WIDTH
                } else {
                    ((event.x.toInt() - ((screenWidth - COL * WIDTH) / 4)) - WIDTH / 2) / WIDTH
                }
                if (state == LOSE || state == WIN || x + 1 > COL || y + 1 > ROW || x < 0 || y < 0) {
                    initGame()
                } else if (getDot(x, y)!!.status == Dot.STATUS_OFF) {
                    getDot(x, y)!!.status = Dot.STATUS_ON
                    move()
                }
                redraw()
            }
            return true
        }

    }

    inner class MultiPlayer(context: Context?, hostname: String, port: Int) : SurfaceView(context),
        View.OnTouchListener {

        lateinit var input: BufferedReader
        lateinit var output: PrintWriter
        lateinit var socket: Socket

        private var glenda: Dot? = null

        private var WIDTH = 40
        private val COL = 11
        private val ROW = 11

        var waitBit: Boolean = false
        var player: Boolean = false


        var callback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                // redraw()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                WIDTH = width / (COL + 1)
                // redraw()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        }

        init {
            holder.addCallback(callback)
            setOnTouchListener(this)
            setupmatrix()
            connect(hostname, port)
            initGame()

        }

        fun read(): String? {
            var r = input.readLine()

            return r
        }

        fun connect(hostName: String, port: Int) {

            CoroutineScope(Dispatchers.Main).launch {

                //45.94.213.254
                val textFromServer = withContext(Dispatchers.IO) {
                    socket = Socket(hostName, port) //todo fix

                    input = BufferedReader(InputStreamReader(socket.getInputStream()))
                    output = PrintWriter(socket.getOutputStream())

                    while (true) {// reads a line and passes it to parse, the parse passes it to parseinit and then parseinit passes it to parseposition
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
            } else {
                r = input
            }

            when (r) {
                "CONN" -> Log.d("Glendy", r)
                "WAIT" -> {
                    Log.d("Glendy", r)
                    waitBit = true
                }

                "INIT" -> {
                    Log.d("Glendy", r)
                    parseInit()
                }

                "SENT" -> Log.d("Glendy", r)
                "TURN" -> {
                    Log.d("Glendy", r)
                    waitBit = false
                }

                "WALL" -> Log.d("Glendy", r)
                "GLND" -> Log.d("Glendy", r)
                "WON" -> {
                    Log.d("Glendy", r)
                    Toast.makeText(context, "You won", Toast.LENGTH_SHORT).show()
                }

                "LOST" -> {
                    Log.d("Glendy", r)
                    Toast.makeText(context, "Lost", Toast.LENGTH_SHORT).show()
                }

                "ERR" -> Log.d("Glendy", r)
                "DIE" -> Log.d("Glendy", r)

                else -> { //Todo replacement for finish()
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
                    pos = buff.split(" ")[1] + " " + buff.split(" ")[2]
                    buff = buff.split(" ")[0] //?
                }
                // else
                //todo: exit the program here
                when (buff) {
                    "w" -> {
                        Log.d("Glendy", "parseInit(): " + buff)
                        tmpDot = parsePosition(pos)
                        putWall(tmpDot!!.y, tmpDot!!.x)
                    } // pos is the string position of the wall and then parsePosition converts this string to int x and int y
                    "g" -> {
                        Log.d("Glendy", "parseInit(): " + buff)
                        tmpDot = parsePosition(pos)
                        putGlenda(tmpDot!!.y, tmpDot!!.x)
                    }

                    "SENT" -> {
                        redraw()
                        break
                    }

                    else -> {// to do replacement for finish
                        // Toast.makeText(context, "parseInit(): got null", Toast.LENGTH_SHORT)
                        // .show()
                        Log.d("Glendy", "ParseInit(): Odd buff: " + buff)
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

            when (dir) {//?
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
            setupmatrix()
        }

        fun putWall(x: Int, y: Int) { //sets a wall on the phone screen
            if (x < 0 || y < 0) {
                Log.d("Glendy", "putMessage(" + x.toString() + ", " + y.toString() + ")")
                return
            }
            getDot(y, x)!!.status = Dot.STATUS_ON
            //matrix[x][y]!!.status = Dot.STATUS_ON
        }

        fun putGlenda(x: Int, y: Int) { //sets a glenda on the screen
            if (x < 0 || y < 0) {
                Log.d("Glendy", "putMessage(" + x.toString() + ", " + y.toString() + ")")
                return
            }
            if (glenda == null) {//?
                glenda = Dot(x, y)
            } else {
                glenda!!.status = Dot.STATUS_OFF
            }
            getDot(y, x)!!.status = Dot.STATUS_IN
            //glenda!!.setXY(x, y)
        }

        // "xx yy\n"
        fun parsePosition(str: String?): Dot? { //gets a string position from server and changes it to an int position
            var xPos: String
            var yPos: String

            if (str == null) {//?
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
            if (event.action == MotionEvent.ACTION_UP) {
                val x: Int
                val y: Int
                y = event.y.toInt() / WIDTH
                x = if (y % 2 == 0) {
                    (event.x / WIDTH).toInt()
                } else {
                    ((event.x - WIDTH / 2) / WIDTH).toInt()
                }
                if (x + 1 > COL || y + 1 > ROW) {
                    //initGame()
                } else if (getDot(x, y)!!.status == Dot.STATUS_OFF) {
                    getDot(x, y)!!.status = Dot.STATUS_ON
                }
                redraw()
            }
            return true
        }

        fun parseConn(str: String?) {
            if (str == null) {
                Log.d("Glendy", "parseConn(null)")
            }
            TODO()
        }
    }
}



