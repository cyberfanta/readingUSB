package com.cyberfanta.readingusb

import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val usbManager = this.getSystemService(USB_SERVICE) as UsbManager

        val tv : TextView = findViewById(R.id.text)
        var string= ""
        for (device in usbManager.deviceList.values) {
            string += "Found usb device: $device\n\n"
            Log.i(TAG, "Found usb device: $device\n\n")

            val interfaceCount = device.interfaceCount - 1
            for (i in 0..interfaceCount) {
                val usbInterface : UsbInterface = device.getInterface(i)
                string += "Found usb interface: $usbInterface\n\n"
                Log.i(TAG, "Found usb interface: $usbInterface\n\n")

                val endpointCount = usbInterface.endpointCount - 1
                for (j in 0..endpointCount){
                    val usbEndpoint : UsbEndpoint = usbInterface.getEndpoint(j)
                    string += "Found usb endpoint: $usbEndpoint\n\n"
                    Log.i(TAG, "Found usb endpoint: $usbEndpoint\n\n")

                }

            }
        }
        tv.text = string
    }

}
