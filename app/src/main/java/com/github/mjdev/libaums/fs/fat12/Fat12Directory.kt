package com.github.mjdev.libaums.fs.fat12

import android.util.Log
import com.github.mjdev.libaums.fs.fat32.Fat32FileSystem
import java.nio.ByteBuffer

class Fat12Directory (buffer: ByteBuffer, bootSector: Fat12BootSector) {
    private val TAG = this::class.java.simpleName

    lateinit var fat: Array<String>
    lateinit var fatHex: Array<String>

    init {
        for (j in 0..31 step 2) {
            var string1 = ""
            var string2 = ""
            var string3 = ""
            var string4 = ""
            for (i in 0..15) {
                string1 += String.format("%02x", buffer.get(j * 16 + i))
                string2 += buffer.get(j * 16 + i).toInt().toChar()
                string3 += String.format("%02x", buffer.get((j + 1) * 16 + i))
                string4 += buffer.get((j + 1) * 16 + i).toInt().toChar()
            }
            Log.i(
                Fat32FileSystem.TAG,
                "Root Directory Entry " + (j / 2) + ": " + (2560 + j * 16) + ".." + (2592 + j * 16 - 1) + ":"
            )
            Log.i(Fat32FileSystem.TAG, string1)
            Log.i(Fat32FileSystem.TAG, string3)
            Log.i(Fat32FileSystem.TAG, string2)
            Log.i(Fat32FileSystem.TAG, string4)
            string1 = ""
            string2 = ""
            for (i in 0..7)
                string1 += buffer.get(j * 16 + i).toInt().toChar()
            for (i in 8..10)
                string2 += buffer.get(j * 16 + i).toInt().toChar()
            Log.i(Fat32FileSystem.TAG, "File: $string1.$string2")
            string1 = ""
            string1 += String.format("%02x", buffer.get(j * 16 + 11))
            var num = Integer.parseInt(string1, 16)
            string2 = "Attrib: $string1 "
            string2 += Integer.toBinaryString(num)
            if (num % 2 == 1)
                string2 += ", Read-only"
            if ((num / 2) % 2 == 1)
                string2 += ", Hidden"
            if ((num / 4) % 2 == 1)
                string2 += ", System"
            if ((num / 8) % 2 == 1)
                string2 += ", Volume label"
            if ((num / 16) % 2 == 1)
                string2 += ", Subdirectory"
            if ((num / 32) % 2 == 1)
                string2 += ", Archive"
            Log.i(Fat32FileSystem.TAG, string2)
            string1 = ""
            string1 += String.format("%02x", buffer.get(j * 16 + 27))
            string1 += String.format("%02x", buffer.get(j * 16 + 26))
            num = string1.toInt(16)
            when (num) {
                0 -> Log.i(Fat32FileSystem.TAG, "First Logical Cluster: $num $string1 Reserved Entry")
                1 -> Log.i(Fat32FileSystem.TAG, "First Logical Cluster: $num $string1 Reserved Entry")
                else -> Log.i(Fat32FileSystem.TAG, "First Logical Cluster: $num $string1")
            }

            string2 = ""
            string2 += String.format("%02x", buffer.get(j * 16 + 31))
            string2 += String.format("%02x", buffer.get(j * 16 + 30))
            string2 += String.format("%02x", buffer.get(j * 16 + 29))
            string2 += String.format("%02x", buffer.get(j * 16 + 28))
            val num2 = string2.toLong(16)
            Log.i(Fat32FileSystem.TAG, "File Size (in bytes): $num2 $string2")
        }

    }

    fun log() {
        Log.i(TAG, this.toString())
    }
}