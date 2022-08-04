package com.example.demo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.demo.libs.Model.BoxController
import com.example.demo.libs.Model.StatusEventArgs

class LoginActivity : AppCompatActivity() {

    var cardview1 : CardView? = null
    var test : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardview1 = findViewById<CardView>(R.id.cardView1)
        test = findViewById<Button>(R.id.btnTestbutton)

        cardview1!!.setOnClickListener{
            var  devicelistactivityIntent : Intent? = Intent(this, DeviceListActivity::class.java)
            startActivity(devicelistactivityIntent)
        }
        test!!.setOnClickListener{
            val intent = Intent(this,TestActivity::class.java)
            startActivity(intent)
        }
    }
}