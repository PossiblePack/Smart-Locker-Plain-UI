package com.example.demo

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.demo.libs.Model.BoxControllerConfig
import com.example.demo.libs.Model.BoxStatus
import com.example.demo.libs.Model.EventsInformation
import java.util.*

class DeviceListActivity : AppCompatActivity() {

    // Status
    var isConnect = ArrayList<Boolean>()
    var isLocked = ArrayList<Boolean>()
    var isLockUnknown = ArrayList<Boolean>()
    var isUpdating = ArrayList<Boolean>()
    var isCmdRunning = ArrayList<Boolean>()

    // Connect Box Number
    var mConnectBoxNo = java.util.ArrayList<Int>()

    // Get Data
    var retGetConfiguration: java.util.ArrayList<BoxControllerConfig> =
        java.util.ArrayList<BoxControllerConfig>()
    var retGetDateTime = java.util.ArrayList<Date>()
    var retGetBatteryStatus = java.util.ArrayList<Int>()
    var retGetStatus: java.util.ArrayList<BoxStatus> = java.util.ArrayList<BoxStatus>()
    var retIsDoorOpened = java.util.ArrayList<Boolean>()
    var retIsLocked = java.util.ArrayList<Boolean>()
    var retGetEvents: java.util.ArrayList<EventsInformation> =
        java.util.ArrayList<EventsInformation>()
    var retDeleteEvents = java.util.ArrayList<Int>()
    var retGetPassword = java.util.ArrayList<Array<ByteArray>>()

    // Key
    var mIvKey = ArrayList<ByteArray>()
    var mAesKey = ArrayList<ByteArray>()

    // Select Adapter
    var adapterSelectAes: ArrayAdapter<*>? = null
    var adapterSelectToken: ArrayAdapter<*>? = null

    var cardview1 : CardView? = null
    var DeviceKeytext : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_device)

        cardview1 = findViewById<CardView>(R.id.cardView1)
        DeviceKeytext = findViewById<TextView>(R.id.HardwareDeviceCode)
        cardview1!!.setOnClickListener{
            val intent = Intent(this,LockUnlockActivity::class.java)
            intent.putExtra("HardwareDeviceCode",DeviceKeytext!!.text.toString())
            startActivity(intent)
        }
    }
}