package com.amir.test

/**
 * Created by Fernando on 15/8/30.
 */
class Dot(@JvmField var x: Int, @JvmField var y: Int) {
    @JvmField
    var status: Int

    init {
        status = STATUS_OFF
    }

    fun setXY(x: Int, y: Int) {
        this.y = y
        this.x = x
    }

    companion object {
        const val STATUS_ON = 1
        const val STATUS_OFF = 0
        const val STATUS_IN = 9
    }

}
