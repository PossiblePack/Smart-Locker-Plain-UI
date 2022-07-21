package com.example.demo

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.demo.libs.Model.BleAccess
import com.example.demo.libs.Model.BoxManager
import java.util.*

class BluetoothDeviceActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter by lazy{
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    var status: TextView? = null
    var listpaired: TextView? = null
    var listscan: TextView? = null
    var onbtn: Button? = null
    var offbtn: Button? = null
    var pairedbtn: Button? = null
    var scanbtn: Button? = null

    private val REQUEST_CODE_ENABLE_BT: Int = 1
    private var scanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    //private val leDeviceListAdapter = LeDeviceListAdapter()

    class LeDeviceListAdapter {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_device)
        //actionBar!!.title = "Bluetooth"

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported.", Toast.LENGTH_SHORT).show()
            finish()
        }

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "error_bluetooth_not_supported", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        //find view
        status = findViewById<TextView>(R.id.txtBTstatus)
        listpaired = findViewById<TextView>(R.id.pairList)
        listscan = findViewById<TextView>(R.id.scanList)
        onbtn = findViewById<Button>(R.id.btnBTon)
        offbtn = findViewById<Button>(R.id.btnBToff)
        pairedbtn = findViewById<Button>(R.id.btnBTpair)
        scanbtn = findViewById<Button>(R.id.btnBTscan)

        if(bluetoothAdapter.isEnabled){
            status!!.text = "On"
        }else{
            status!!.text = "Off"
        }

        onbtn!!.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                Toast.makeText(this, "Bluetooth is already on", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, REQUEST_CODE_ENABLE_BT)
            }
        }

        offbtn!!.setOnClickListener {
            if (!bluetoothAdapter.isEnabled) {
                Toast.makeText(this, "Bluetooth is already off", Toast.LENGTH_SHORT).show()
            } else {
                if (ActivityCompat.checkSelfPermission(                ///////////permission check//////////////
                        this,                                    /////////////////////////////////////////
                        Manifest.permission.BLUETOOTH_CONNECT          /////////////////////////////////////////
                    ) != PackageManager.PERMISSION_GRANTED             /////////////////////////////////////////
                ) {                                                    /////////////////////////////////////////
                }                                                      ///////////permission check//////////////
                bluetoothAdapter.disable()
                Toast.makeText(this, "Bluetooth is off", Toast.LENGTH_SHORT).show()
                status!!.text = "Off"
            }
        }

        pairedbtn!!.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                listpaired!!.text = "Paired Devices"
                val devices = bluetoothAdapter.bondedDevices
                for (device in devices){
                    val deviceName = device.name
                    val deviceAddress = device.uuids
                    listpaired!!.append("\nDevice: Name: $deviceName, $deviceAddress")
                }
            } else {
                Toast.makeText(this, "Please turn on Bluetooth first", Toast.LENGTH_SHORT).show()
            }
        }

        scanbtn!!.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                Toast.makeText(this, "Start scan ", Toast.LENGTH_SHORT).show()
//                scanLeDevice()

            } else {
                Toast.makeText(this, "Please turn on Bluetooth first", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == REQUEST_CODE_ENABLE_BT){
            if (resultCode == Activity.RESULT_OK){
                Toast.makeText(this, "Bluetooth is on", Toast.LENGTH_SHORT).show()
                status!!.text = "On"
            }
            else{
                Toast.makeText(this, "Could not on Bluetooth", Toast.LENGTH_SHORT).show()
            }

        super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun scanLeDevice() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                }
                bluetoothAdapter.bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothAdapter.bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothAdapter.bluetoothLeScanner.stopScan(leScanCallback)
        }
    }


    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val devices = result?.device

            //leDeviceListAdapter.notifyDataSetChanged()
            //Log.e("onScanResult:", "${device}")
            val device = result.device
            val rssi = result.rssi
            val scanRecord = result.scanRecord!!.bytes
            BleAccess.onScanSub(device, rssi, scanRecord)
            //var response = BleAccess.onScanSub(device, rssi, scanRecord)
        }
    }



}