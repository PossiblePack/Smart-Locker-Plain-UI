package com.example.demo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.demo.MainActivity.Companion
import com.example.demo.MainActivity.Companion.HardwareDeviceCode
import com.example.demo.MainActivity.Companion.aeskey
import com.example.demo.MainActivity.Companion.autoLockTime
import com.example.demo.MainActivity.Companion.handler
import com.example.demo.MainActivity.Companion.isLocked
import com.example.demo.MainActivity.Companion.isRunnning
import com.example.demo.MainActivity.Companion.mAesKey
import com.example.demo.MainActivity.Companion.mIvKey
import com.example.demo.MainActivity.Companion.target
import com.example.demo.libs.Model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DeviceListActivity : AppCompatActivity(), OnClickListener {

    private var loadingDialogue: Companion.LoadingDialogue? = null
    var errorMSG: String? = null
    var errorTag: String? = null

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

    //initial alert Button
    val positiveButtonClick = { dialog: DialogInterface, which: Int ->
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_device)

        loadingDialogue = Companion.LoadingDialogue(this)
        loadingDialogue!!.startLoadingDialogue()

        //get GUI
        cvDevice = findViewById(R.id.cvDevice1)
        cvDevice?.setOnClickListener(this)

        txtHardwareDeviceCode = findViewById<TextView>(R.id.HardwareDeviceCode)
        txtLockStatus = findViewById<TextView>(R.id.txtLockStatus)

        //Set $HardwareDeviceCode from $txtHardwareDeviceCode in GUI
        HardwareDeviceCode = txtHardwareDeviceCode!!.text.toString()

        //set from constructor
        txtLockStatus!!.text = "Locked"
        GetAeskey()

        handler!!.postDelayed({
            loadingDialogue!!.stopLoadingDialog()
        }, 2000)
    }

    override fun onClick(v: View?) {
        if (cvDevice?.id == v!!.id) {
            SetAndConnectDevice()
        }
    }

    private fun SetAndConnectDevice(){
        CoroutineScope(Main).launch{
            loadingDialogue!!.startLoadingDialogue()
            SetDevice().join()
            ConnectDevice().join()
            loadingDialogue!!.stopLoadingDialog()
            if (errorMSG != null){
                ShowAlertDialogue(View(this@DeviceListActivity), errorTag!!, errorMSG!!)
                ResetErrorTag()
            }
        }
    }

    fun ShowAlertDialogue(view: View, title: String, msg: String ){
        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle(title)
            setMessage(msg)
            setPositiveButton(android.R.string.ok, positiveButtonClick)
            show()
        }
    }

    private fun ResetErrorTag(){
        errorTag = null
        errorMSG = null
    }

    override fun onRestart() {
        super.onRestart()
        if (isLocked==false){
            txtLockStatus!!.text = "Unlocked"
        }else{
            txtLockStatus!!.text = "Locked"
        }
        CoroutineScope(Main).launch {
            loadingDialogue!!.startLoadingDialogue()
            DeviceIsDisconnected().join()
            ResetErrorTag()
            loadingDialogue!!.stopLoadingDialog()
        }
    }

    private fun DeviceIsDisconnected() = CoroutineScope(IO).launch{
        delay(1000)
        while (target!!.advdata != null){
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

    private fun SetDevice() = CoroutineScope(IO).launch{
        Log.e("Set device", "Start")
        try {
            //initial bluetooth
            bleDevice = BleDevice()
            bleAccess = BleAccess(applicationContext)
//            device = bluetoothAdapter.getRemoteDevice("")                               //this null device
//            device = bluetoothAdapter.getRemoteDevice("07:6B:D7:16:B2:AD")              //this incorrect device code
            device = bluetoothAdapter.getRemoteDevice(HardwareDeviceCode)                      //this correct device code
            bleDevice!!._Device = device
            target = DiscoverEventArgs(bleAccess,bleDevice)
            //get ivkey for connect device
            GetDeviceIvkey()
            Log.e("Set device", "Success")
        } catch (e: IllegalArgumentException) {
            Log.e("Set device", "Not success")
            errorTag = "Set device failed!"
            errorMSG = e.message
        } catch (e: BoxException) {
            Log.e("Set device", "Not success")
            errorTag = "Set device failed!"
            errorMSG = e.message
        } catch (e: java.lang.NullPointerException ) {
            Log.e("Set device", "Not success")
            errorTag = "Set device failed!"
            errorMSG = e.message
        }
    }

    private fun GoToDeviceLockUnlockPage(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }

    private fun ConnectDevice() = CoroutineScope(IO).launch{
        try {
            Log.e("Connect device", "Start")
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
            target!!.box.Connect(cipheredToken, hashToken)
            setConfiguration()
            GoToDeviceLockUnlockPage()
            Log.e("Connect device", "Success")
        } catch (e: BoxException) {
            Log.e("Connect device", "Not success")
            errorTag = "Connect device failed!"
            errorMSG = e.message
        } catch (e: Exception) {
            Log.e("Connect device", "Not success")
            errorTag = "Connect device failed!"
            errorMSG = e.message
        }
    }

    private fun setConfiguration(){
        //set configuration
        var config: ByteArray? = byteArrayOf(
            0xFB.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),     //interval=251
            0xE2.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),     //txpow=-30
            0x06.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),     //ch=6
            0x06.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),     //attmpt_max=6
            0x3C.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),     //auto_close=60
            0x14.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),     //input_impossible_time=20
            0x04.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),     //warn_event=4
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),     //conn_tout=0
            0x1E.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()      //password_delete_time=30
        )
        try {
            val cipheredBoxConfig = ByteArray(48)
            val hashConfigByte = ByteArray(4)
            var hashConfig = 0
            if (hashConfigByte != null) {
                val encrypter: Cipher
                val iv = IvParameterSpec(mIvKey[0])
                val key = SecretKeySpec(mAesKey[0], "AES")
                encrypter = Cipher.getInstance("AES/CBC/PKCS5Padding")
                encrypter.init(Cipher.ENCRYPT_MODE, key, iv)
                val cipheredTmp = encrypter.doFinal(config)
                for (i in 0..47) {
                    cipheredBoxConfig[i] = cipheredTmp[i]
                }
                val sha256 = MessageDigest.getInstance("SHA-256").digest(config)
                for (i in 0..3) {
                    hashConfigByte[i] = sha256[28 + i]
                }
                hashConfig = Utility.ToInt32(hashConfigByte, 0)
                target!!.box.SetConfiguration(cipheredBoxConfig, hashConfig);
                autoLockTime = target!!.box.GetConfiguration()._autoCloseTime
                isRunnning = false
            }
            Log.e("Set configuration", "Success")
        } catch (e: BoxException){
            Log.e("Set configuration", "Not success")
            errorTag = "Set configuration failed!"
            errorMSG = e.message
        } catch (e: java.lang.Exception){
            Log.e("Set configuration", "Not success")
            errorTag = "Set configuration failed!"
            errorMSG = e.message
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
        } catch (ex: java.lang.Exception) {
            ShowAlertDialogue(View(this), "Get IV key failed", ex.message.toString() )
        }
    }
}


