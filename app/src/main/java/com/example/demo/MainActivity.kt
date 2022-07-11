package com.example.demo

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    var cardview1 : CardView? = null
    var cardview2 : CardView? = null
    var cardview3 : CardView? = null
    var test : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardview1 = findViewById<CardView>(R.id.cardView1)
        cardview2 = findViewById<CardView>(R.id.cardView2)
        cardview3 = findViewById<CardView>(R.id.cardView3)
        test = findViewById<Button>(R.id.Testbutton)

        cardview1!!.setOnClickListener{
            val intent = Intent(this,DeviceListActivity::class.java)
            intent.putExtra("lockerName","001")
            startActivity(intent)
        }
        cardview2!!.setOnClickListener{
            val intent = Intent(this,DeviceListActivity::class.java)
            intent.putExtra("lockerName","002")
            startActivity(intent)
        }
        cardview3!!.setOnClickListener{
            val intent = Intent(this,DeviceListActivity::class.java)
            intent.putExtra("lockerName","002")
            startActivity(intent)
        }
        test!!.setOnClickListener{
            val intent = Intent(this,Test::class.java)
            startActivity(intent)
        }

        val mErrorMessageHandler: Handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                val str = msg.obj as String
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Error")
                builder.setMessage(str)
                builder.setPositiveButton("OK", null)
                builder.show()
            }
        }


    }

    companion object {
        lateinit var btsConnect: Thread
        lateinit var mErrorMessageHandler: Handler
    }
}