package com.example.demo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.MainActivity.Companion.HardwareDeviceCode
import com.example.demo.MainActivity.Companion.REQUEST_CONNECTDEVICE
import com.example.demo.MainActivity.Companion.adapterSelectToken
import com.example.demo.MainActivity.Companion.isCmdRunning
import com.example.demo.MainActivity.Companion.isConnect
import com.example.demo.MainActivity.Companion.isLockUnknown
import com.example.demo.MainActivity.Companion.isLocked
import com.example.demo.MainActivity.Companion.isUpdating
import com.example.demo.MainActivity.Companion.mConnectBoxNo
import com.example.demo.MainActivity.Companion.mIvKey
import com.example.demo.MainActivity.Companion.max_device_num
import com.example.demo.MainActivity.Companion.retDeleteEvents
import com.example.demo.MainActivity.Companion.retGetBatteryStatus
import com.example.demo.MainActivity.Companion.retGetConfiguration
import com.example.demo.MainActivity.Companion.retGetDateTime
import com.example.demo.MainActivity.Companion.retGetEvents
import com.example.demo.MainActivity.Companion.retGetPassword
import com.example.demo.MainActivity.Companion.retGetStatus
import com.example.demo.MainActivity.Companion.retIsDoorOpened
import com.example.demo.MainActivity.Companion.retIsLocked
import com.example.demo.MainActivity.Companion.select_device_no
import com.example.demo.libs.Model.*
import java.security.MessageDigest

class DeviceListActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    companion object {
        var mTargetDevice = java.util.ArrayList<DiscoverEventArgs>()
    }
    // constant
    val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"

    // member
    private var mDeviceListAdapter: DeviceListAdapter? = null
    private var mScanning = false

    private var mBoxManager: BoxManager? = null

    private var mScanThread: ScanThread? = null

    private var listView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_device)

        setResult(RESULT_CANCELED)

        mBoxManager = BoxManager(applicationContext)
        mBoxManager!!.OnBoxControllerDiscovered.addListener("addDevice",
            BoxManager.DiscoverEventHandler { sender, e -> this.addDevice(sender, e) })

        // ListView setting

//        // ListView setting
//        mDeviceListAdapter = DeviceListAdapter(this)
//        listView = findViewById<View>(R.id.devicelist) as ListView
//        listView?.adapter = mDeviceListAdapter
//        listView?.onItemClickListener = this

    }

    fun addDevice(sender: Any?, e: DiscoverEventArgs) {
        mDeviceListAdapter!!.addDevice(e)
        for (i in 0 until HardwareDeviceCode.size) {
            if (HardwareDeviceCode[i] == e.box._HardwareDeviceCode) {
                mTargetDevice[i] = e
                return
            }
        }
        HardwareDeviceCode.add(e.box._HardwareDeviceCode)

        // Status
        isConnect.add(false)
        isLocked.add(true)
        isLockUnknown.add(true)
        isUpdating.add(false)
        isCmdRunning.add(false)

        // Connect Box Number
        mConnectBoxNo.add(-1)

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

//        // View Data
//        retStringGetConfiguration.add(String(""))
//        retStringGetDateTime.add(String(""))
//        retStringGetBatteryStatus.add(String(""))
//        retStringGetStatus.add(String(""))
//        retStringIsDoorOpened.add(String(""))
//        retStringIsLocked.add(String(""))
//        retStringGetEvents.add(String(""))
//        retStringDeleteEvents.add(String(""))
//        retStringGetPassword.add(String(""))

        // IV Key
        val tmp = (e.box._HardwareDeviceCode.replace(":", "") + "00000000").toByteArray()
        var sha256 = byteArrayOf(0)
        try {
            sha256 = MessageDigest.getInstance("SHA-256").digest(tmp)
        } catch (ex: java.lang.Exception) {
            val msg = Message()
            msg.obj = ex.message
//            mErrorMessageHandler.sendMessage(msg)
        }
        val ivkey = ByteArray(16)
        for (j in 0..15) {
            ivkey[j] = sha256[16 + j]
        }
        mIvKey.add(ivkey)
        adapterSelectToken!!.add(e.box._HardwareDeviceCode as Nothing?)
        max_device_num = HardwareDeviceCode.size
        mTargetDevice.add(e)
    }


    override fun onResume() {
        super.onResume()
//        mScanning = true
//        mScanThread = ScanThread()
//        mScanThread?.start()

    }

    override fun onPause() {
        super.onPause()
        stopScan()
    }

    // StartScan
    private fun startScan() {
        //mDeviceListAdapter!!.clear()
        mScanning = true
//        mScanThread = ScanThread()
//        mScanThread?.start()
        try {
            Log.e("Status", "Start scan")
                mBoxManager?.StartScanBoxControllers()
        } catch (e: BoxException) {
            val msg = Message()
            msg.obj = e.message
                mErrorMessageHandler.sendMessage(msg)
        } catch (e: Exception) {
            val msg = Message()
            msg.obj = e.message
                mErrorMessageHandler.sendMessage(msg)
        }
        invalidateOptionsMenu()
    }

    // StopScan
    private fun stopScan() {
        Log.e("Status", "Stop scan")
        mScanning = false
        mBoxManager!!.StopScanBoxControllers()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuitem_scan -> startScan()
            R.id.menuitem_stop -> onStop()
        }
        return true
    }



    private class ScanThread : Thread() {
        override fun run() {
            try {
                Log.e("Status", "Start scan")
//                mBoxManager.StartScanBoxControllers()
            } catch (e: BoxException) {
                val msg = Message()
                msg.obj = e.message
//                mErrorMessageHandler.sendMessage(msg)
            } catch (e: Exception) {
                val msg = Message()
                msg.obj = e.message
//                mErrorMessageHandler.sendMessage(msg)
            }
        }
    }
    internal class DeviceListAdapter(activity: Activity) : BaseAdapter() {
        private var mDeviceList = ArrayList<DiscoverEventArgs>()
        private var mInflater = activity.layoutInflater

        fun addDevice(device: DiscoverEventArgs) {
            for (i in mDeviceList.indices) {
                if (device.box.hardwareDeviceCode == mDeviceList[i].box.hardwareDeviceCode) {
                    mDeviceList[i] = device
                    notifyDataSetChanged()
                    return
                }
            }
            mDeviceList.add(device)
            notifyDataSetChanged()
        }

        fun clear() {
            mDeviceList.clear()
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return mDeviceList.size
        }

        override fun getItem(position: Int): Any {
            return mDeviceList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        internal class ViewHolder {
            var hardwareDeviceCode: TextView? = null
            var type: TextView? = null
            var firmVersion: TextView? = null
            var remainingBattery: TextView? = null
            var isDoorOpen: TextView? = null
            var isLockLock: TextView? = null
            var isTooMuchEvent: TextView? = null
            var isPasswordSet: TextView? = null
            var passwordNum: TextView? = null
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            var convertView = convertView
            val viewHolder: ViewHolder
            // General ListView optimization code.
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.listitem_device, parent, false)
                viewHolder = ViewHolder()
                viewHolder.hardwareDeviceCode =
                    convertView.findViewById(R.id.textview_hardwareDeviceCode)
                viewHolder.type = convertView.findViewById(R.id.textview_type)
                viewHolder.firmVersion = convertView.findViewById(R.id.textview_firmVersion)
                viewHolder.remainingBattery =
                    convertView.findViewById(R.id.textview_remainingBattery)
                viewHolder.isDoorOpen = convertView.findViewById(R.id.textview_isDoorOpen)
                viewHolder.isLockLock = convertView.findViewById(R.id.textview_isLockLock)
                viewHolder.isTooMuchEvent = convertView.findViewById(R.id.textview_isTooMuchEvent)
                viewHolder.isPasswordSet = convertView.findViewById(R.id.textview_isPasswordSet)
                viewHolder.passwordNum = convertView.findViewById(R.id.textview_passwordNum)
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
            }
            val device = mDeviceList[position]
            viewHolder.hardwareDeviceCode!!.text =
                "hardwareDeviceCode : " + device.box._HardwareDeviceCode
            viewHolder.type!!.text = "type : " + device.box.getAdvertisedMessage()._type.toString()
            val temp = device.box.getAdvertisedMessage()._firmVersion
            var firmVersion = ""
            for (b in temp) {
                firmVersion += String.format("%d.", b)
            }
            firmVersion = firmVersion.substring(0, firmVersion.length - 1)
            viewHolder.firmVersion!!.text = "firmVersion : $firmVersion"
            viewHolder.remainingBattery!!.text =
                "remainingBattery : " + device.box.getAdvertisedMessage()._remainingBattery.toString() + " %"
            viewHolder.isDoorOpen!!.text =
                "isDoorOpen : " + device.box.getAdvertisedMessage()._isDoorOpen.toString()
            viewHolder.isLockLock!!.text =
                "isLockLock : " + device.box.getAdvertisedMessage()._isLockLock.toString()
            viewHolder.isTooMuchEvent!!.text =
                "isTooMuchEvent : " + device.box.getAdvertisedMessage()._isTooMuchEvent.toString()
            viewHolder.isPasswordSet!!.text =
                "isPasswordSet : " + device.box.getAdvertisedMessage()._isPasswordSet.toString()
            viewHolder.passwordNum!!.text =
                "passwordNum : " + device.box.getAdvertisedMessage()._passwordNum.toString()
            return convertView
        }

        init {
            mDeviceList = ArrayList()
            mInflater = activity.layoutInflater
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_device_list, menu)
        menu.findItem(R.id.menuitem_back).isVisible = true
        if (!mScanning) {
            menu.findItem(R.id.menuitem_stop).isVisible = false
            menu.findItem(R.id.menuitem_scan).isVisible = true
            menu.findItem(R.id.menuitem_progress).actionView = null
        } else {
            menu.findItem(R.id.menuitem_stop).isVisible = true
            menu.findItem(R.id.menuitem_scan).isVisible = false
            menu.findItem(R.id.menuitem_progress)
                .setActionView(R.layout.actionbar_indeterminate_progress)
        }
        return true
    }

    fun refreshDevice() {
        for (i in 0 until MainActivity.HardwareDeviceCode.size) {
            var del_flag = true
            if (MainActivity.isConnect.get(i) === true) {
                del_flag = false
            }
            var j = 0
            while (del_flag && j < mDeviceListAdapter!!.count) {
                if (MainActivity.HardwareDeviceCode[i]
                        .equals((mDeviceListAdapter!!.getItem(j) as DiscoverEventArgs).box.hardwareDeviceCode)
                ) {
                    del_flag = false
                    break
                }
                j++
            }
            if (del_flag == true) {
                if (MainActivity.select_device_no > i) {
                    MainActivity.select_device_no--
                } else if (MainActivity.select_device_no === i) {
                    MainActivity.select_device_no = 0
                } else {
                    // Do Nothing
                }
                MainActivity.HardwareDeviceCode.removeAt(i)

                // Status
                MainActivity.isConnect.removeAt(i)
                MainActivity.isLocked.removeAt(i)
                MainActivity.isLockUnknown.removeAt(i)
                MainActivity.isUpdating.removeAt(i)
                MainActivity.isCmdRunning.removeAt(i)

                // Connect Box Number
                MainActivity.mConnectBoxNo.remove(i)

                // Get Data
                MainActivity.retGetConfiguration.removeAt(i)
                MainActivity.retGetDateTime.removeAt(i)
                MainActivity.retGetBatteryStatus.remove(i)
                MainActivity.retGetStatus.removeAt(i)
                MainActivity.retIsDoorOpened.removeAt(i)
                MainActivity.retIsLocked.removeAt(i)
                MainActivity.retGetEvents.removeAt(i)
                MainActivity.retDeleteEvents.remove(i)
                MainActivity.retGetPassword.removeAt(i)

                // View Data
                MainActivity.retStringGetConfiguration.removeAt(i)
                MainActivity.retStringGetDateTime.removeAt(i)
                MainActivity.retStringGetBatteryStatus.removeAt(i)
                MainActivity.retStringGetStatus.removeAt(i)
                MainActivity.retStringIsDoorOpened.removeAt(i)
                MainActivity.retStringIsLocked.removeAt(i)
                MainActivity.retStringGetEvents.removeAt(i)
                MainActivity.retStringDeleteEvents.removeAt(i)
                MainActivity.retStringGetPassword.removeAt(i)

                // IV Key
                MainActivity.mIvKey.removeAt(i)
                MainActivity.adapterSelectToken?.remove(MainActivity.adapterSelectToken?.getItem(i) as Nothing?)
                MainActivity.max_device_num--
                mTargetDevice.removeAt(i)
            }
        }
        if (MainActivity.HardwareDeviceCode.size === 0) {
            MainActivity.select_device_no = -1
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        stopScan()
        refreshDevice()
        for (i in 0 until MainActivity.HardwareDeviceCode.size) {
            if (MainActivity.HardwareDeviceCode[i] == (mDeviceListAdapter!!.getItem(position) as DiscoverEventArgs).box.hardwareDeviceCode) {
                MainActivity.select_device_no = i
                break
            }
        }
        val intent = Intent(this, MainActivity::class.java)
        setResult(RESULT_OK, intent)
    }
}


