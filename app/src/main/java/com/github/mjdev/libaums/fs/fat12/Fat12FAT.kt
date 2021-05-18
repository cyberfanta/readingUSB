package com.github.mjdev.libaums.fs.fat12

import android.util.Log
import com.github.mjdev.libaums.fs.fat32.Fat32FileSystem
import java.nio.ByteBuffer
import kotlin.experimental.and

class Fat12FAT (buffer: ByteBuffer, bootSector: Fat12BootSector) {
    private val TAG = this::class.java.simpleName

    lateinit var fat: Array<String>
    lateinit var fatHex: Array<String>

    var fatCopies = 1

    init {
        this.fatCopies = bootSector.fatCopiesDec
        for (j in 1..this.fatCopies) {
            var string1 = ""
            var string2 = ""
            val k = 512 * (j - 1)
            for (i in (0 + k)..(511 + k) step 3) {
                var string3 = ""
                var string4 = ""
                var string5 = ""
                var string6 = ""

                for (l in 0..2) {
                    try {
                        string1 += String.format("%02x", buffer.get(i+l))
                        string2 += buffer.get(i+l).toInt().toChar()
                    } catch (e: IndexOutOfBoundsException) {
                    }
                }

                try {
                    string3 += String.format("%02x", buffer.get(i+1))
                } catch (e: IndexOutOfBoundsException) {
                }
                string3 += String.format("%02x", buffer.get(i))
                try {
                    string5 += String.format("%8s", Integer.toBinaryString((buffer.get(i+1) and 0xFF.toByte()).toInt())).replace(' ', '0')
                } catch (e: IndexOutOfBoundsException) {
                }
                string5 += String.format("%8s", Integer.toBinaryString((buffer.get(i) and 0xFF.toByte()).toInt())).replace(' ', '0')
                try {
                    string4 += String.format("%02x", buffer.get(i+2))
                } catch (e: IndexOutOfBoundsException) {
                }
                try {
                    string4 += String.format("%02x", buffer.get(i+1))
                } catch (e: IndexOutOfBoundsException) {
                }
                try {
                    string6 += String.format("%8s", Integer.toBinaryString((buffer.get(i+2) and 0xFF.toByte()).toInt())).replace(' ', '0')
                } catch (e: IndexOutOfBoundsException) {
                }
                try {
                    string6 += String.format("%8s", Integer.toBinaryString((buffer.get(i+1) and 0xFF.toByte()).toInt())).replace(' ', '0')
                } catch (e: IndexOutOfBoundsException) {
                }

                val num1 = string3.toLong(16) //todo: revisar conversion a 12 bits
                val num2 = string4.toLong(16)

                string1 += " - $string3 $string4 ($string5 $string6) - $num1 $num2\n"
            }
            fat[j-1] = string1
            fatHex[j-1] = string2
        }
    }

    override fun toString(): String {
        var result = "Fat12FAT("
        for (j in 1..this.fatCopies) {
            result += "FAT $j: " + (512 + (j - 1) * 512) + ".." + (1023 + (j - 1) * 512) + ": "
            result += fat[j-1] + ", "
            result += fatHex[j-1] + ", "
        }
        result = result.substring(0, result.length - 1)
        return "$result)"
    }

    fun log() {
        Log.i(TAG, this.toString())
    }
}