package com.kozminandruxacorp.bluetoothtestv1

import android.bluetooth.BluetoothAdapter

class BtConnection(private val adapter: BluetoothAdapter) {
    lateinit var cThread: ConnectThread

    fun connect(mac: String) {
        if (adapter.isEnabled && mac.isNotEmpty()) {
            val device = adapter.getRemoteDevice(mac)
            device.let {
                cThread = ConnectThread(it)
                cThread.start()
            }
        }
    }

    fun sendMessage(message: String) {
        cThread.rThread.sendMessage(message.toByteArray())
    }
}