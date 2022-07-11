package com.example.demo

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DeviceDetailActivity : AppCompatActivity() {

    // member
    //private val mConnectThread: ConnectThread? = null
    private var mDisconnectThread: DisconnectThread? = null

    private var mUnlockThread:UnlockThread? = null
    private var mLockThread: LockThread? = null
    //private val mGetBatteryStatusThread: GetBatteryStatusThread? = null

    // constant
    private val REQUEST_CONNECTDEVICE = 1
    private val TRUE = 1
    private val FALSE = 0
    var HardwareDeviceCode = java.util.ArrayList<String>()

    var select_aes_no = 0
    var select_device_no = -1
    var txtHardwareDeviceCode : TextView? = null
    var btnLock : Button? = null
    var btnUnlock : Button? = null

    // Status
    var isConnect = ArrayList<Boolean>()
    var isLocked = ArrayList<Boolean>()
    var isLockUnknown = ArrayList<Boolean>()
    var isUpdating = ArrayList<Boolean>()
    var isCmdRunning = ArrayList<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        txtHardwareDeviceCode = findViewById<TextView>(R.id.txtDeviceKey)
        var intent = intent
        txtHardwareDeviceCode!!.text = intent.getStringExtra("HardwareDeviceCode")

        btnLock!!.setOnClickListener {

        }
        btnUnlock!!.setOnClickListener {

        }
    }

    fun onClick(v: View) {
        if (btnUnlock!!.id === v.id) {
            //unlock()
            return
        }
        if (btnLock!!.id === v.id) {
            //lock()
            return
        }
    }

    private fun buttonUpdateAll() {
        if (isConnect[select_device_no]) {
            if (isLockUnknown[select_device_no]) {
                btnUnlock!!.isEnabled = true
                btnLock!!.isEnabled = true
            } else if (isLocked[select_device_no]) {
                btnUnlock!!.isEnabled = true
                btnLock!!.isEnabled = false
            } else {
                btnUnlock!!.isEnabled = false
                btnLock!!.isEnabled = true
            }
        } else {
            btnUnlock!!.isEnabled = false
            btnLock!!.isEnabled = false
        }
    }
    private fun unlock() {
        isCmdRunning.set(
            select_device_no,
            true
        )
        mUnlockThread = UnlockThread()
    }

    private fun lock() {
        isCmdRunning.set(
            select_device_no,
            true
        )
        mLockThread = LockThread()
    }

    private fun disconnect() {
        if (select_device_no >= HardwareDeviceCode.size) {
            return
        }
        isConnect.set(
            select_device_no,
            false
        )
        isCmdRunning.set(
            select_device_no,
            true
        )
        mDisconnectThread = DisconnectThread()
    }

    class DisconnectThread {

    }

//    private fun unlock() {
//        isCmdRunning.set(
//            select_device_no,
//            true
//        )
//        mUnlockThread = UnlockThread()
//    }

    class UnlockThread {

    }

    class LockThread {

    }

}


