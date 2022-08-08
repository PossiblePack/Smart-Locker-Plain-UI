package com.example.demo

import android.content.DialogInterface
import android.os.Bundle
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

        // Status
        var isConnect : Boolean? = null
        var isLocked : Boolean? = null
    }

    var txtHardwareDeviceCode : TextView? = null
    var txtGetBatteryStatus: TextView? = null
    var btnLock : Button? = null
    var btnUnlock : Button? = null

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
        setContentView(R.layout.activity_lock_unlock)

        txtHardwareDeviceCode = findViewById<TextView>(R.id.txtDeviceKey)
        txtGetBatteryStatus = findViewById<TextView>(R.id.txtBatteryStatus)
        btnLock = findViewById<Button>(R.id.btnLock)
        btnUnlock = findViewById<Button>(R.id.btnUnlock)

        txtHardwareDeviceCode!!.text = HardwareDeviceCode
        GetBatteryStatus()

        btnLock!!.setOnClickListener {
            Lock()
        }
        btnUnlock!!.setOnClickListener {
            Unlock()
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
        GetBatteryStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
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
                isLocked = false
                Toast.makeText(this, "device is unlocked!" , Toast.LENGTH_SHORT).show()
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
                target!!.box.Lock()
                isLocked = true
                Log.e("Locked status", isLocked.toString())
                Log.e("Lock status", "Device is locked")
                Toast.makeText(this, "device is locked!" , Toast.LENGTH_SHORT).show()
            } catch (e: BoxException) {
                Log.e("BoxException", e.message.toString())
                ShowAlertDialogue(View(this), "Lock device failed", e.message.toString() )
            } catch (e: java.lang.Exception) {
                Log.e("java.lang.Exception", e.message.toString())
                ShowAlertDialogue(View(this), "Lock device failed", e.message.toString() )
            }
    }

    private fun Disconnect() {
            try {
                target!!.box.Disconnect()
                isConnect = false
                Log.e("Connect status", isConnect.toString())
                Log.e("Device status", "Device is disconnected")
                Toast.makeText(this, "The device ${HardwareDeviceCode} is disconnected!" , Toast.LENGTH_SHORT).show()
            } catch (e: BoxException) {
                Log.e("BoxException", e.message.toString())
                ShowAlertDialogue(View(this), "Disconnect device failed", e.message.toString() )
            } catch (e: java.lang.Exception) {
                Log.e("java.lang.Exception", e.message.toString())
                ShowAlertDialogue(View(this), "Disconnect device failed", e.message.toString() )
            }
    }

}