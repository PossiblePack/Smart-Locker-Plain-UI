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
            var  devicelistactivityIntent : Intent? = Intent(
                this,
                DeviceListActivity::class.java
            )
            startActivityForResult(devicelistactivityIntent, MainActivity.REQUEST_CONNECTDEVICE)
        }
        test!!.setOnClickListener{
            val intent = Intent(this,TestActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode== MainActivity.REQUEST_CONNECTDEVICE) {
            var i = 0
            while (i < MainActivity.max_device_num) {
                if (DeviceListActivity.mTargetDevice[i] != null) {
                    if (MainActivity.isConnect[i] != true) {
                        DeviceListActivity.mTargetDevice[i].box.OnDoorChange.addListener("DoorChangeEventHandler",
                            BoxController.StatusEventHandler { sender, e ->
                                DoorChangeEventHandler(
                                    sender,
                                    e
                                )
                            })
                        DeviceListActivity.mTargetDevice[i].box.OnLockChange.addListener("LockChangeEventHandler",
                            BoxController.StatusEventHandler { sender, e ->
                                LockChangeEventHandler(
                                    sender,
                                    e
                                )
                            })
                        DeviceListActivity.mTargetDevice[i].box.OnConnectionChange.addListener("ConnectionChangeEventHandler",
                            BoxController.StatusEventHandler { sender, e ->
                                ConnectionChangeEventHandler(
                                    sender,
                                    e
                                )
                            })
                    }
                    if (i == MainActivity.select_device_no) {
//                            buttonUpdateAll()
                    }
                } else {
                }
                i++
            }
            if (MainActivity.select_device_no >= 0) {
                (findViewById<View>(R.id.textview_hardwareDeviceCode) as TextView).text =
                    MainActivity.HardwareDeviceCode[MainActivity.select_device_no]
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun ConnectionChangeEventHandler(sender: BoxController, e: StatusEventArgs) {
        for (i in 0 until MainActivity.max_device_num) {
            if (MainActivity.HardwareDeviceCode[i] == sender.hardwareDeviceCode) {
                if (e.status == true) {
                    // Disconnect
                    MainActivity.isConnect[i] = false
                    if (MainActivity.select_device_no == i) {
                        mButtonConnectEnableChangeHandler.sendEmptyMessage(1)
                    }
                } else {
                    // None
                }
                break
            }
        }
    }

    val mButtonConnectEnableChangeHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 1) {
                MainActivity.isConnect[MainActivity.select_device_no] = false
            } else {
                MainActivity.isConnect[MainActivity.select_device_no] = true
            }
//            buttonUpdateAll()
        }
    }

    fun DoorChangeEventHandler(sender: BoxController?, e: StatusEventArgs) {
        if (e.status === true) {
            // OPEN
        } else {
            // Close
        }
    }

    fun LockChangeEventHandler(sender: BoxController, e: StatusEventArgs) {
        for (i in 0 until MainActivity.max_device_num) {
            if (MainActivity.HardwareDeviceCode[i] == sender.hardwareDeviceCode) {
                MainActivity.isLockUnknown[i] = false
                if (e.status === true) {
                    // Lock
                    MainActivity.isLocked[i] = true
                    if (MainActivity.select_device_no == i) {
                        mButtonUnlockEnableChangeHandler.sendEmptyMessage(MainActivity.TRUE)
                    }
                } else {
                    // Unlock
                    MainActivity.isLocked[i] = false
                    if (MainActivity.select_device_no == i) {
                        mButtonLockEnableChangeHandler.sendEmptyMessage(MainActivity.TRUE)
                    }
                }
            }
        }
    }

    val mButtonUnlockEnableChangeHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 1) {
                MainActivity.isLocked[MainActivity.select_device_no] = true
            } else {
                MainActivity.isLocked[MainActivity.select_device_no] = false
            }
            //buttonUpdateAll()
        }
    }

    val mButtonLockEnableChangeHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 1) {
                MainActivity.isLocked[MainActivity.select_device_no] = false
            } else {
                MainActivity.isLocked[MainActivity.select_device_no] = true
            }
            //buttonUpdateAll()
        }
    }
}