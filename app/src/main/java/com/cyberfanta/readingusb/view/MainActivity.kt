package com.cyberfanta.readingusb.view

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cyberfanta.readingusb.R
import com.cyberfanta.readingusb.readUSB.ReadUSB

class MainActivity : AppCompatActivity() {
    private val TAG="MainActivity"
    private lateinit var readUSB : ReadUSB

    //Starting App
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Reading USB
        readUSB = ReadUSB(this)
    }

    //Button Connect
//    @Suppress("UNUSED_PARAMETER")
    fun connect (view: View) {
        readUSB.loadUSB()
    }

    //Button Read
//    @Suppress("UNUSED_PARAMETER")
    fun read(view: View) {
        var textView = findViewById<TextView>(R.id.labeltext)
        textView.text = readUSB.getUSBName()
        textView = findViewById<TextView>(R.id.typetext)
        textView.text = readUSB.getUSBType()
        textView = findViewById<TextView>(R.id.capacitytext)
        textView.text = readUSB.getUSBTotalSpace()
//        textView = findViewById<TextView>(R.id.occupiedtext)
//        textView.text = occupiedSpace.toString()
        textView = findViewById<TextView>(R.id.freetext)
        textView.text = readUSB.getUSBUnallocatedSpace()
//        textView = findViewById<TextView>(R.id.chucktext)
//        textView.text = chunkSize.toString()
    }

    //Button Copy
//    @Suppress("UNUSED_PARAMETER")
    fun copy(view: View) {
        //
    }
    public override fun onDestroy() {
        super.onDestroy()
//        readUSB.unmountUSB()
    }
}