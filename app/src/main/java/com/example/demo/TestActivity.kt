package com.example.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class TestActivity : AppCompatActivity() {
    var btnTestBT : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        btnTestBT = findViewById<Button>(R.id.btnTestBT)
        btnTestBT!!.setOnClickListener {
            val intent = Intent(this,BluetoothDeviceActivity::class.java)
            startActivity(intent)
        }
    }
}