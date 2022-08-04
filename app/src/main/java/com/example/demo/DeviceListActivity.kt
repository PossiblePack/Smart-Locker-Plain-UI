package com.example.demo

import android.app.AlertDialog
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.demo.MainActivity.Companion.HardwareDeviceCode
import com.example.demo.MainActivity.Companion.aeskey
import com.example.demo.MainActivity.Companion.isCmdRunning
import com.example.demo.MainActivity.Companion.isConnect
import com.example.demo.MainActivity.Companion.isLockUnknown
import com.example.demo.MainActivity.Companion.isLocked
import com.example.demo.MainActivity.Companion.isUpdating
import com.example.demo.MainActivity.Companion.mAesKey
import com.example.demo.MainActivity.Companion.mConnectBoxNo
import com.example.demo.MainActivity.Companion.mIvKey
import com.example.demo.MainActivity.Companion.mTargetDevice
import com.example.demo.MainActivity.Companion.retDeleteEvents
import com.example.demo.MainActivity.Companion.retGetBatteryStatus
import com.example.demo.MainActivity.Companion.retGetConfiguration
import com.example.demo.MainActivity.Companion.retGetDateTime
import com.example.demo.MainActivity.Companion.retGetEvents
import com.example.demo.MainActivity.Companion.retGetPassword
import com.example.demo.MainActivity.Companion.retGetStatus
import com.example.demo.MainActivity.Companion.retIsDoorOpened
import com.example.demo.MainActivity.Companion.retIsLocked
import com.example.demo.libs.Model.*
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DeviceListActivity : AppCompatActivity(){

    private var txtHardwareDeviceCode: TextView? = null
    private var cvDevice: CardView? = null
    //private var mConnectThread: ConnectThread? = null
    private var mBoxManager: BoxManager? = null
    private var target: DiscoverEventArgs? = null
    var bleDevice: BleDevice? = null
    var bleAccess: BleAccess? = null
    var device: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_device)

        cvDevice = findViewById(R.id.cvDevice1)
        txtHardwareDeviceCode = findViewById<TextView>(R.id.HardwareDeviceCode)
        HardwareDeviceCode = txtHardwareDeviceCode!!.text.toString()

        getAeskey()
        getDevice()
        addDevice()

        cvDevice!!.setOnClickListener{
//            mConnectThread = ConnectThread()
//            mConnectThread!!.start()
            ConnectThread()
        }
    }

    private fun getAeskey() {
        aeskey = arrayOf(
            byteArrayOf(
                0xDB.toByte(), 0x33.toByte(), 0x62.toByte(), 0x02.toByte(),
                0x53.toByte(), 0xF6.toByte(), 0x48.toByte(), 0xD3.toByte(),
                0xF4.toByte(), 0x02.toByte(), 0x70.toByte(), 0xB4.toByte(),
                0xD2.toByte(), 0xCB.toByte(), 0xDF.toByte(), 0x33.toByte(),
                0xB7.toByte(), 0x50.toByte(), 0x98.toByte(), 0x1C.toByte(),
                0xEC.toByte(), 0xB0.toByte(), 0xE4.toByte(), 0xB1.toByte(),
                0xD5.toByte(), 0xD5.toByte(), 0x22.toByte(), 0x9E.toByte(),
                0x94.toByte(), 0x07.toByte(), 0xB4.toByte(), 0x3F.toByte()
            )
        )
        for (i in aeskey!!.indices) {
            mAesKey.add(aeskey!![i])
        }
        Log.e("Aeskey", mAesKey[0].toString())
    }

    private fun getDevice() {
        //from database
        val adv = byteArrayOf(
            0x00.toByte(), 0x00.toByte(), 0x04.toByte(), 0x00.toByte(),
            0x00.toByte(), 0xAF.toByte(), 0x5F.toByte(), 0x50.toByte(),
            0x53.toByte(), 0x2D.toByte(), 0x4C.toByte(), 0x6F.toByte(),
            0x63.toByte(), 0x6B.toByte(), 0x00.toByte(), 0x20.toByte(),
            0x01.toByte(), 0x06.toByte(), 0x11.toByte(), 0x06.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x08.toByte(), 0x6B.toByte(),
            0xD7.toByte(), 0x16.toByte(), 0xB2.toByte(), 0xAD.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),	0x00.toByte() )
        // set device
        bleDevice = BleDevice()
        bleAccess = BleAccess(applicationContext)

        bleDevice!!._AdvertisedData = adv
        bleDevice!!._DeviceCodeData = "08:6B:D7:16:B2:AD"

        bleDevice!!._Device = device

        target = DiscoverEventArgs(bleAccess,bleDevice)
        Log.e("DiscoverEventArg", target!!.box.toString())
    }



//    private class ConnectThread : Thread() {
//        override fun run() {
//            try {
//                val token = byteArrayOf(
//                    0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
//                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
//                    0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
//                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
//                )
//                var cipheredTmp = ByteArray(32)
//                val cipheredToken = ByteArray(16)
//                val hashTokenByte = ByteArray(4)
//                val iv = IvParameterSpec(mIvKey[0])
//                Log.e("mIvKey", mIvKey[0].toString())
//                val key = SecretKeySpec(mAesKey[0], "AES")
//                Log.e("mAesKey", mAesKey[0].toString())
//                val encrypter: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
//                encrypter.init(Cipher.ENCRYPT_MODE, key, iv)
//                cipheredTmp = encrypter.doFinal(token)
//                for (i in 0..15) {
//                    cipheredToken[i] = cipheredTmp[i]
//                }
//                val sha256 = MessageDigest.getInstance("SHA-256").digest(token)
//                for (i in 0..3) {
//                    hashTokenByte[i] = sha256[28 + i]
//                }
//                var hashToken = 0
//                if (hashTokenByte != null) {
//                    hashToken = Utility.ToInt32(hashTokenByte, 0)
//                }
//                isLockUnknown.equals(true)
//                mTargetDevice!![0].box.Connect(cipheredToken, hashToken)
//                Log.e("Connect status", "device is connect")
//            } catch (e: BoxException) {
//                Log.e("BoxException", e.message.toString())
//                isConnect.equals(false)
//                isCmdRunning.equals(false)
//            } catch (e: Exception) {
//                Log.e("Exception", e.message.toString())
//                isConnect.equals(false)
//                isCmdRunning.equals(false)
//            }
//            isConnect.equals(true)
//            isCmdRunning.equals(false)
//            Log.e("Connect status", isConnect.equals(true).toString())
//            Log.e("Running status", isCmdRunning.equals(false).toString())
//
//        }
//    }

    private fun ConnectThread(){
            try {
                val token = byteArrayOf(
                    0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
                    0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
                )
                var cipheredTmp = ByteArray(32)
                val cipheredToken = ByteArray(16)
                val hashTokenByte = ByteArray(4)
                val iv = IvParameterSpec(mIvKey[0])
                Log.e("mIvKey", mIvKey[0].toString())
                val key = SecretKeySpec(mAesKey[0], "AES")
                Log.e("mAesKey", mAesKey[0].toString())
                val encrypter: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                encrypter.init(Cipher.ENCRYPT_MODE, key, iv)
                cipheredTmp = encrypter.doFinal(token)
                for (i in 0..15) {
                    cipheredToken[i] = cipheredTmp[i]
                }
                val sha256 = MessageDigest.getInstance("SHA-256").digest(token)
                for (i in 0..3) {
                    hashTokenByte[i] = sha256[28 + i]
                }
                var hashToken = 0
                if (hashTokenByte != null) {
                    hashToken = Utility.ToInt32(hashTokenByte, 0)
                }
                isLockUnknown.equals(true)
                target!!.box.Connect(cipheredToken, hashToken)
                Log.e("Connect status", "device is connect")
            } catch (e: BoxException) {
                Log.e("BoxException", e.message.toString())
                isConnect.equals(false)
                isCmdRunning.equals(false)
            } catch (e: Exception) {
                Log.e("Exception", e.message.toString())
                isConnect.equals(false)
                isCmdRunning.equals(false)
            }
            isConnect.equals(true)
            isCmdRunning.equals(false)
            Log.e("Connect status", isConnect.equals(true).toString())
            Log.e("Running status", isCmdRunning.equals(false).toString())
    }

    fun addDevice() {

        // Status
        isConnect.equals(false)
        isLocked.equals(true)
        isLockUnknown.equals(true)
        isUpdating.equals(false)
        isCmdRunning.equals(false)

        // Connect Box Number
        mConnectBoxNo.equals(-1)

        // Get Data
        retGetConfiguration.add(BoxControllerConfig())
        retGetDateTime.add(java.sql.Date(0))
        retGetBatteryStatus.add(0)
        retGetStatus.add(BoxStatus())
        retIsDoorOpened.add(false)
        retIsLocked.add(false)
        retGetEvents.add(EventsInformation())
        retDeleteEvents.add(0)
        retGetPassword.add(Array(10) { ByteArray(16) })

        // IV Key
        val tmp = (HardwareDeviceCode!!.replace(":", "") + "00000000").toByteArray()
        var sha256 = byteArrayOf(0)
        try {
            sha256 = MessageDigest.getInstance("SHA-256").digest(tmp)
        } catch (ex: java.lang.Exception) {
            val msg = Message()
            msg.obj = ex.message
            mErrorMessageHandler.sendMessage(msg)
        }
        val ivkey = ByteArray(16)
        for (j in 0..15) {
            ivkey[j] = sha256[16 + j]
        }
        mIvKey.add(ivkey)
        Log.e("mIvKey", mIvKey.toString())

    }

    // Handler
    val mErrorMessageHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val str = msg.obj as String
            val builder = AlertDialog.Builder(this@DeviceListActivity)
            builder.setTitle("Error")
            builder.setMessage(str)
            builder.setPositiveButton("OK", null)
            builder.show()
        }
    }

}


