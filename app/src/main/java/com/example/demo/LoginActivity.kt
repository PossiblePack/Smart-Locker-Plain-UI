package com.example.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class LoginActivity : AppCompatActivity() {

    var cardview1 : CardView? = null
    var test : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardview1 = findViewById<CardView>(R.id.cardView1)
        test = findViewById<Button>(R.id.btnTestbutton)

        cardview1!!.setOnClickListener{
            val intent = Intent(this,DeviceListActivity::class.java)
            intent.putExtra("lockerName","001")
            startActivity(intent)
        }
        test!!.setOnClickListener{
            val intent = Intent(this,TestActivity::class.java)
            startActivity(intent)
        }
    }
}