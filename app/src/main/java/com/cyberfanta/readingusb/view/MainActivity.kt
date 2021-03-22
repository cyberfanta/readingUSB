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
//import com.github.mjdev.libaums.UsbMassStorageDevice
//import com.github.mjdev.libaums.UsbMassStorageDevice.Companion.getMassStorageDevices
//import com.github.mjdev.libaums.fs.UsbFile
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val TAG="MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Reading USB
        val readUSB = ReadUSB(this)

        //Buttom Connect
        val button = findViewById<Button>(R.id.button1)
        button.setOnClickListener {
            // Code here executes on main thread after user presses button
            val answer = discoverUSB(this)
            if (answer) {
                ///
                val textViewMain = findViewById<TextView>(R.id.textView)
                textViewMain.text = getString(R.string.connect)
                button.text = getString(R.string.connected)
                var textView = findViewById<TextView>(R.id.labeltext)
                textView.text = volumeLabel
                textView = findViewById<TextView>(R.id.typetext)
                textView.text = type.toString()
                textView = findViewById<TextView>(R.id.capacitytext)
                textView.text = capacity.toString()
                textView = findViewById<TextView>(R.id.occupiedtext)
                textView.text = occupiedSpace.toString()
                textView = findViewById<TextView>(R.id.freetext)
                textView.text = freeSpace.toString()
                textView = findViewById<TextView>(R.id.chucktext)
                textView.text = chunkSize.toString()
            } else {
                AlertDialog.Builder(this)
                        .setTitle("No device found")
                        .setMessage("No device found connected to your phone")
                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.ok)
                        { dialog, witch ->
                            // Post execution
                        }
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                val textViewMain = findViewById<TextView>(R.id.textView)
                textViewMain.text = getString(R.string.connect)
            }
        }

        usbReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                val massStorageDevices = getMassStorageDevices(context)
                if (ACTION_USB_PERMISSION == action) {
                    val device = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let {
//                        if (device != null) {
                            setupDevice(massStorageDevices)
                        }
                    }
                } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                    val device = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    Log.d(TAG, "USB device attached")
                    val textViewMain = findViewById<TextView>(R.id.textView)
                    textViewMain.text = getString(R.string.attached)

                    // determine if connected device is a mass storage devuce
                    device?.let {
//                    if (device != null) {
                        discoverUSB(context)
                    }
                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                    val device = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    Log.d(TAG, "USB device detached")
                    val textViewMain = findViewById<TextView>(R.id.textView)
                    textViewMain.text = getString(R.string.detached)

                    // determine if connected device is a mass storage device
                    device?.let {
//                    if (device != null) {
//                        if (currentDevice != -1) {
                            massStorageDevices[0].close()
//                        }
                        // check if there are other devices or set action bar title
                        // to no device if not
                        discoverUSB(context)
                    }
                }
            }
        }

        val filter = IntentFilter(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usbReceiver, filter)
    }

    private fun discoverUSB(context: Context): Boolean {
        val usbManager = getSystemService(USB_SERVICE) as UsbManager
        val massStorageDevices = getMassStorageDevices(context)
        if (massStorageDevices.isEmpty()) {
            Log.w(TAG, "no device found!")
            val textViewMain = findViewById<TextView>(R.id.textView)
            textViewMain.text = getString(R.string.nodevice)
            return false
        }

        val currentDevice = 0
        val usbDevice = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
        if (usbDevice != null && usbManager.hasPermission(usbDevice)) {
            Log.d(TAG, "received usb device via intent")
            setupDevice(massStorageDevices)
        } else {
            Log.d(TAG, "requesting permission to access the usb device")
            val permissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_USB_PERMISSION),
                0
            )
            usbManager.requestPermission(
                massStorageDevices[currentDevice].usbDevice,
                permissionIntent
            )
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
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun root(view: View) {
        val textViewMain = findViewById<TextView>(R.id.textView)
//        textViewMain.text = getString(R.string.detached)
        textViewMain.text = root?.absolutePath ?: ""
    }

    public override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }
}