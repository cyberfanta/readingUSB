package com.github.mjdev.libaums.fs.fat12

import android.util.Log
import java.nio.ByteBuffer

@Suppress("MemberVisibilityCanBePrivate")
class Fat12FileData(buffer: ByteBuffer, bootSector: Fat12BootSector, fat12Tables: Fat12FAT, fat12Directory: Fat12Directory, offset: Long) {
    @Suppress("PrivatePropertyName")
    private val TAG = this::class.java.simpleName

    lateinit var cluster: Array<Byte>
    lateinit var clusterHex: Array<String>

    var clusterAmount = -1

    init {
        if (fat12Directory.indexLastFile != -1) {
            val fileSize = fat12Directory.fileSize[fat12Directory.indexLastFile]
            val bytesPerCluster = bootSector.sectorsPerClusterDec * bootSector.bytesPerSectorDec
            var num = fileSize.toFloat() / bytesPerCluster.toFloat()
            val num2 = num.toInt()
            if (num - num2 > 0)
                num++
            clusterAmount = num.toInt()

            Log.i(TAG, "Amount of Cluster: $clusterAmount")

            cluster = Array(clusterAmount * bytesPerCluster + 1){0x00.toByte()}
            clusterHex = Array(clusterAmount + 1){""}

            for (k in 0 until clusterAmount) {
                var string1 = ""
                val l = 512 * k
                for (i in (0 + l)..(511 + l)) {
                    string1 += String.format("%02x", buffer.get(i))
                    cluster[i] = buffer.get(i)
                }
                clusterHex[k] = string1
            }
        } else {
            //todo: Should load the rest of filesystem to memory, by now directly to the point of this development
            //bootSector and fat12Tables will be used here
            log()
        }
    }

    override fun toString(): String {
        var result = ""
        for (i in 0 until clusterAmount) {
            result += "{File Data (Cluster $i): " +
                    " File Data (in hex): " + clusterHex[i] +
                    " File Data (in char): " + cluster[i] +
                    "}, "
        }
        result = result.substring(0, result.length - 2)
        return "Fat12FileData($result)"
    }

    fun log() {
        Log.i(TAG, this.toString())
    }
}