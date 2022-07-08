package com.example.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class Test : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }

    fun TestUserOnclick(view: View){
        var i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }

    fun TestDeviceOnclick(view: View){
        var i = Intent(this, UserDetailActivity::class.java)
        startActivity(i)
    }

    fun TestDeviceListOnclick(view: View){
        var i = Intent(this, LockerDetailActivity::class.java)
        startActivity(i)
    }
}