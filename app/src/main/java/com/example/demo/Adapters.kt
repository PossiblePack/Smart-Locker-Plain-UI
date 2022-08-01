package com.example.demo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class  UserAdapter (val users: ArrayList<User>) : RecyclerView.Adapter<UserAdapter.ViewHolder>()
{
    class ViewHolder (ItemView: View) : RecyclerView.ViewHolder(ItemView){
        val textUserID = itemView.findViewById<TextView>(R.id.userIDText) as TextView
        val textUserName = itemView.findViewById<TextView>(R.id.userNameText) as TextView
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): UserAdapter.ViewHolder{
        val v = LayoutInflater.from(p0.context).inflate(R.layout.activity_main,p0)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return users.size
    }
    override fun onBindViewHolder(p0: UserAdapter.ViewHolder, p1: Int){
        val user : User = users[p1]

        p0.textUserID.text = user.UserID.toString()
        p0.textUserName.text = user.UserName.toString()
    }
}

//class DeviceAdapter (val devices: ArrayList<Devices>) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>()
//{
//    class ViewHolder (ItemView: View) : RecyclerView.ViewHolder(ItemView){
//        val textDeviceID = itemView.findViewById<TextView>(R.id.deviceIDText) as TextView
//        val textDeviceName = itemView.findViewById<TextView>(R.id.HardwareDeviceCode) as TextView
//    }
//
//    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DeviceAdapter.ViewHolder{
//        val v = LayoutInflater.from(p0.context).inflate(R.layout.activity_main,p0)
//        return ViewHolder(v)
//    }
//
//    override fun getItemCount(): Int {
//        return devices.size
//    }
//    override fun onBindViewHolder(p0: DeviceAdapter.ViewHolder, p1: Int){
//        val device : Devices = devices[p1]
//
//        p0.textDeviceName.text = device.DeviceID.toString()
//        p0.textDeviceName.text = device.DeviceName.toString()
//    }
//}