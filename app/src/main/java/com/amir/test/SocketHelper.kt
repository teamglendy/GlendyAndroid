package com.amir.test

import java.io.BufferedReader
import java.io.PrintWriter
import java.net.Socket


object SocketHelper {
    lateinit var input: BufferedReader
    lateinit var output: PrintWriter
    lateinit var socket: Socket
}