package com.example.demo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.libs.Model.*
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
        var TargetDevice: ArrayList<DiscoverEventArgs>? = null

        // Status
        var isConnect = Boolean
        var isLocked = Boolean
        var isLockUnknown = Boolean
        var isUpdating = Boolean
        var isCmdRunning = Boolean

        // Connect Box Number
        var mConnectBoxNo = Int

        // Get Data
        var retGetConfiguration = java.util.ArrayList<BoxControllerConfig>()
        var retGetDateTime = java.util.ArrayList<Date>()
        var retGetBatteryStatus = java.util.ArrayList<Int>()
        var retGetStatus = java.util.ArrayList<BoxStatus>()
        var retIsDoorOpened = java.util.ArrayList<Boolean>()
        var retIsLocked = java.util.ArrayList<Boolean>()
        var retGetEvents = java.util.ArrayList<EventsInformation>()
        var retDeleteEvents = java.util.ArrayList<Int>()
        var retGetPassword = java.util.ArrayList<Array<ByteArray>>()
    }

    var txtHardwareDeviceCode : TextView? = null
    var txtGetBatteryStatus: TextView? = null
    var btnLock : Button? = null
    var btnUnlock : Button? = null

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

    private fun GetBatteryStatus(){
            try {
                batteryStatus = TargetDevice!![0].box.GetBatteryStatus()
                Log.e("Battery Status", "Now battery status is equal ${batteryStatus}%")
            } catch (e: BoxException) {
                Log.e("BoxException", e.message.toString())
            } catch (e: Exception) {
                Log.e("BoxException", e.message.toString())
            }
            txtGetBatteryStatus!!.text = "$batteryStatus%"
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
                    TargetDevice!![0].box.Unlock(encryptedPassword, hashPassword)
                } else {
                    // Not Set Password
                    TargetDevice!![0].box.Unlock()
                }
                Log.e("Unlock status", "Device is unlocked!")
            } catch (e: BoxException) {
                Log.e("BoxException", e.message.toString())
            } catch (e: java.lang.Exception) {
                Log.e("java.lang.Exception", e.message.toString())
            }
    }

    private fun Lock() {
            try {
                TargetDevice!![0].box.Lock()
                Log.e("Lock status", "Device is locked")
            } catch (e: BoxException) {
                Log.e("BoxException", e.message.toString())
            } catch (e: java.lang.Exception) {
                Log.e("java.lang.Exception", e.message.toString())
            }
    }

}