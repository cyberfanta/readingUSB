package com.cyberfanta.readingusb.readUSB

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import net.alphadev.usbstorage.DeviceManager
import net.alphadev.usbstorage.StorageManager
import net.alphadev.usbstorage.UsbBulkDevice
import net.alphadev.usbstorage.api.device.BulkDevice
import net.alphadev.usbstorage.api.filesystem.StorageDevice
import java.text.DecimalFormat
import java.util.*

class ReadUSB(context: Context) {
    private val TAG="readUSB"
    private val ACTION_USB_PERMISSION = "USB_PERMISSION"
    private var volumeLabel = ""
    private var type = 0
    private var capacity = 0.toLong()
    private var occupiedSpace = 0.toLong()
    private var freeSpace = 0.toLong()
    private var chunkSize = 0
//    private var root : UsbFile? = null
    private var usbReceiver: BroadcastReceiver? = null

    private val mContext: Context = context
    private var mUsbManager: UsbManager? = null
    private var mStorageManager: StorageManager? = null
    private var usbBulkDevice: BulkDevice? = null
//    private var mMountedDevices = HashMap<String, StorageDevice>()
    var exception: String = ""

    var storage: StorageDevice? = null

    init {
        mStorageManager = StorageManager(context)
        DeviceManager(context, mStorageManager)

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
                unmountUSB(device)
            }
        }
        context.registerReceiver(broadcastReceiver, intentFilter)

        loadUSB()
    }

    fun loadUSB(){
        for (device in mUsbManager!!.deviceList.values) {
            if (!mUsbManager!!.hasPermission(device)) {
                val intent = PendingIntent.getBroadcast(mContext, 0, Intent(ACTION_USB_PERMISSION), 0)
                mUsbManager!!.requestPermission(device, intent)
                continue
            }
            mountUSB(device)
        }
    }

    fun mountUSB(device: UsbDevice?){
        try {
            usbBulkDevice = UsbBulkDevice.read(mContext, device)
            if (usbBulkDevice != null && mStorageManager!!.tryMount(usbBulkDevice)) {
                mStorageManager!!.notifyStorageChanged()

                //Reading USB Internal Data
//            val blockDevice: BlockDevice = BulkBlockDevice(usbBulkDevice)
//            blockDevice.initialize()
//            val mbr = MasterBootRecord(blockDevice)
//            for (partition in mbr.partitions) {
//                if (tryMountPartition(partition)) {
//                    return
//                }
//            }
                storage = mStorageManager!!.storage
                exception = ""
            }
        } catch (e: Exception) {
            exception = e.toString()
        }
    }

    fun getUSBDeviceLListSize (): String {
        return mStorageManager?.mMountedDevices?.size.toString()
    }

    fun getUSBName (): String {
        return if (storage != null) {
            storage!!.getName()
        } else
            "---"
    }

    fun getUSBUnallocatedSpace (): String {
        return if (storage != null) {
            storage!!.getUnallocatedSpace().toString()
        } else
            "---"
    }

    fun getUSBTotalSpace (): String {
        return if (storage != null) {
            storage!!.getTotalSpace().toString()
        } else
            "---"
    }

    fun getUSBType (): String {
        return if (storage != null) {
            storage!!.getType()
        } else
            "---"
    }

//    private fun tryMountPartition(device: Partition): Boolean {
//        if (mMountedDevices.containsKey(device.id)) {
//            // device seems already mounted… do nothing.
//            return false
//        }
//        val storage: StorageDevice = firstTry(device)
//        if (storage != null) {
//            mMountedDevices.put(device.id, storage)
//            postStorageNotification(storage)
//            return true
//        }
//        return false
//    }

    fun unmountUSB(device: UsbDevice?){
        val deviceId = Integer.valueOf(device!!.deviceId).toString()
        mStorageManager!!.unmount(deviceId)
        mStorageManager!!.notifyStorageChanged()
    }

//    fun discoverUSB(): Boolean {
//        if (usbBulkDevice != null && mStorageManager!!.tryMount(usbBulkDevice)) {
//            mStorageManager!!.notifyStorageChanged()
//        }
//
//
////        val usbManager = getSystemService(AppCompatActivity.USB_SERVICE) as UsbManager
////        val massStorageDevices = getMassStorageDevices(context)
//        if (massStorageDevices.isEmpty()) {
//            Log.w(TAG, "no device found!")
//            val textViewMain = findViewById<TextView>(R.id.textView)
//            textViewMain.text = getString(R.string.nodevice)
//            return false
//        }
//
//        val currentDevice = 0
//        val usbDevice = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
//        if (usbDevice != null && usbManager.hasPermission(usbDevice)) {
//            Log.d(TAG, "received usb device via intent")
//            setupDevice(massStorageDevices)
//        } else {
//            Log.d(TAG, "requesting permission to access the usb device")
//            val permissionIntent = PendingIntent.getBroadcast(
//                this,
//                0,
//                Intent(ACTION_USB_PERMISSION),
//                0
//            )
//            usbManager.requestPermission(
//                massStorageDevices[currentDevice].usbDevice,
//                permissionIntent
//            )
//        }
//        return true
//    }

//    private fun setupDevice(massStorageDevices: Array<UsbMassStorageDevice>){
//        try {
//            val currentDevice = 0
//            massStorageDevices[currentDevice].init()
//
//            val currentFs = massStorageDevices[currentDevice].partitions[0].fileSystem.also {
//                volumeLabel = it.volumeLabel
//                Log.d(TAG, "Capacity: $volumeLabel")
//                type = it.type
//                Log.d(TAG, "Capacity: $type")
//                capacity = it.capacity
//                Log.d(TAG, "Capacity: $capacity")
//                occupiedSpace = it.occupiedSpace
//                Log.d(TAG, "Occupied Space: $occupiedSpace")
//                freeSpace = it.freeSpace
//                Log.d(TAG, "Free Space: $freeSpace")
//                chunkSize = it.chunkSize
//                Log.d(TAG, "Chunk Size: $chunkSize")
//            }
//
//            root = currentFs.rootDirectory
//
//        } catch (e: IOException) {
//            Log.e(TAG, "error setting up device", e)
//        }
//    }

    /**
     * http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
     */
    private fun readableFileSize(size: Long): String? {
        if (size <= 0) {
            return "0B"
        }
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        val roundedSize = (size / Math.pow(1024.0, digitGroups.toDouble())).toFloat()
        return DecimalFormat("#,##0.#").format(roundedSize.toDouble()) + " " + units[digitGroups]
    }
}