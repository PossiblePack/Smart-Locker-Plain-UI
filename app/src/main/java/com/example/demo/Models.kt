package com.example.demo

class User(val UserID : Int, val UserName : String,val Password : String,
           val FirstName : String, val LastName : String, val IsActive : Int)
class Devices(val DeviceID : Int, val DeviceName : String,val DeviceKey : String,
              val IsActive : Int  )
class DevicesMapping(val UserID : Int, val DeviceID : Int )