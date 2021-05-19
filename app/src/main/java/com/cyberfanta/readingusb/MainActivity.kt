package com.cyberfanta.readingusb

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mjdev.libaums.UsbMassStorageDevice
import com.github.mjdev.libaums.fs.FileSystem
import com.github.mjdev.libaums.fs.UsbFile
import java.io.IOException

@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName
    val ACTION_USB_PERMISSION = "USB_PERMISSION"

    lateinit var mUsbManager : UsbManager
    var mUsbDevice : UsbDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context, intent: Intent) {
                val action : String? = intent.action
                mUsbManager = context.getSystemService(USB_SERVICE) as UsbManager
                mUsbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice?

                mUsbDevice?.let {
                    when {
                        ACTION_USB_PERMISSION == action -> {
                            setupUSB(mUsbManager, mUsbDevice)
                            val tv: TextView = findViewById(R.id.text)
                            tv.text = tv.text.toString() + "\n" + action
                            Log.i(TAG, "$action")
                        }
                        UsbManager.ACTION_USB_DEVICE_ATTACHED == action -> {
                            if (!mUsbManager.hasPermission(mUsbDevice))
                                requestUSB (mUsbManager)
                            val tv: TextView = findViewById(R.id.text)
                            tv.text = tv.text.toString() + "\n" + action
                            Log.i(TAG, "$action")
                        }
                        UsbManager.ACTION_USB_DEVICE_DETACHED == action -> {
//                            if (usbManager.hasPermission(usbDevice))
//                                closeUSB(usbManager, usbDevice)
                            val tv: TextView = findViewById(R.id.text)
                            tv.text = tv.text.toString() + "\n" + action
                            Log.i(TAG, "$action")
                        }
                        else -> {
                        }
                    }
                }
            }
        }

        val intentFilter = IntentFilter(ACTION_USB_PERMISSION)
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(broadcastReceiver, intentFilter)

        val usbManager = getSystemService(USB_SERVICE) as UsbManager
        requestUSB(usbManager)
    }

    private fun requestUSB (usbManager: UsbManager) {
        for (usbDevice in usbManager.deviceList.values) {
            if (!usbManager.hasPermission(usbDevice)) {
                val permissionIntent = PendingIntent.getBroadcast(
                        this, 0, Intent(
                        ACTION_USB_PERMISSION
                ), 0
                )
                usbManager.requestPermission(usbDevice, permissionIntent)
                mUsbDevice = usbDevice
                continue
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun setupUSB (usbManager: UsbManager, usbDevice: UsbDevice?) {
        synchronized(this) {
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, true)) {
                requestUSB (usbManager)
                readUSB(usbManager)
            } else {
                val string = "Permission denied for device: $usbDevice\n\n"
                Log.i(TAG, string)
                val tv: TextView = findViewById(R.id.text)
                tv.text = tv.text.toString() + "\n" + string
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun readUSB (usbManager: UsbManager){
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
        tv.text = tv.text.toString() + "\n" + string
    }

    @SuppressLint("SetTextI18n")
    fun openUSBAction (view: View) {
        mUsbManager = getSystemService(USB_SERVICE) as UsbManager

        if (mUsbDevice == null) {
            requestUSB(mUsbManager)
            val tv: TextView = findViewById(R.id.text)
            val string = "Manual Loading of USB"
            tv.text = tv.text.toString() + "\n$string"
            Log.i(TAG, string)
            return
        }

        if (mUsbManager.hasPermission(mUsbDevice))
            openUSB(mUsbManager, mUsbDevice)
    }

    @SuppressLint("SetTextI18n")
    fun openUSB (usbManager: UsbManager, usbDevice: UsbDevice?) {
        val bytes: ByteArray? = null
        val TIMEOUT = 0
        val forceClaim = true

        val usbInterface : UsbInterface? =
                usbDevice?.getInterface(0)
        val usbEndpoint : UsbEndpoint? =
                usbInterface?.getEndpoint(0)
        val usbDeviceConnection : UsbDeviceConnection =
            usbManager.openDevice(usbDevice)
        usbDeviceConnection.claimInterface(usbInterface, forceClaim)
        usbDeviceConnection.bulkTransfer(usbEndpoint, bytes, bytes!!.size, TIMEOUT)
        val string = "\n\nusbDeviceConnection: $bytes\n\n"
        Log.i(TAG, string)
        val tv : TextView = findViewById(R.id.text)
        tv.text = tv.text.toString() + "\n" + string
    }

    fun closeUSB (usbManager: UsbManager, usbDevice: UsbDevice?) {
        val usbInterface : UsbInterface? =
                usbDevice?.getInterface(0)
        val usbDeviceConnection : UsbDeviceConnection =
            usbManager.openDevice(usbDevice)

        val release = usbDeviceConnection.releaseInterface(usbInterface)
        if (!release){
            val string = "Could not release the interface!!!"
            Log.i(TAG, string)
            val tv: TextView = findViewById(R.id.text)
            tv.text = string
            return
        }
        usbDeviceConnection.close()

        val string = "USB was released!!!"
        Log.i(TAG, string)
        val tv: TextView = findViewById(R.id.text)
        tv.text = string
    }

    fun setupUSBAction (view: View) {
        if (mUsbManager.hasPermission(mUsbDevice))
            setupUSB(this)
    }

    fun setupUSB (context: Context) {
        val devices : Array<UsbMassStorageDevice> = UsbMassStorageDevice.getMassStorageDevices(context)

        if (devices.isEmpty()) {
            Log.i(TAG, "No devices found!!!")
            return
        }

        val device : UsbMassStorageDevice = devices[0]

        try {
            device.init()

            val fileSystem : FileSystem = device.partitions[0].fileSystem
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

    @SuppressLint("SetTextI18n")
    fun setupDeviceAction (view: View) {
        if (
            !(
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    )
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ), 1500
            )
            if (!(
                        (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        )
            ) {
                return
            }
        }


        val devices : Array<UsbMassStorageDevice> = UsbMassStorageDevice.getMassStorageDevices(this)

        if (devices.isEmpty()) {
            Log.i(TAG, "No device found")
            return
        }

        val device : UsbMassStorageDevice = devices[0]

        try {
            device.init()

            var string: String
            val tv : TextView = findViewById(R.id.text)

            string = "\ndevice.partitions.size: " + device.partitions.size.toString()
            Log.i(TAG, string)
            tv.text = tv.text.toString() + "\n" + string

            if (device.partitions.isNotEmpty()) {
                string = "\ndevice.partitions[0].fileSystem.type: " + device.partitions[0].fileSystem.type
                Log.i(TAG, string)
                tv.text = tv.text.toString() + "\n" + string
            }

        } catch (e : IOException) {
            val string = "Error setting up device $e"
            Log.i(TAG, string)
            val tv : TextView = findViewById(R.id.text)
            tv.text = tv.text.toString() + "\n" + string
        }

    }

//    fun
}
