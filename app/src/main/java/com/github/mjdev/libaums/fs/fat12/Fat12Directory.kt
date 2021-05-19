package com.github.mjdev.libaums.fs.fat12

import android.util.Log
import java.nio.ByteBuffer

@Suppress("MemberVisibilityCanBePrivate")
class Fat12Directory (buffer: ByteBuffer, bootSector: Fat12BootSector, offset: Long) {
    @Suppress("PrivatePropertyName")
    private val TAG = this::class.java.simpleName

    var directory: Array<String>
    var directory16Bytes: Array<String>
    var filename: Array<String>
    var attrib: Array<String>
    var firstLogicalCluster: Array<Int>
    var fileSize: Array<Long>

    var directoryHex: Array<String>
    var directoryHex16Bytes: Array<String>
    var firstLogicalClusterHex: Array<String>
    var fileSizeHex: Array<String>

    var attribDec: Array<Int>

    var attribText: Array<String>
    var firstLogicalClusterText: Array<String>

    var entryAmount = 0
    var offset = 0L

    var indexLastFile = -1

    init {
        entryAmount = bootSector.directoryEntriesDec
        this.offset = offset

        //todo: Create class for file directory object to reduce to code here???
        directory = Array(entryAmount){""}
        directory16Bytes = Array(entryAmount * 2){""}
        filename = Array(entryAmount){""}
        attrib = Array(entryAmount){""}
        firstLogicalCluster = Array(entryAmount){0}
        fileSize = Array(entryAmount){0L}

        directoryHex = Array(entryAmount){""}
        directoryHex16Bytes = Array(entryAmount * 2){""}
        firstLogicalClusterHex = Array(entryAmount){""}
        fileSizeHex = Array(entryAmount){""}

        attribDec = Array(entryAmount){0}

        attribText = Array(entryAmount){""}
        firstLogicalClusterText = Array(entryAmount){""}

        val amount = entryAmount * 2 - 1
        for (j in 0..amount step 2) {
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
            directory16Bytes[j]=string2
            directory16Bytes[j+1]=string4
            directory[j/2]="$string2$string4"

            directoryHex16Bytes[j]=string1
            directoryHex16Bytes[j+1]=string3
            directoryHex[j/2]="$string1$string3"

            string1 = ""
            string2 = ""
            for (i in 0..7)
                string1 += buffer.get(j * 16 + i).toInt().toChar()
            for (i in 8..10)
                string2 += buffer.get(j * 16 + i).toInt().toChar()
            filename[j/2]= "$string1.$string2"

            string1 = ""
            string1 += String.format("%02x", buffer.get(j * 16 + 11))
            var num = Integer.parseInt(string1, 16)
            string2 = ""
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
            attrib[j/2]=string1
            attribDec[j/2]=num
            attribText[j/2]=string2

            string1 = ""
            string2 = ""
            string1 += String.format("%02x", buffer.get(j * 16 + 27))
            string1 += String.format("%02x", buffer.get(j * 16 + 26))
            num = string1.toInt(16)
            when (num) {
                0 -> string2 = "Reserved Entry"
                1 -> string2 = "Reserved Entry"
            }
            firstLogicalCluster[j/2]=num
            firstLogicalClusterHex[j/2]=string1
            firstLogicalClusterText[j/2]=string2

            string2 = ""
            string2 += String.format("%02x", buffer.get(j * 16 + 31))
            string2 += String.format("%02x", buffer.get(j * 16 + 30))
            string2 += String.format("%02x", buffer.get(j * 16 + 29))
            string2 += String.format("%02x", buffer.get(j * 16 + 28))
            val num2 = string2.toLong(16)
            fileSize[j/2]=num2
            fileSizeHex[j/2]=string2
        }
    }

    override fun toString(): String {
        var result = "Root Directory Total Entries: '$entryAmount' "

        for (i in 0 until entryAmount) {
            result += "{Root Directory Entry " + i + ": " + (offset + i * 32) + ".." + (offset + i * 32 + 31) + ": '" +
                    directoryHex[i] + "' - '" + directory[i] + "', " +
                    "File: '" + filename[i] + "', " +
                    "Attrib: " + attrib[i] + "' - '" + attribDec[i] +
                    (if (attribText[i] == "") ", " else "' - '" + attribText[i] + "', ") +
                    "First Logical Cluster: " + firstLogicalCluster[i] + "' - '" + firstLogicalClusterHex[i] +
                    (if (firstLogicalClusterText[i] == "") ", " else "' - '" + firstLogicalClusterText[i] + "', ") +
                    "File Size (in bytes): " + fileSize[i] + "' - '" + fileSizeHex[i] +
                    "}, "
        }
        result = result.substring(0, result.length - 2)
        return "Fat12Directory($result)"
    }

    fun log() {
        Log.i(TAG, this.toString())
    }
}