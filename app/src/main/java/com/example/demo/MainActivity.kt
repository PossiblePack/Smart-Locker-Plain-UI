package com.example.demo

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.demo.R
import com.example.demo.libs.Model.BoxControllerConfig
import com.example.demo.libs.Model.BoxStatus
import com.example.demo.libs.Model.DiscoverEventArgs
import com.example.demo.libs.Model.EventsInformation
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    companion object {
        var mTargetDevice = java.util.ArrayList<DiscoverEventArgs>()
        /* AES Key Select */ /* 0: default */
        var select_aes_no = 0

        var max_device_num = 0
        var select_device_no = -1

        //        var mTargetDevice = ArrayList<DiscoverEventArgs>()
        var HardwareDeviceCode = ArrayList<String>()

        // Status
        var isConnect = ArrayList<Boolean>()
        var isLocked = ArrayList<Boolean>()
        var isLockUnknown = ArrayList<Boolean>()
        var isUpdating = ArrayList<Boolean>()
        var isCmdRunning = ArrayList<Boolean>()

        // Connect Box Number
        var mConnectBoxNo = ArrayList<Int>()

        // Get Data
        var retGetConfiguration = java.util.ArrayList<BoxControllerConfig>()
        var retGetDateTime = java.util.ArrayList<Date>()
        var retGetBatteryStatus = java.util.ArrayList<Int>()
        var retGetStatus = java.util.ArrayList<BoxStatus>()
        var retIsDoorOpened = java.util.ArrayList<Boolean>()
        var retIsLocked = java.util.ArrayList<Boolean>()
        var retGetEvents = java.util.ArrayList<EventsInformation>()
        var retDeleteEvents = java.util.ArrayList<Int>()
        var retGetPassword = java.util.ArrayList<Array<ByteArray>>()

        // View Data
        var retStringGetConfiguration = ArrayList<String>()
        var retStringGetDateTime = ArrayList<String>()
        var retStringGetBatteryStatus = ArrayList<String>()
        var retStringGetStatus = ArrayList<String>()
        var retStringIsDoorOpened = ArrayList<String>()
        var retStringIsLocked = ArrayList<String>()
        var retStringGetEvents = ArrayList<String>()
        var retStringDeleteEvents = ArrayList<String>()
        var retStringGetPassword = ArrayList<String>()

        // Key
        var mIvKey = ArrayList<ByteArray>()
        var mAesKey = ArrayList<ByteArray>()

        // Select Adapter
        var adapterSelectAes: ArrayAdapter<*>? = null
        var adapterSelectToken: ArrayAdapter<*>? = null

        // Progress Dialog
        var mProgressDialog: ProgressDialog? = null

        // constant
        val REQUEST_CONNECTDEVICE = 1
        val TRUE = 1
        private val FALSE = 0
    }

    var txtHardwareDeviceCode : TextView? = null
    var btnLock : Button? = null
    var btnUnlock : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_unlock)

        txtHardwareDeviceCode = findViewById<TextView>(R.id.txtDeviceKey)
        btnLock = findViewById<Button>(R.id.btnLock)
        btnUnlock = findViewById<Button>(R.id.btnUnlock)

        val intent = intent
        txtHardwareDeviceCode!!.text = intent.getStringExtra("HardwareDeviceCode")

        btnLock!!.setOnClickListener {

        }
        btnUnlock!!.setOnClickListener {

        }

    }
}