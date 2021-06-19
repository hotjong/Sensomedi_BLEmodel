package com.example.blemodel

import android.Manifest

class Constants {
    companion object{
        const val REQUEST_ALL_PERMISSION = 2
        val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        //Arduino UUID
        const val SERVICE_STRING = "0000FFE0-0000-1000-8000-00805F9B34FB"
        const val CHARACTERISTIC_RESPONSE_STRING = "0000FFE1-0000-1000-8000-00805F9B34FB"

        const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
        const val iBeaconUUID = "000002A01-0000-1000-8000-00805F9B34FB"
    }
}