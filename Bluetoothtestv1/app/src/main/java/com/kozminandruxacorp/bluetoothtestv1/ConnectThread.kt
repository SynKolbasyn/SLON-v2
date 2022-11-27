package com.kozminandruxacorp.bluetoothtestv1

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.util.*

class ConnectThread(private val device: BluetoothDevice) : Thread() {
    val uuid = "00001101-0000-1000-8000-00805F9B34FB"
    var mSocket: BluetoothSocket? = null
    lateinit var rThread: ReciveeThread

    init {
        try {
            mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
        } catch (i: IOException) {

        }
    }

    override fun run() {
        try {
            Log.d(TAG, "Connecting...")
            mSocket?.connect()
            rThread = ReciveeThread(mSocket!!)
            rThread.start()
            Log.d(TAG, "Connected")
        } catch (i: IOException) {
            Log.d(TAG, "Can not connect to device")
            closeConnection()
        }
    }

    fun closeConnection() {
        try {
            mSocket?.close()
        } catch (i: IOException) {

        }
    }
}