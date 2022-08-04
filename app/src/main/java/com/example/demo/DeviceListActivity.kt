package com.example.demo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.demo.MainActivity.Companion.HardwareDeviceCode
import com.example.demo.MainActivity.Companion.aeskey
import com.example.demo.MainActivity.Companion.isLockUnknown
import com.example.demo.MainActivity.Companion.mAesKey
import com.example.demo.MainActivity.Companion.mIvKey
import com.example.demo.MainActivity.Companion.TargetDevice
import com.example.demo.libs.Model.*
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList

class DeviceListActivity : AppCompatActivity(){

    //create member
    private var txtHardwareDeviceCode: TextView? = null
    private var cvDevice: CardView? = null
    private var target: DiscoverEventArgs? = null
    var bleDevice: BleDevice? = null
    var bleAccess: BleAccess? = null
    var device: BluetoothDevice? = null
    private val bluetoothAdapter: BluetoothAdapter by lazy{
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_device)

        //get GUI
        cvDevice = findViewById(R.id.cvDevice1)
        txtHardwareDeviceCode = findViewById<TextView>(R.id.HardwareDeviceCode)
        HardwareDeviceCode = txtHardwareDeviceCode!!.text.toString()

        //Create Aarray list
        TargetDevice = ArrayList<DiscoverEventArgs>()

        //set from constructor
        getAeskey()
        getDevice()
        addDevice()

        //set card view listener
        cvDevice!!.setOnClickListener{
            Connect()
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
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
    }

    private fun getDevice() {
        //set bluetooth device
        bleDevice = BleDevice()
        bleAccess = BleAccess(applicationContext)
        device = bluetoothAdapter.getRemoteDevice("08:6B:D7:16:B2:AD")
        bleDevice!!._Device = device
        target = DiscoverEventArgs(bleAccess,bleDevice)

        //add bluetooth device to array list
        TargetDevice!!.add(target!!)
    }

    private fun Connect(){
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
                val key = SecretKeySpec(mAesKey[0], "AES")
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
                TargetDevice!![0].box.Connect(cipheredToken, hashToken)
                Log.e("Connect status", "device is connect")
            } catch (e: BoxException) {
                Log.e("BoxException", e.message.toString())
            } catch (e: Exception) {
                Log.e("Exception", e.message.toString())
            }
    }

    fun addDevice() {
        // IV Key
        val tmp = (HardwareDeviceCode!!.replace(":", "") + "00000000").toByteArray()
        var sha256 = byteArrayOf(0)
        try {
            sha256 = MessageDigest.getInstance("SHA-256").digest(tmp)
        } catch (ex: java.lang.Exception) {
            Log.e("java.lang.Exception", ex.message.toString())
        }
        val ivkey = ByteArray(16)
        for (j in 0..15) {
            ivkey[j] = sha256[16 + j]
        }

        //add ivkey to array list
        mIvKey.add(ivkey)
    }

}


