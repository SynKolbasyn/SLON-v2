package com.kozminandruxacorp.bluetoothtestv1

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ReciveeThread(val bSocket: BluetoothSocket) : Thread() {
    var inStream: InputStream? = null
    var outStream: OutputStream? = null

    init {
        try {
            inStream = bSocket.inputStream
        } catch (i: IOException) {

        }
        try {
            outStream = bSocket.outputStream
        } catch (i: IOException) {

        }
    }

    override fun run() {
        val buf = ByteArray(100)
        while (true) {
            try {
                val size = inStream?.read(buf)
                val message = String(buf, 0, size!!)
                Log.d(TAG, message)
            } catch (i: IOException) {
                break
            }
        }
    }

    fun sendMessage(byteArray: ByteArray) {
        try {
            outStream?.write(byteArray)
        } catch (i: IOException) {

        }
    }
}