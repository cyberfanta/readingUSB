package com.cyberfanta.readingusb

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mjdev.libaums.UsbMassStorageDevice
import com.github.mjdev.libaums.UsbMassStorageDevice.Companion.getMassStorageDevices
import com.github.mjdev.libaums.fs.UsbFile
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val TAG="MainActivity"
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    private var volumeLabel = ""
    private var type = 0
    private var capacity = 0.toLong()
    private var occupiedSpace = 0.toLong()
    private var freeSpace = 0.toLong()
    private var chunkSize = 0
    private var root : UsbFile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Buttom Connect
        val button = findViewById<Button>(R.id.button1)
        button.setOnClickListener {
            // Code here executes on main thread after user presses button
            val answer = discoverUSB(this)
            if (answer) {
                ///
                val button1 = findViewById<TextView>(R.id.textView)
                button1.text = getString(R.string.connect)
                button.text = getString(R.string.connected)
            } else {
                AlertDialog.Builder(this)
                        .setTitle("No device found")
                        .setMessage("No device found connected to your phone")
                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.ok)
                        { _, _ ->
                            // Post execution
                        }
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                val button1 = findViewById<TextView>(R.id.textView)
                button1.text = getString(R.string.nodevice)
            }
        }


    }

    private fun discoverUSB (context: Context): Boolean {
        val usbManager = getSystemService(USB_SERVICE) as UsbManager
        val massStorageDevices = getMassStorageDevices(context)
        if (massStorageDevices.isEmpty()) {
            Log.w(TAG, "no device found!")
            return false
        }

        val currentDevice = 0
        val usbDevice = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
        if (usbDevice != null && usbManager.hasPermission(usbDevice)) {
            Log.d(TAG, "received usb device via intent")
            setupDevice(massStorageDevices)
        } else {
            val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            usbManager.requestPermission(massStorageDevices[currentDevice].usbDevice, permissionIntent)
        }
        return true
    }

    private fun setupDevice(massStorageDevices: Array<UsbMassStorageDevice>){
        try {
            val currentDevice = 0
            massStorageDevices[currentDevice].init()

            val currentFs = massStorageDevices[currentDevice].partitions[0].fileSystem.also {
                volumeLabel = it.volumeLabel
                Log.d(TAG, "Capacity: $volumeLabel")
                type = it.type
                Log.d(TAG, "Capacity: $type")
                capacity = it.capacity
                Log.d(TAG, "Capacity: $capacity")
                occupiedSpace = it.occupiedSpace
                Log.d(TAG, "Occupied Space: $occupiedSpace")
                freeSpace = it.freeSpace
                Log.d(TAG, "Free Space: $freeSpace")
                chunkSize = it.chunkSize
                Log.d(TAG, "Chunk Size: $chunkSize")
            }

            root = currentFs.rootDirectory

        } catch (e: IOException) {
            Log.e(TAG, "error setting up device", e)
        }    }
}