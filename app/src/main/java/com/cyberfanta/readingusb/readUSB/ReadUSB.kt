package com.cyberfanta.readingusb.readUSB

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import net.alphadev.usbstorage.StorageManager
import net.alphadev.usbstorage.UsbBulkDevice
import net.alphadev.usbstorage.api.device.BulkDevice

class ReadUSB(context: Context) {
    private val TAG="readUSB"
    private val ACTION_USB_PERMISSION = "USB_PERMISSION"
    private var volumeLabel = ""
    private var type = 0
    private var capacity = 0.toLong()
    private var occupiedSpace = 0.toLong()
    private var freeSpace = 0.toLong()
    private var chunkSize = 0
    private var root : UsbFile? = null
    private var usbReceiver: BroadcastReceiver? = null

    private var mUsbManager: UsbManager? = null
    private val mStorageManager: StorageManager? = null
    private var mContext: Context? = context


    init {
        mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        var intentFilter = IntentFilter(ACTION_USB_PERMISSION)
        var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    mountUSB(device)
                }
            }
        }
        context.registerReceiver(broadcastReceiver, intentFilter)

        intentFilter = IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                loadUSB()
            }
        }
        context.registerReceiver(broadcastReceiver, intentFilter)

        intentFilter = IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                unloadUSB(device)
            }
        }
        context.registerReceiver(broadcastReceiver, intentFilter)
    }

    fun mountUSB(device: UsbDevice?){
        val usbBulkDevice: BulkDevice? = UsbBulkDevice.read(mContext, device)
        if (usbBulkDevice != null && mStorageManager!!.tryMount(usbBulkDevice)) {
            mStorageManager!!.notifyStorageChanged()
        }
    }

    fun loadUSB(){
        for (device in mUsbManager!!.deviceList.values) {
            if (!mUsbManager!!.hasPermission(device)) {
                val intent = PendingIntent.getBroadcast(
                    mContext,
                    0,
                    Intent(ACTION_USB_PERMISSION),
                    0
                )
                mUsbManager!!.requestPermission(device, intent)
                continue
            }
            mountUSB(device)
        }
    }

    fun unloadUSB(device: UsbDevice?){
        //
    }

/*
    private fun discoverUSB(context: Context): Boolean {
        val usbManager = getSystemService(AppCompatActivity.USB_SERVICE) as UsbManager
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
*/

}