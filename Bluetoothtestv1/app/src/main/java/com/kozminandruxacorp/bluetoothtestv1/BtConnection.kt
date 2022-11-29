package com.kozminandruxacorp.bluetoothtestv1

import android.bluetooth.BluetoothAdapter
import android.util.Log

class BtConnection(private val adapter: BluetoothAdapter) {
    lateinit var cThread: ConnectThread
    var btState: Boolean = false
    var flag: Boolean = false

    fun connect(mac: String) {
        if (adapter.isEnabled && mac.isNotEmpty()) {
            val device = adapter.getRemoteDevice(mac)
            device.let {
                cThread = ConnectThread(it)
                cThread.start()
                flag = true
            }
        }
    }

    fun sendMessage(message: String) {
        cThread.rThread.sendMessage(message.toByteArray())
    }

    @JvmName("getBtState1")
    fun getBtState() : Boolean {
        if (flag == true) {
            return cThread.btState()
        }
        else {
            return false
        }
    }
}