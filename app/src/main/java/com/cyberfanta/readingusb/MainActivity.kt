package com.cyberfanta.readingusb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mjdev.libaums.UsbMassStorageDevice
import com.github.mjdev.libaums.fs.UsbFile
import java.io.IOException
import java.nio.file.FileSystem

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val usbManager = this.getSystemService(USB_SERVICE) as UsbManager

        val ACTION_USB_PERMISSION = "USB_PERMISSION"
        val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action : String? = intent.action
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)

                if (ACTION_USB_PERMISSION == action) {
                    synchronized(this) {

                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            readUSB(usbManager)
                        } else {
                            var string= ""
                            string += "Permission denied for device: $device\n\n"
                            Log.i(TAG, "Permission denied for device: $device\n\n")

                            val tv : TextView = findViewById(R.id.text)
                            tv.text = string
                        }
                    }
                } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                    if (device != null){
                        openUSB (usbManager, device)
                    }
                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                    if (device != null) {
                        closeUSB (usbManager, device)
                    }
                }
            }
        }

        val intentFilter = IntentFilter(ACTION_USB_PERMISSION)
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        this.registerReceiver(broadcastReceiver, intentFilter)

        for (device in usbManager.deviceList.values) {
            if (!usbManager.hasPermission(device)) {
                val permissionIntent = PendingIntent.getBroadcast(
                    this, 0, Intent(
                        ACTION_USB_PERMISSION
                    ), 0
                )
                usbManager.requestPermission(device, permissionIntent)
                continue
            }
        }
    }

    fun readUSB(usbManager: UsbManager){
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

        val tv : TextView = findViewById(R.id.text)
        tv.text = string
    }

    fun openUSB(usbManager: UsbManager, usbDevice: UsbDevice) {
        var bytes: ByteArray?
        val TIMEOUT = 0
        val forceClaim = true

        val usbInterface : UsbInterface =
            usbDevice.getInterface(0)
        val usbEndpoint : UsbEndpoint =
            usbInterface.getEndpoint(0)
        val usbDeviceConnection : UsbDeviceConnection =
            usbManager.openDevice(usbDevice)
        usbDeviceConnection.claimInterface(usbInterface, forceClaim)
//        usbDeviceConnection.bulkTransfer(usbEndpoint, bytes, bytes.size, TIMEOUT)
    }

    fun closeUSB(usbManager: UsbManager, usbDevice: UsbDevice) {
        val usbInterface : UsbInterface =
            usbDevice.getInterface(0)
        val usbDeviceConnection : UsbDeviceConnection =
            usbManager.openDevice(usbDevice)

        val release = usbDeviceConnection.releaseInterface(usbInterface)
        if (!release){
            Log.i(TAG, "Could not release the interface!!!")
        }
        usbDeviceConnection.close()
    }

    fun setupUSB(context: Context) {
        val devices : Array<UsbMassStorageDevice> = UsbMassStorageDevice.getMassStorageDevices(context)

        if (devices.isEmpty()) {
            Log.i(TAG, "No devices found!!!")
            return
        }

        val device : UsbMassStorageDevice = devices[0]

        try {
            device.init()

            val fileSystem : com.github.mjdev.libaums.fs.FileSystem = device.partitions[0].fileSystem
            Log.i(TAG, "Volume label: " + fileSystem.volumeLabel)

            val root : UsbFile = fileSystem.rootDirectory
            val content : Array<String> = root.list()

            for (string in content) {
                Log.i(TAG, string)
            }
        } catch (e : IOException) {
            Log.i(TAG, "Error setting up device ", e)
        }
    }

//    fun
}
