package com.example.demo

import android.os.StrictMode
import android.util.Log
import java.lang.Exception
import java.sql.*

class ConnectionClass {
    private val ip = "192.168.228.93::1444"
    private  val db = "dbo.tbt_DeviceMapping"
    private  val username = "user"
    private  val password = "password"

    fun dbConnect() : Connection? {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        var conn: Connection? = null
        var connString: String? = null
        try {
            Class.forName("net.sourceforge.jtds.jdbc.driver")
            connString =
                "jdbc:jtds:sqlserver://$ip;databaseName=$db;user=$username;password=$password"
            conn = DriverManager.getConnection(connString)
        } catch (ex: SQLException) {
            ex.message?.let { Log.e("Error : ", it) }
        } catch (ex1: ClassNotFoundException) {
            ex1.message?.let { Log.e("Error : ", it) }
        } catch (ex2: Exception) {
            ex2.message?.let { Log.e("Error : ", it) }
        }
        return  conn
    }
}