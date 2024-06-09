package com.amir.test

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import com.amir.test.SocketHelper.input
import com.amir.test.SocketHelper.output
import com.amir.test.SocketHelper.socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.Vector


class Playground() {

    lateinit var best: Dot

    private var state = START

    private var screenHeight = 0
    private var screenWidth = 0

    private var WIDTH = 40
    private var COL = 11
    private var ROW = 11
    private var BLOCKS = 0

    private var glenda: Dot? = null
    private var matrix: Array<Array<Dot?>> = Array(ROW) { arrayOfNulls(COL) }

    private var player: Int = TRAPPER
    private var turn = 0

    companion object {
        const val WIN = 0
        const val LOSE = 1
        const val START = 2
        const val PLAYING = 3

        const val TRAPPER = 0
        const val GLENDA = 1
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
        glenda = Dot(5, 5)
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
        turn = 0
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
    ): Int { //checks the status of glenda's neighbor dot(neighbor dot)
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
            distance++
            if (isAtEdge(next)) {
                return distance
            }
            ori = next
        }
    }

    private fun Moveto(dir: Int) {
        var dst: Dot? = getNeighbor(glenda, dir)

        if (dst == null) {
            return
        }
        MoveTo(dst)
    }

    private fun MoveTo(one: Dot?) {
        //moves the glenda to the new position on the game board (one is the next dot)

        one?.status = Dot.STATUS_IN
        getDot(glenda!!.x, glenda!!.y)!!.status = Dot.STATUS_OFF
        glenda!!.setXY(one!!.x, one!!.y)

        turn++
    }

    private fun move() { //?
        val avaliable = Vector<Dot?>()
        val positive = Vector<Dot?>()
        val al = HashMap<Dot?, Int>()
        for (i in 1..6) {
            val n = getNeighbor(glenda, i)
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
                println("Step forward")
                var min = 999
                for (i in positive.indices) {
                    val a = getDistance(positive[i], al[positive[i]]!!)
                    if (a < min) {
                        min = a
                        best = positive[i]
                    }
                }
            } else { //All have blocks
                println("Avoid Blocks")
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
            if (isAtEdge(glenda))
                state = LOSE
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
            setZOrderOnTop(true) // necessary
            val h = holder
            h.setFormat(PixelFormat.TRANSPARENT)

            val c = holder.lockCanvas()
            screenHeight = c.height
            screenWidth = c.width

            if (state == WIN) {
                c.drawColor(Color.parseColor("#64DD17"))
                win()
            } else if (state == LOSE) {
                c.drawColor(Color.parseColor("#FFCC0000"))
                lose()
            } else
                c.drawColor(0, PorterDuff.Mode.CLEAR)

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
                        Dot.STATUS_OFF -> paint.setColor(Color.parseColor("#90A4AE"))
                        Dot.STATUS_ON -> paint.color = -0x5600
                        Dot.STATUS_IN -> paint.color = -0x10000
                        else -> {}
                    }
                    c.drawOval(
                        RectF(
                            ((screenWidth - COL * WIDTH) / 4 + one.x * WIDTH + offset).toFloat(),
                            ((screenHeight - ROW * WIDTH) / 2 + one.y * WIDTH).toFloat(),
                            ((screenWidth - COL * WIDTH) / 4 + (one.x + 1) * WIDTH + offset).toFloat(),
                            ((screenHeight - ROW * WIDTH) / 2 + (one.y + 1) * WIDTH).toFloat()
                        ), paint
                    )
                }
            }
            holder.unlockCanvasAndPost(c)
        }

        fun lose() {
            Toast.makeText(context, "Lose!", Toast.LENGTH_SHORT).show()
        }

        fun win() {
            Toast.makeText(context, "You won!", Toast.LENGTH_SHORT).show()
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {

            if (event.action == MotionEvent.ACTION_UP) {
//            Toast.makeText(getContext(),event.getX()+":"+ event.getY(),Toast.LENGTH_SHORT).show();
                val x: Int
                val y: Int

                y = (event.y.toInt() - ((screenHeight - ROW * WIDTH) / 2)) / WIDTH

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
                    turn++
                    move()
                }
                redraw()
            }
            return true
        }

    }

    inner class MultiPlayer(
        context: Context?,
        hostname: String,
        port: Int,
        nickName: String,
        game: Int,
        side: Int,
        opts: Int
    ) : SurfaceView(context),
        View.OnTouchListener {

        var waitBit: Boolean = false
        var connected = false

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
            holder.addCallback(callback)
            setOnTouchListener(this)
            setupmatrix()
            connect(hostname, port, nickName, game, side, opts)
            initGame()

        }

        fun read(): String? {
            try {
                var r = input.readLine()
                return r
            } catch (ex: Exception) {
                return null
            }
        }

        fun connect(
            hostName: String,
            port: Int,
            nickName: String,
            game: Int,
            side: Int,
            opts: Int
        ) {

            //45.94.213.254
            val textFromServer = GlobalScope.launch {
                try {
                    socket = Socket(hostName, port) //todo: fix ipv6

                    input = BufferedReader(InputStreamReader(socket.getInputStream()))
                    output = PrintWriter(socket.getOutputStream())
                } catch (ex: Exception) {
                    socket.close()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Check your internet connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                output.printf("%s %d %d %d\n", nickName, game, side, opts)
                output.flush()
                // output.println(nickName + " " + game.toString() + " " + side.toString() + " " + opts.toString());
                while (true) {// reads a line and passes it to parse, the parse passes it to parseinit and then parseinit passes it to parseposition
                    var r = read()
                    if (r == null)
                        break
                    parse(r!!)
                }
            }

            Log.d("Glendy", "Connected to server")

        }

        fun parse(input: String?) {//checks different states
            var r: String
            if (input!!.contains(' ')) {
                r = input.split(" ")[0]
            } else {
                r = input
            }

            Log.d("Glendy", input)
            when (r) {
                "CONN" -> {
                    var side = input.split(" ")[1]
                    if (side == "0") {
                        player = TRAPPER
                    } else if (side == "1") {
                        player = GLENDA
                    }
                }

                "WAIT" -> {
                    waitBit = true
                }

                "INIT" -> {
                    parseInit()
                    connected = true
                }

                "TURN" -> {
                    waitBit = false
                }
                //t is the first word after sync
                //if t is even it is glenda's turn
                //if t is odd it is trapper's turn
                "SYNC" -> {
                    var t = input.split(" ")[1]
                    if (t.toInt() % 2 == GLENDA) {
                        var xPos = input.split(" ")[2]
                        var yPos = input.split(" ")[3]
                        putWall(yPos.toInt(), xPos.toInt())
                        turn++
                    } else if (t.toInt() % 2 == TRAPPER) {
                        var dir = input.split(" ")[2]
                        Moveto(parseDir(dir))
                    } else {

                    }
                    redraw()
                }

                "WON" -> {
                    state = WIN
                    redraw()
                }

                "LOST" -> {
                    state = LOSE
                    redraw()
                }

                "ERR" -> Log.d("Glendy", r)
                "DIE" -> Log.d("Glendy", r)

                "UGUD" -> {
                    output.println("y")
                    output.flush()
                }

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
            Log.d("Glendy", "p " + x.toString() + " " + y.toString())
            GlobalScope.launch {
                output.println("p " + x.toString() + " " + y.toString())
                output.flush()
            }
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
            GlobalScope.launch {
                output.println("m " + str)
                output.flush()
            }
        }

        fun toDir(one: Dot): Int {
            var dst: Dot
            for (i in 1..6) {
                dst = getNeighbor(glenda, i)!!
                if (dst.x == one.x && dst.y == one.y)
                    return i;
            }
            return 0;
        }

        fun initGame() {
            setupmatrix()
        }

        fun putWall(x: Int, y: Int) { //sets a wall on the phone screen
            Log.d("Glendy", "putWall( " + x.toString() + " " + y.toString() + ")")
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
            setZOrderOnTop(true)
            holder.setFormat(PixelFormat.TRANSPARENT)

            val c = holder.lockCanvas()
            if (state == WIN) {
                c.drawColor(Color.parseColor("#64DD17"))
            } else if (state == LOSE) {
                c.drawColor(Color.parseColor("#FFCC0000"))
            } else
                c.drawColor(0, PorterDuff.Mode.CLEAR) //RGBA

            screenWidth = c.width
            screenHeight = c.height


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
                        Dot.STATUS_OFF -> paint.setColor(Color.parseColor("#90A4AE"))
                        Dot.STATUS_ON -> paint.color = -0x5600
                        Dot.STATUS_IN -> paint.color = -0x10000
                        else -> {}
                    }
                    c.drawOval(
                        RectF(
                            ((screenWidth - COL * WIDTH) / 4 + one.x * WIDTH + offset).toFloat(),
                            ((screenHeight - ROW * WIDTH) / 2 + one.y * WIDTH).toFloat(),
                            ((screenWidth - COL * WIDTH) / 4 + (one.x + 1) * WIDTH + offset).toFloat(),
                            ((screenHeight - ROW * WIDTH) / 2 + (one.y + 1) * WIDTH).toFloat()
                        ), paint
                    )

                }
            }
            holder.unlockCanvasAndPost(c)
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (!connected || turn % 2 != player) {
                return true
            }
            if (event.action == MotionEvent.ACTION_UP) {
                val x: Int
                val y: Int

                y = (event.y.toInt() - ((screenHeight - ROW * WIDTH) / 2)) / WIDTH
                x = if (y % 2 == 0) {
                    (event.x.toInt() - ((screenWidth - COL * WIDTH) / 4)) / WIDTH
                } else {
                    ((event.x.toInt() - ((screenWidth - COL * WIDTH) / 4)) - WIDTH / 2) / WIDTH
                }
                if (state == LOSE || state == WIN || x + 1 > COL || y + 1 > ROW || x < 0 || y < 0) {
                    ;
                } else if (player == TRAPPER) {
                    if (getDot(x, y)!!.status == Dot.STATUS_OFF) {
                        putMessage(x, y)
                    }
                } else if (player == GLENDA) {
                    if (getDot(x, y)!!.status == Dot.STATUS_OFF) {
                        val dir: Int = toDir(getDot(x, y)!!)
                        if (dir == 0)
                            return true
                        moveMessage(dir)
                    }
                }
                redraw()
            }

            return true
        }

        fun parseDir(str: String?): Int {
            var i: Int = 0
            Log.d("Glendy", "parseDir(" + str + ")")
            when (str) {//?
                "W" -> i = 1
                "NW" -> i = 2
                "NE" -> i = 3
                "E" -> i = 4
                "SE" -> i = 5
                "SW" -> i = 6
                else -> {}
            }
            return i
        }

        fun parseConn(str: String?) {
            if (str == null) {
                Log.d("Glendy", "parseConn(null)")
            }
            TODO()
        }
    }
}



