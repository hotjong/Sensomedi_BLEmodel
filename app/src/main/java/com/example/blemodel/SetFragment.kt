package com.example.blemodel

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SimpleAdapter
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_set.*
import kotlinx.android.synthetic.main.fragment_set.view.*
import java.util.HashMap


class SetFragment : Fragment() {

    //device spinner list
    val device_spinner = arrayOf("Mattress", "Glove", "Cushion", "Chair")

    var sensorDevice: String? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_set, container, false)

        val mainActivity = activity as MainActivity?

        val spinner_adapter =
            activity?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, device_spinner) }
        rootView.sensorDeviceList.adapter = spinner_adapter

        rootView.sensorDeviceList.onItemSelectedListener = spinnerListener


        // When start button is clicked, open MainActivity and send data
        rootView.startButton.setOnClickListener {

            val colorMap = checkColor()
            val numValue = getNum()
            val rotate = checkRotate()

            val mainIntent = Intent(mainActivity, MainActivity::class.java)
            mainIntent.putExtra("sensorDevice", sensorDevice ?: "Mattress")
            mainIntent.putExtra("colorMap", colorMap)
            mainIntent.putExtra("numValue",numValue)
            mainIntent.putExtra("rotate",rotate)

            startActivityForResult(mainIntent, 101)


        }

        return rootView

    }

    val spinnerListener = object : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            sensorDevice = sensorDeviceList.getItemAtPosition(position).toString()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            return
        }
    }

    // check which colormap is selected
    fun checkColor(): String {
        if (natural_radio.isChecked) {
            return "natural"
        } else {
            return "rainbow"
        }
    }

    // get max, min numbers
    fun getNum(): Array<Int> {
        var maxNum = maxNum?.text?.toString() ?: "255"
        var minNum = minNum?.text?.toString() ?: "0"
        if(maxNum == ""){
            maxNum = "255"
        }
        if(minNum == ""){
            minNum = "0"
        }

        return arrayOf(maxNum.toInt(), minNum.toInt())
    }

    // check Rotate part is checked
    fun checkRotate() : Int{
        if(lrCheckbox.isChecked && fCheckbox.isChecked){
            return 0
        }
        if(lrCheckbox.isChecked){
            return 1
        }
        if(fCheckbox.isChecked){
            return 2
        }
        return 3
    }
}