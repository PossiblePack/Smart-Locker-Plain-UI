package com.example.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.demo.R

class LockUnlockActivity : AppCompatActivity() {

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