package com.example.demo

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.libs.Model.BoxException
import com.example.demo.libs.Model.DiscoverEventArgs
import com.example.demo.libs.Model.Utility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {

    companion object {
        var batteryStatus : Int? = null
        var HardwareDeviceCode : String? = null
        var aeskey : Array<ByteArray>? = null
        var mIvKey = ArrayList<ByteArray>()
        var mAesKey = ArrayList<ByteArray>()
        var target: DiscoverEventArgs? = null
        var autoLockTime: Int? = null
        val handler: Handler? = Handler()
        var runnable: Runnable? = null
        var isCountdown: Boolean? = null
        var isLocked: Boolean? = null
        var isRunnning: Boolean? = null

        class LoadingDialogue(myactivity: Activity) : Thread(){
            val layoutInflater = myactivity.layoutInflater
            val builder = AlertDialog.Builder(myactivity).setView(layoutInflater.inflate(R.layout.loading, null))
                                                         .setCancelable(false)
            val dialog = builder.create()

            fun startLoadingDialogue(){
                Log.e("Loading", "Start loading")
                dialog!!.show()
            }

            fun stopLoadingDialog() {
                dialog!!.dismiss()
                Log.e("Loading", "Stop loading")
            }
        }

    }

    private class CountDownAndCheckAfterUnlock(_autoCloseTime: Int, txtLockStat: TextView?, handler: Handler, runnable: Runnable?, isCountdown: Boolean?) : Thread() {
        val txtLockStat: TextView? = txtLockStat
        private var LockStat : String? = null
        val autoLockTime = (_autoCloseTime - 1)*1000

        override fun run() {
            Log.e("Countdown: ", "Start countdown for ${autoLockTime.toString()} ms")
            handler!!.postDelayed(Runnable {
                handler.postDelayed(runnable!!, autoLockTime.toLong())
                Log.e("Count down:", "Start check")
                if (target!!.box.IsLocked().equals(false)){
                    LockStat = "Unlocked"
                    isCountdown = true
                    Log.e("txtLockStat", LockStat!!)
                }else{
                    LockStat = "Locked"
                    Log.e("txtLockStat", LockStat!!)
                    isCountdown = false
                    txtLockStat!!.text = LockStat!!
                    Log.e("Countdown: ", "Stop countdown")
                    handler.removeCallbacks(runnable!!);
                }
            }.also { runnable = it }, 1000.toLong())
        }
    }

    //initial GUI item
    private var txtHardwareDeviceCode : TextView? = null
    private var txtGetBatteryStatus: TextView? = null
    private var btnLock : Button? = null
    private var btnUnlock : Button? = null
    private var txtLockStat: TextView? = null

    //initial AlertDialogue Button
    val positiveButtonClick = { dialog: DialogInterface, which: Int ->
//        Toast.makeText(applicationContext,
//            R.string.yes, Toast.LENGTH_SHORT).show()
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
        setContentView(R.layout.activity_lock_unlock)

        txtHardwareDeviceCode = findViewById<TextView>(R.id.txtDeviceKey)
        txtGetBatteryStatus = findViewById<TextView>(R.id.txtBatteryStatus)
        btnLock = findViewById<Button>(R.id.btnLock)
        btnUnlock = findViewById<Button>(R.id.btnUnlock)
        txtLockStat = findViewById<TextView>(R.id.txtLockStat)

        CheckLockStatus()

        txtHardwareDeviceCode!!.text = HardwareDeviceCode
        GetBatteryStatus()

        btnLock!!.setOnClickListener {
            Lock()
        }
        btnUnlock!!.setOnClickListener {
            Unlock()
        }
    }

    private fun CheckLockStatus(){
        if (target == null){
            txtLockStat!!.text = "Locked"
        } else {
            if (target!!.box.IsLocked().equals(false)){
                txtLockStat!!.text = "Unlocked"
            }else{
                txtLockStat!!.text = "Locked"
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

    override fun onDestroy() {
        super.onDestroy()
        if (isCountdown == true){
            handler!!.removeCallbacks(runnable!!);
            isLocked = true
        }else{
            isLocked = false
        }
        CheckLockStatus()
        Disconnect()
    }


    private fun GetBatteryStatus(){
            try {
                batteryStatus = target!!.box.GetBatteryStatus()
                Log.e("Battery Status", "Now battery status is equal ${batteryStatus}%")
                txtGetBatteryStatus!!.text = "$batteryStatus%"
            } catch (e: BoxException) {
                ShowAlertDialogue(View(this), "Get battery Status failed", e.message.toString() )
            } catch (e: Exception) {
                ShowAlertDialogue(View(this), "Get battery Status failed", e.message.toString() )
            }
    }

    private fun Unlock() {
            try {
                if(target!!.box.IsLocked().equals(true)){
                    val passwordTmp = Array(10) {
                        ByteArray(
                            1
                        )
                    }
                    val encryptedPassword = Array(10) {
                        ByteArray(
                            16
                        )
                    }
                    val hashPasswordByte = Array(10) {
                        ByteArray(
                            4
                        )
                    }
                    var isSetPassword = true
                    passwordTmp[0] = "0".toByteArray()
                    passwordTmp[1] = "1".toByteArray()
                    passwordTmp[2] = "2".toByteArray()
                    passwordTmp[3] = "3".toByteArray()
                    passwordTmp[4] = "4".toByteArray()
                    passwordTmp[5] = "5".toByteArray()
                    passwordTmp[6] = "6".toByteArray()
                    passwordTmp[7] = "7".toByteArray()
                    passwordTmp[8] = "8".toByteArray()
                    passwordTmp[9] = "9".toByteArray()
                    val hashPassword = arrayOfNulls<Int>(10)
                    if (hashPasswordByte != null) {
                        for (j in 0..9) {
                            if (Arrays.equals(passwordTmp[j], "".toByteArray())) {
                                // Empty password
                                for (i in 0..15) {
                                    encryptedPassword[j][i] = 0
                                }
                                hashPassword[j] = 0
                                continue
                            }
                            val password = ByteArray(16)
                            for (i in 0 until passwordTmp[j].size) {
                                password[i] = passwordTmp[j][i]
                            }
                            var encrypter: Cipher
                            val iv = IvParameterSpec(mIvKey[0])
                            val key = SecretKeySpec(mAesKey[0], "AES")
                            encrypter = Cipher.getInstance("AES/CBC/PKCS5Padding")
                            encrypter.init(Cipher.ENCRYPT_MODE, key, iv)
                            var cipheredTmp = ByteArray(32)
                            cipheredTmp = encrypter.doFinal(password)
                            for (i in 0..15) {
                                encryptedPassword[j][i] = cipheredTmp[i]
                            }
                            val sha256 = MessageDigest.getInstance("SHA-256").digest(
                                passwordTmp[j]
                            )
                            for (i in 0..3) {
                                hashPasswordByte[j][i] = sha256[28 + i]
                            }
                            hashPassword[j] = Utility.ToInt32(hashPasswordByte[j], 0)
                        }
                    }
                    if (isSetPassword == true) {
                        // Set Password
                        target!!.box.Unlock(encryptedPassword, hashPassword)
                    } else {
                        // Not Set
                        target!!.box.Unlock()
                    }
                } else { }  // device is already unlock!
                CheckLockStatus()
                autoLockTime = target!!.box.GetConfiguration()._autoCloseTime
                val countdown =  CountDownAndCheckAfterUnlock(autoLockTime!!, txtLockStat, handler!! , runnable, isCountdown)
                countdown!!.start()
            } catch (e: BoxException) {
                Log.e("BoxException", e.message.toString())
                ShowAlertDialogue(View(this), "Unlock device failed", e.message.toString() )
            } catch (e: java.lang.Exception) {
                Log.e("java.lang.Exception", e.message.toString())
                ShowAlertDialogue(View(this), "Unlock device failed", e.message.toString() )
            }
    }

    private fun Lock() {
            try {
                if(target!!.box.IsLocked().equals(true)){       //device already lock!
                } else {
                    target!!.box.Lock()
                    Log.e("Locked status", target!!.box.IsLocked().toString())
                    Log.e("Lock status", "Device is locked")
                }
                CheckLockStatus()
            } catch (e: BoxException) {
                Log.e("BoxException", e.message.toString())
                ShowAlertDialogue(View(this), "Lock device failed", e.message.toString() )
            } catch (e: java.lang.Exception) {
                Log.e("java.lang.Exception", e.message.toString())
                ShowAlertDialogue(View(this), "Lock device failed", e.message.toString() )
            }
    }

    private fun Disconnect(){
            try {
                target!!.box.Disconnect()
                Log.e("Device status", "Device is disconnected")
            } catch (e: BoxException) {
                Log.e("BoxException", e.message.toString())
                ShowAlertDialogue(View(this), "Disconnect device failed", e.message.toString() )
            } catch (e: java.lang.Exception) {
                Log.e("java.lang.Exception", e.message.toString())
                ShowAlertDialogue(View(this), "Disconnect device failed", e.message.toString() )
            }
    }
}