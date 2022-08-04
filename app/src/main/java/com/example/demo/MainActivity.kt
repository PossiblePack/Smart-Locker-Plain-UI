package com.example.demo

import android.os.Bundle
import android.os.Message
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
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    companion object {
        var HardwareDeviceCode : String? = null
        var IvKey : ByteArray? = null
        var aeskey : Array<ByteArray>? = null
        var mIvKey = ArrayList<ByteArray>()
        var mAesKey = ArrayList<ByteArray>()
        var mTargetDevice: ArrayList<DiscoverEventArgs>? = null

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

    private var mUnlockThread: UnlockThread? = null

    var txtHardwareDeviceCode : TextView? = null
    var btnLock : Button? = null
    var btnUnlock : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_unlock)

        txtHardwareDeviceCode = findViewById<TextView>(R.id.txtDeviceKey)
        btnLock = findViewById<Button>(R.id.btnLock)
        btnUnlock = findViewById<Button>(R.id.btnUnlock)

        val intent = intent
        txtHardwareDeviceCode!!.text = intent.getStringExtra("HardwareDeviceCode")

        btnLock!!.setOnClickListener {

        }
        btnUnlock!!.setOnClickListener {
            mUnlockThread = UnlockThread()
            mUnlockThread?.start()
        }
    }

    private class UnlockThread : Thread() {
        override fun run() {
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
                        Log.e("iv", iv.toString())
                        val key = SecretKeySpec(mAesKey[0], "AES")
                        Log.e("key", key.toString())
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
                    mTargetDevice?.get(0)!!.box.Unlock(encryptedPassword, hashPassword)
                } else {
                    // Not Set Password
                    mTargetDevice?.get(0)!!.box.Unlock()
                }
            } catch (e: BoxException) {
                isLockUnknown.equals(true)
                val msg = Message()
                msg.obj = e.message
                isCmdRunning.equals(false)
//                mErrorMessageHandler.sendMessage(msg)
            } catch (e: java.lang.Exception) {
                isLockUnknown.equals(true)
                val msg = Message()
                msg.obj = e.message
                isCmdRunning.equals(false)
//                mErrorMessageHandler.sendMessage(msg)
            }
            isCmdRunning.equals(false)
//            mViewUpdateHandler.sendEmptyMessage(MainActivity.TRUE)
        }
    }
    private class LockThread : Thread() {
        override fun run() {
            try {
                mTargetDevice?.get(0)!!.box.Lock()
            } catch (e: BoxException) {
                isLockUnknown.equals(true)
                val msg = Message()
                msg.obj = e.message
                isCmdRunning.equals(false)
//                mErrorMessageHandler.sendMessage(msg)
            } catch (e: java.lang.Exception) {
                isLockUnknown.equals(true)
                val msg = Message()
                msg.obj = e.message
                isCmdRunning.equals(false)
//                mErrorMessageHandler.sendMessage(msg)
            }
            isCmdRunning.equals(false)
//            mViewUpdateHandler.sendEmptyMessage(MainActivity.TRUE)
        }
    }

}