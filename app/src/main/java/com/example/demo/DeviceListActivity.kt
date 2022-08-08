package com.example.demo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.demo.MainActivity.Companion.HardwareDeviceCode
import com.example.demo.MainActivity.Companion.aeskey
import com.example.demo.MainActivity.Companion.isConnect
import com.example.demo.MainActivity.Companion.isLocked
import com.example.demo.MainActivity.Companion.mAesKey
import com.example.demo.MainActivity.Companion.mIvKey
import com.example.demo.MainActivity.Companion.target
import com.example.demo.libs.Model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DeviceListActivity : AppCompatActivity(){

    //initial GUI variable
    private var txtHardwareDeviceCode: TextView? = null
    private var txtLockStatus: TextView? = null
    private var cvDevice: CardView? = null

    //initial member variable
    var bleDevice: BleDevice? = null
    var bleAccess: BleAccess? = null
    var device: BluetoothDevice? = null
    private val bluetoothAdapter: BluetoothAdapter by lazy{
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    //initial
    val positiveButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(applicationContext,
            R.string.yes, Toast.LENGTH_SHORT).show()
    }
    val negativeButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(applicationContext,
            R.string.no, Toast.LENGTH_SHORT).show()
    }
    val neutralButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(applicationContext,
            "", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_device)

        //get GUI
        cvDevice = findViewById(R.id.cvDevice1)
        txtHardwareDeviceCode = findViewById<TextView>(R.id.HardwareDeviceCode)
        txtLockStatus = findViewById<TextView>(R.id.txtLockStatus)

        //Set $HardwareDeviceCode from $txtHardwareDeviceCode in GUI
        HardwareDeviceCode = txtHardwareDeviceCode!!.text.toString()

        //set from constructor
        CheckLockStatus()
        GetAeskey()


        //set card view listener
        cvDevice!!.setOnClickListener{
            SetDevice()
            ConnectDevice()
        }
    }

    fun ShowAlertDialogue(view: View, title: String, msg: String ){
        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle(title)
            setMessage(msg)
            setPositiveButton(android.R.string.ok, positiveButtonClick)
//            setPositiveButton("OK", DialogInterface.OnClickListener(function = positiveButtonClick))
//            setNegativeButton(android.R.string.no, negativeButtonClick)
//            setNeutralButton("Maybe", neutralButtonClick)
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        CheckLockStatus()
    }

    private fun CheckLockStatus() {
        if (target == null){
            txtLockStatus!!.text = "Locked"
        } else {
            if (target!!.box.IsLocked()==true){
                txtLockStatus!!.text = "Locked"
            }else{
                txtLockStatus!!.text = "Unlocked"
            }
        }
    }

    private fun GetAeskey() {
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

    private fun SetDevice() {
        //initial bluetooth
        bleDevice = BleDevice()
        bleAccess = BleAccess(applicationContext)

        //set bluetooth device
        try {
//            device = bluetoothAdapter.getRemoteDevice("")                               //this null device
//            device = bluetoothAdapter.getRemoteDevice("07:6B:D7:16:B2:AD")              //this incorrect device code
            device = bluetoothAdapter.getRemoteDevice(HardwareDeviceCode)                      //this correct device code
            bleDevice!!._Device = device
            target = DiscoverEventArgs(bleAccess,bleDevice)

            //get ivkey for connect device
            GetDeviceIvkey()
            Toast.makeText(this, "The device ${HardwareDeviceCode} is set!" , Toast.LENGTH_SHORT).show()
        } catch (e: IllegalArgumentException) {
            ShowAlertDialogue(View(this), "Get device failed", e.message.toString() )
        }
    }

    private fun GoToDeviceLockUnlockPage(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }

    private fun ConnectDevice(){
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
                isConnect = true
                target!!.box.Connect(cipheredToken, hashToken)
                Toast.makeText(this, "The device ${HardwareDeviceCode} is connect" , Toast.LENGTH_SHORT).show()
                GoToDeviceLockUnlockPage()
            } catch (e: BoxException) {
                ShowAlertDialogue(View(this), "Connect device failed", e.message.toString() )
            } catch (e: Exception) {
                ShowAlertDialogue(View(this), "Connect device failed", e.message.toString() )
            }
    }


    fun GetDeviceIvkey() {
        //Get IV Key
        val tmp = (HardwareDeviceCode!!.replace(":", "") + "00000000").toByteArray()
        var sha256 = byteArrayOf(0)
        try {
            sha256 = MessageDigest.getInstance("SHA-256").digest(tmp)
            val ivkey = ByteArray(16)
            for (j in 0..15) {
                ivkey[j] = sha256[16 + j]
            }

            //add ivkey to array list
            mIvKey.add(ivkey)
            Toast.makeText(this, "Get IV key from device ${HardwareDeviceCode} is successfully" , Toast.LENGTH_SHORT).show()
        } catch (ex: java.lang.Exception) {
            ShowAlertDialogue(View(this), "Get IV key failed", ex.message.toString() )
        }
    }
}


