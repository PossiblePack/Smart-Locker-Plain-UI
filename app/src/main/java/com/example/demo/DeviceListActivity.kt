package com.example.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.cardview.widget.CardView

class DeviceListActivity : AppCompatActivity() {
    var cardview1 : CardView? = null
    var DeviceKeytext : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_device)

        cardview1 = findViewById<CardView>(R.id.cardView1)
        DeviceKeytext = findViewById<TextView>(R.id.HardwareDeviceCode)
        cardview1!!.setOnClickListener{
            val intent = Intent(this,DeviceDetailActivity::class.java)
            intent.putExtra("HardwareDeviceCode",DeviceKeytext!!.text.toString())
            startActivity(intent)
        }

//        if (mButton_Connect.getId() === v.id) {
//            connect()
//            return
//        }
//        if (mButton_Disconnect.getId() === v.id) {
//            disconnect()
//            return
//        }
    }

}