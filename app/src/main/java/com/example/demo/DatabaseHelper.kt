package com.example.demo

import android.content.Context
import androidx.recyclerview.widget.RecyclerView

class DatabaseHelper {
    lateinit var ctx: Context

    private var isConnect = false
    private lateinit var  rv : RecyclerView
    private  lateinit var query : String
    private lateinit var adapter : RecyclerView.Adapter<DeviceAdapter>
}