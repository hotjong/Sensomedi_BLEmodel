package com.example.blemodel

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.app.ActivityManager
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelUuid
import android.provider.Contacts.Settings.getSetting
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.blemodel.Constants.Companion.CLIENT_CHARACTERISTIC_CONFIG
import com.example.blemodel.Constants.Companion.PERMISSIONS
import com.example.blemodel.Constants.Companion.REQUEST_ALL_PERMISSION
import com.example.blemodel.Constants.Companion.SERVICE_STRING
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {

    // Log name
    private val TAG = "SensoMedi"
    // scan BLE device list
    var scanDeviceList: ArrayList<BluetoothDevice>? = ArrayList()
    // scan BLE device information list
    val dataList = ArrayList<HashMap<String, String>>()
    // BLE Gatt
    private var bluetoothGatt: BluetoothGatt? = null
    // set information from SetFragmnet
    var infoArray: ArrayList<String>? = null
    // color array of Rainbow
    val colorArrayR = arrayOf(
        R.color.color1,
        R.color.color2,
        R.color.color3,
        R.color.color4,
        R.color.color5,
        R.color.color6,
        R.color.color7,
        R.color.color8,
        R.color.color9,
        R.color.color10,
        R.color.color11,
        R.color.color12
    )
    // color array of Natural
    val colorArrayN = arrayOf(
        R.color.color2_1,
        R.color.color2_2,
        R.color.color2_3,
        R.color.color2_4,
        R.color.color2_5,
        R.color.color2_6,
        R.color.color2_7,
        R.color.color2_8,
        R.color.color2_9,
        R.color.color2_10,
        R.color.color2_11,
        R.color.color2_12
    )
    // BLE adapter
    var bluetoothAdapter: BluetoothAdapter? = null

    // When off application, off BLE
    override fun onResume() {
        super.onResume()
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish()
        }
    }
    // check it permissions
    fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
        }

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter


        toggleButton.setOnClickListener {
            if (toggleButton.isChecked) {
                if (bluetoothAdapter == null || !bluetoothAdapter?.isEnabled!!) {
                    makeBLE()
                }

//                run {
//                    Thread.sleep(2000)
//                    startScan()
//                }

            } else {
                bleDeviceList.adapter = null
                bluetoothAdapter?.disable()
            }
        }

        scanButton.setOnClickListener {
            startScan()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
                } else {
                    requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    Toast.makeText(this, "Permissions must be granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
            }
        }
    // Turn on BLE function
    fun makeBLE() {
        val bleIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startForResult.launch(bleIntent)
    }
     // Scanning Arduino Device
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter?.isEnabled!!) {
            showToast("Scanning Failed Check it")
            return
        }

        val filters: MutableList<ScanFilter> = java.util.ArrayList()
        val scanFilter: ScanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID.fromString(SERVICE_STRING)))
            .build()
        filters.add(scanFilter)

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        bluetoothAdapter?.bluetoothLeScanner?.startScan(filters, settings, BLEScanCallback)

        Log.i(TAG, filters.toString() + settings + "")


        Timer("Setting Time", false).schedule(3000) { stopScan() }
    }

    // Stop Scan Arduino Device
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(BLEScanCallback)
        Log.d(TAG, "STOP Bluetooth")
        // Initialize scan Device
        scanDeviceList = ArrayList()
    }

    // BLE ScanCallback
    private val BLEScanCallback: ScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.i(TAG, "Remote device name: " + result?.device?.name)
            addScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            Log.i(TAG, "Batch it : ")
            for (result in results) {
                addScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "BLE scan failed: $errorCode")
        }

        private fun addScanResult(result: ScanResult) {
            val device = result?.device

            for (existDevice in scanDeviceList!!) {
                if (existDevice.address == device.address) return
            }

            showToast(device.name)

            scanDeviceList?.add(result.device)

            // show what device is scanned
            if (scanDeviceList != null) {
                val map = HashMap<String, String>()
                try {
                    map["name"] = scanDeviceList!![0].name
                    map["mac"] = scanDeviceList!![0].toString()
                } catch(e: Exception){
                    e.printStackTrace()
                }
                dataList.add(map)

                val keys = arrayOf("name", "mac")
                val ids = intArrayOf(R.id.rowNameText, R.id.rowMacText)
                val adapter = SimpleAdapter(this@MainActivity, dataList, R.layout.row, keys, ids)
                bleDeviceList.adapter = adapter

                bleDeviceList.setOnItemClickListener { parent, view, position, id ->
                    try {
                        connectDevice(scanDeviceList!![position])
                    } catch (e: java.lang.IndexOutOfBoundsException){

                    }
                }
            }
        }

    }

    // BLE GattCallback
    private val btGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (status == BluetoothGatt.GATT_FAILURE) {
                disconnectGatt()
                return
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGatt()
                return
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT")
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGatt()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Device service is not discovered : $status")
                return
            }
            Log.d(TAG, "Service is discovered!!")

            val responseCharacteristic = gatt?.let { BluetoothUtils.findResponseCharacteristic(it) }
            if (gatt?.let { BluetoothUtils.findResponseCharacteristic(it) } == null) {
                Log.i(TAG, "Found it!")
            }
            if (responseCharacteristic == null) {
                Log.e(TAG, "Unable to find cmd characteristic")
                disconnectGatt()
                return
            }
            gatt.setCharacteristicNotification(responseCharacteristic, true)
            val descriptor: BluetoothGattDescriptor =
                responseCharacteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }

        // When characteristic is changed
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            readCharacteristic(characteristic)
        }

        // When characteristic read
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic read successfully")
                readCharacteristic(characteristic)
            } else {
                Log.e(TAG, "Characteristic read unsuccessfully status, $status")
            }
        }
    }

    // When Arduino device send data, the function read it
    fun readCharacteristic(character: BluetoothGattCharacteristic?) {

        val sensorDevice = infoArray?.get(0)
        val colorMap = infoArray?.get(1)
        val standard = infoArray?.get(2)

        val message = character?.getStringValue(0)
        var splitMsg = message?.split(',')
        var firstValue = splitMsg?.get(0)
        var degree = checkDegree(firstValue!!.toInt(), standard?.toInt() ?: 20)

        Log.d(TAG, "read: $firstValue $degree \n")
        when(colorMap){
            "nature" -> changeColorNature(degree)
            else -> changeColorRainbow(degree)
        }
        logText.append("$message \n")
    }

    // When pressure sensor is pushed, according to push power color change
    fun changeColorRainbow(degree: Int) {
        when (degree) {
            1 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayR[11]))
            }
            2 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayR[10]))
            }
            3 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayR[9]))
            }
            4 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayR[8]))
            }
            5 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayR[7]))
            }
            6 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayR[6]))
            }
            7 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayR[5]))
            }
            8 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayR[4]))
            }
            9 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayR[3]))
            }
            10 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayR[2]))
            }
            11 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayR[1]))
            }
            12 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayR[0]))
            }
        }
    }

    fun changeColorNature(degree: Int) {
        when (degree) {
            1 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayN[11]))
            }
            2 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayN[10]))
            }
            3 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayN[9]))
            }
            4 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayN[8]))
            }
            5 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayN[7]))
            }
            6 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayN[6]))
            }
            7 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayN[5]))
            }
            8 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayN[4]))
            }
            9 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayN[3]))
            }
            10 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayN[2]))
            }
            11 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayN[1]))
            }
            12 -> {
                view1.setBackgroundColor(ContextCompat.getColor(this, colorArrayN[0]))
            }
        }
    }

    // connect device
    fun connectDevice(device: BluetoothDevice?) {
        showToast("Connecting to ${device?.address}")
        bluetoothGatt = device?.connectGatt(this, false, btGattCallback)
    }

    //disconnect Gatt
    fun disconnectGatt() {
        Log.d(TAG, "Off BLE GATT")
        if (bluetoothGatt != null) {
            bluetoothGatt!!.disconnect()
            bluetoothGatt!!.close()
            showToast("Disconnected")
        }
    }

    fun showToast(message: String?) {
        Toast.makeText(this, "$message", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            getSetting(data)
        }
    }

    // get information from SetFragment
    fun getSetting(data: Intent?): ArrayList<String>? {

        val sensorDevice = data?.getStringExtra("sensorDevice")
        val colorMap = data?.getStringExtra("colorMap")
        val numValue = data?.getIntArrayExtra("numValue")
        val rotate = data?.getIntExtra("rotate", 3)

        infoArray!!.add(sensorDevice ?: "Mattress")
        infoArray!!.add(colorMap ?: "rainbow")
        infoArray!!.add(standard(numValue!![0], numValue!![1]).toString())

        return infoArray
    }

    // get standard from max and min input
    fun standard(max: Int, min: Int): Int {
        var minus = max - min
        var standard = minus / 12
        return standard
    }

    // check how much power input is strong
    fun checkDegree(number: Int, standard: Int): Int {
        when (number) {
            in standard * 0..standard * 1 -> return 1
            in standard * 1..standard * 2 -> return 2
            in standard * 2..standard * 3 -> return 3
            in standard * 3..standard * 4 -> return 4
            in standard * 4..standard * 5 -> return 5
            in standard * 5..standard * 6 -> return 6
            in standard * 6..standard * 7 -> return 7
            in standard * 7..standard * 8 -> return 8
            in standard * 8..standard * 9 -> return 9
            in standard * 9..standard * 10 -> return 10
            in standard * 10..standard * 11 -> return 11
            in standard * 11..standard * 20 -> return 12
            else -> return 13
        }
    }

    // setting menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                this.onFragmentChanged(1)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // when it is called, Fragment is changed by input index
    fun onFragmentChanged(index: Int) {
        when (index) {
            1 -> supportFragmentManager.beginTransaction().replace(R.id.container, SetFragment())
                .commit()
        }
    }
}