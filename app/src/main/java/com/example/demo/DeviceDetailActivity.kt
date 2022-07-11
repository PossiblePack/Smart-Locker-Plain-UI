package com.example.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class DeviceDetailActivity : AppCompatActivity() {

    var txtDeviceKey : TextView? = null
    var btnLock : Button? = null
    var btnUnlock : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        txtDeviceKey = findViewById<TextView>(R.id.txtDeviceKey)
        var intent = intent
        txtDeviceKey!!.text = intent.getStringExtra("HardwareDeviceCode")

        btnLock!!.setOnClickListener {

        }
        btnUnlock!!.setOnClickListener {

        }
    }
}