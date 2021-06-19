package com.example.blemodel

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import com.example.blemodel.Constants.Companion.CHARACTERISTIC_RESPONSE_STRING
import com.example.blemodel.Constants.Companion.SERVICE_STRING
import java.util.*

class BluetoothUtils {
    companion object {

        // find response characteristic
        fun findResponseCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
            return findCharacteristic(gatt, CHARACTERISTIC_RESPONSE_STRING)
        }

        // find characteristic
        fun findCharacteristic(
            gatt: BluetoothGatt,
            uuidString: String
        ): BluetoothGattCharacteristic? {
            val serviceList = gatt.services
            val service = findGattService(serviceList) ?: return null
            val characteristicList = service.characteristics
            for (characteristic in characteristicList) {
                if (checkCharacteristic(characteristic, uuidString)) {
                    return characteristic
                }
            }
            return null
        }

        // check characteristic with UUID
        fun checkCharacteristic(
            characteristic: BluetoothGattCharacteristic?,
            uuidString: String
        ): Boolean {
            if (characteristic == null) {
                return false
            }
            val uuid: UUID = characteristic.uuid
            return checkUUID(uuid.toString(), uuidString)
        }

        // find Gatt service
        fun findGattService(serviceList: List<BluetoothGattService>): BluetoothGattService? {
            for (service in serviceList) {
                val serviceUuidString = service.uuid.toString()
                if (checkServiceUUID(serviceUuidString)) {
                    return service
                }
            }
            return null
        }

        // check UUID with service UUID that from characteristic
        fun checkServiceUUID(serviceUuidString: String): Boolean {
            return checkUUID(serviceUuidString, SERVICE_STRING)
        }

        // check UUID
        fun checkUUID(uuidString: String, vararg matches: String): Boolean {
            for (match in matches) {
                if (uuidString.equals(match, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }
}