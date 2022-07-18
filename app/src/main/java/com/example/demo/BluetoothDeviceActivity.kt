package com.example.demo

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
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

class BluetoothDeviceActivity : AppCompatActivity() {

    lateinit var bluetoothAdapter: BluetoothAdapter
    var status: TextView? = null
    var listpaired: TextView? = null
    var onbtn: Button? = null
    var offbtn: Button? = null
    var discoverbtn: Button? = null
    var pairbtn: Button? = null
    var scanbtn: Button? = null

//    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
//    private var scanning = false
//    private val handler = Handler()

    //private val leDeviceListAdapter = LeDeviceListAdapter

//    private val SCAN_PERIOD: Long = 10000
    private val REQUEST_CODE_ENABLE_BT: Int = 1
    private val REQUEST_CODE_DISCOVERABLE_BT: Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_device)
        //actionBar!!.title = "Bluetooth"

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported.", Toast.LENGTH_SHORT).show();
            finish();
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "error_bluetooth_not_supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //find view
        status = findViewById<TextView>(R.id.txtBTstatus)
        listpaired = findViewById<TextView>(R.id.pairedList)
        onbtn = findViewById<Button>(R.id.btnBTon)
        offbtn = findViewById<Button>(R.id.btnBToff)
        discoverbtn = findViewById<Button>(R.id.btnBTdiscover)
        pairbtn = findViewById<Button>(R.id.btnBTpair)
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
                status!!.text = "On"
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
                status!!.text = "Off"
                Toast.makeText(this, "Bluetooth is off", Toast.LENGTH_SHORT).show()
            }
        }

        discoverbtn!!.setOnClickListener {
            if (!bluetoothAdapter.isDiscovering) {
                Toast.makeText(this, "Making your device discover", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE))
                startActivityForResult(intent, REQUEST_CODE_DISCOVERABLE_BT)
            }
        }

        pairbtn!!.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                listpaired!!.text = "Paired Devices"
                val devices = bluetoothAdapter.bondedDevices
                for (device in devices){
                    val deviceName = device.name
                    val deviceType = device.type
                    val deviceAddress = device.address
                    listpaired!!.append("\nDevice: Name: $deviceName, Type $deviceType , $deviceAddress")
                }
            } else {
                Toast.makeText(this, "Please turn on Bluetooth first", Toast.LENGTH_SHORT).show()
            }
        }

        scanbtn!!.setOnClickListener {
            if(bluetoothAdapter.isEnabled){

            }else{
                Toast.makeText(this, "Please turn on Bluetooth first", Toast.LENGTH_SHORT).show()
            }
        }


    }

//    private val leScanCallback: ScanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            super.onScanResult(callbackType, result)
//            leDeviceListAdapter.addDevice(result.device)
//            leDeviceListAdapter.notifyDataSetChanged()
//        }
//    }
//
//    private fun scanLeDevice() {
//        if (!scanning) {
//            // Stops scanning after a predefined scan period.
//            handler.postDelayed({
//                scanning = false
//                if (ActivityCompat.checkSelfPermission(
//                        this,
//                        Manifest.permission.BLUETOOTH_SCAN
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                }
//                bluetoothLeScanner.stopScan(leScanCallback)
//            }, SCAN_PERIOD)
//            scanning = true
//            bluetoothLeScanner.startScan(leScanCallback)
//        } else {
//            scanning = false
//            bluetoothLeScanner.stopScan(leScanCallback)
//        }
//    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            REQUEST_CODE_ENABLE_BT ->
                if (resultCode == Activity.RESULT_OK){
                    Toast.makeText(this, "Bluetooth is on", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "Could not on Bluetooth", Toast.LENGTH_SHORT).show()
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


}