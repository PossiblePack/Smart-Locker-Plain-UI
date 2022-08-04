package com.example.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class LoginActivity : AppCompatActivity() {
    var cardview1 : CardView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        cardview1 = findViewById<CardView>(R.id.cardView1)

        cardview1!!.setOnClickListener{
            val  intent = Intent(this, DeviceListActivity::class.java)
            startActivity(intent)
        }
    }
}