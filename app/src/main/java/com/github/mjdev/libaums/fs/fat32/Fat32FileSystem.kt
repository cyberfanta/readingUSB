/*
 * (C) Copyright 2014 mjahnen <github@mgns.tech>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.github.mjdev.libaums.fs.fat32

import android.util.Log
import com.github.mjdev.libaums.driver.BlockDeviceDriver
import com.github.mjdev.libaums.fs.FileSystem
import com.github.mjdev.libaums.fs.UsbFile
import com.github.mjdev.libaums.fs.fat12.Fat12BootSector
import com.github.mjdev.libaums.fs.fat12.Fat12FAT
import com.github.mjdev.libaums.partition.PartitionTypes
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import kotlin.experimental.and

/**
 * This class represents the FAT32 file system and is responsible for setting
 * the FAT32 file system up and extracting the volume label and the root
 * directory.
 *
 * @author mjahnen
 */
class Fat32FileSystem
/**
 * This method constructs a FAT32 file system for the given block device.
 * There are no further checks that the block device actually represents a
 * valid FAT32 file system. That means it must be ensured that the device
 * actually holds a FAT32 file system in advance!
 *
 * @param blockDevice
 * The block device the FAT32 file system is located.
 * @param first512Bytes
 * First 512 bytes read from block device.
 * @throws IOException
 * If reading from the device fails.
 */
@Throws(IOException::class)
private constructor(blockDevice: BlockDeviceDriver, first512Bytes: ByteBuffer) : FileSystem {

    private val bootSector: Fat32BootSector = Fat32BootSector.read(first512Bytes)
    private val fat: FAT
    private val fsInfoStructure: FsInfoStructure
    override val rootDirectory: FatDirectory
    /**
     * Caches UsbFile instances returned by list files method. If we do not do
     * that we will get a new instance when searching for the same file.
     * Depending on what you do with the two different instances they can get out
     * of sync and only the one which is written latest will actually be persisted on
     * disk. This is especially problematic if you create files on different directory
     * instances.. See also issue 215.
     */
    internal val fileCache = WeakHashMap<String, UsbFile>()

    override val volumeLabel: String
        get() {
            return rootDirectory.volumeLabel?.let { it }.orEmpty()
        }

    override val capacity: Long
        get() = bootSector.totalNumberOfSectors * bootSector.bytesPerSector

    override val occupiedSpace: Long
        get() = capacity - freeSpace

    override val freeSpace: Long
        get() = fsInfoStructure.freeClusterCount * bootSector.bytesPerCluster

    override val chunkSize: Int
        get() = bootSector.bytesPerCluster

    override val type: Int
        get() = PartitionTypes.FAT32

    init {
        fsInfoStructure = FsInfoStructure.read(blockDevice, bootSector.fsInfoStartSector * bootSector.bytesPerSector)
        fat = FAT(blockDevice, bootSector, fsInfoStructure)
        rootDirectory = FatDirectory.readRoot(this, blockDevice, fat, bootSector)

        Log.d(TAG, bootSector.toString())
    }

    companion object {

        private val TAG = Fat32FileSystem::class.java.simpleName

        /**
         * This method constructs a FAT32 file system for the given block device.
         * There are no further checks if the block device actually represents a
         * valid FAT32 file system. That means it must be ensured that the device
         * actually holds a FAT32 file system in advance!
         *
         * @param blockDevice
         * The block device the FAT32 file system is located.
         * @throws IOException
         * If reading from the device fails.
         */
        @Throws(IOException::class)
        @JvmStatic
        fun read(blockDevice: BlockDeviceDriver): Fat32FileSystem? {

            val buffer = ByteBuffer.allocate(512)
            blockDevice.read(0, buffer)
            buffer.flip()

            return if (buffer.get(82).toInt().toChar() != 'F' ||
                buffer.get(83).toInt().toChar() != 'A' ||
                buffer.get(84).toInt().toChar() != 'T' ||
                buffer.get(85).toInt().toChar() != '3' ||
                buffer.get(86).toInt().toChar() != '2' ||
                buffer.get(87).toInt().toChar() != ' ' ||
                buffer.get(88).toInt().toChar() != ' ' ||
                buffer.get(89).toInt().toChar() != ' ') {
                        Log.i(TAG, "Not FAT32")
                if (!(buffer.get(54).toInt().toChar() != 'F' ||
                            buffer.get(55).toInt().toChar() != 'A' ||
                            buffer.get(56).toInt().toChar() != 'T' ||
                            buffer.get(57).toInt().toChar() != '1' ||
                            buffer.get(58).toInt().toChar() != '2' ||
                            buffer.get(59).toInt().toChar() != ' ' ||
                            buffer.get(60).toInt().toChar() != ' ' ||
                            buffer.get(61).toInt().toChar() != ' ')) {
                    Log.i(TAG, "It is FAT12!!!")

                    //todo: mejorar extracción de datos, crear clase a partir del  blockDevice
                    val bootSector = Fat12BootSector(buffer)
                    bootSector.log()

/*
//                    var string1 = ""
//                    var string2 = ""
//                    for (i in 0..2) {
//                        string1 += String.format("%02x", buffer.get(i))
//                        string2 += buffer.get(i).toInt().toChar()
//                    }
//                    Log.i(TAG, "Bootstrap: 0..2: $string1 $string2")

//                    string1 = ""
//                    string2 = ""
//                    for (i in 3..10) {
//                        string1 += String.format("%02x", buffer.get(i))
//                        string2 += buffer.get(i).toInt().toChar()
//                    }
//                    Log.i(TAG, "OEM name/version: 3..10: $string1 $string2")

//                    string1 = ""
//                    string2 = ""
//                    string1 += String.format("%02x", buffer.get(12))
//                    string1 += String.format("%02x", buffer.get(11))
//                    string2 += buffer.get(12).toInt().toChar()
//                    string2 += buffer.get(11).toInt().toChar()
//                    var num = Integer.parseInt(string1, 16)
//                    Log.i(TAG, "Number of bytes per sector: 11..12: $string1 $string2 ($num)")

//                    string1 = ""
//                    string2 = ""
//                    string1 += String.format("%02x", buffer.get(13))
//                    string2 += buffer.get(13).toInt().toChar()
//                    num = Integer.parseInt(string1, 16)
//                    Log.i(TAG, "Number of sectors per cluster: 13: $string1 $string2 ($num)")

//                    string1 = ""
//                    string2 = ""
//                    string1 += String.format("%02x", buffer.get(15))
//                    string1 += String.format("%02x", buffer.get(14))
//                    string2 += buffer.get(15).toInt().toChar()
//                    string2 += buffer.get(14).toInt().toChar()
//                    num = Integer.parseInt(string1, 16)
//                    var string = "Number of reserved sectors: 14..15: $string1 $string2"
//                    when (num) {
//                        1 -> Log.i(TAG, "$string (FAT12 or FAT16)")
//                        32 -> Log.i(TAG, "$string (FAT32)")
//                        else -> Log.i(TAG, string)
//                    }

//                    string1 = ""
//                    string2 = ""
//                    string1 += String.format("%02x", buffer.get(16))
//                    string2 += buffer.get(16).toInt().toChar()
//                    num = Integer.parseInt(string1, 16)
//                    Log.i(TAG, "Number of FAT copies: 16: $string1 $string2 ($num)")

//                    string1 = ""
//                    string2 = ""
//                    string1 += String.format("%02x", buffer.get(18))
//                    string1 += String.format("%02x", buffer.get(17))
//                    string2 += buffer.get(18).toInt().toChar()
//                    string2 += buffer.get(17).toInt().toChar()
//                    num = Integer.parseInt(string1, 16)
//                    string = "Number of root directory entries: 17..18: $string1 $string2 ($num)"
//                    when (num) {
//                        0 -> Log.i(TAG, "$string FAT32")
//                        512 -> Log.i(TAG, "$string FAT16")
//                        else -> Log.i(TAG, string)
//                    }

//                    string1 = ""
//                    string2 = ""
//                    string1 += String.format("%02x", buffer.get(20))
//                    string1 += String.format("%02x", buffer.get(19))
//                    string2 += buffer.get(20).toInt().toChar()
//                    string2 += buffer.get(19).toInt().toChar()
//                    num = Integer.parseInt(string1, 16)
//                    Log.i(
//                        TAG,
//                        "Total number of sectors in the filesystem: 19..20: $string1 $string2 ($num)"
//                    )

//                    string1 = ""
//                    string2 = ""
//                    string1 += String.format("%02x", buffer.get(21))
//                    string2 += buffer.get(21).toInt().toChar()
//                    when (string1) {
//                        "f0" -> Log.i(
//                            TAG,
//                            "Media descriptor type: 21: $string1 $string2 1.4 MB 3.5\" floppy"
//                        )
//                        "f8" -> Log.i(TAG, "Media descriptor type: 21: $string1 $string2 hard disk")
//                        else -> Log.i(TAG, "Media descriptor type: 21: $string1 $string2")
//                    }

//                    string1 = ""
//                    string2 = ""
//                    string1 += String.format("%02x", buffer.get(23))
//                    string1 += String.format("%02x", buffer.get(22))
//                    string2 += buffer.get(23).toInt().toChar()
//                    string2 += buffer.get(22).toInt().toChar()
//                    num = Integer.parseInt(string1, 16)
//                    string = "Number of sectors per FAT: 22..23: $string1 $string2 ($num)"
//                    when (num) {
//                        0 -> Log.i(TAG, "$string FAT32")
//                        else -> Log.i(TAG, string)
//                    }

//                    string1 = ""
//                    string2 = ""
//                    string1 += String.format("%02x", buffer.get(25))
//                    string1 += String.format("%02x", buffer.get(24))
//                    string2 += buffer.get(25).toInt().toChar()
//                    string2 += buffer.get(24).toInt().toChar()
//                    num = Integer.parseInt(string1, 16)
//                    Log.i(TAG, "Number of sectors per track: 24..25: $string1 $string2 ($num)")

//                    string1 = ""
//                    string2 = ""
//                    string1 += String.format("%02x", buffer.get(27))
//                    string1 += String.format("%02x", buffer.get(26))
//                    string2 += buffer.get(27).toInt().toChar()
//                    string2 += buffer.get(26).toInt().toChar()
//                    num = Integer.parseInt(string1, 16)
//                    when (num) {
//                        2 -> Log.i(
//                            TAG,
//                            "Number of heads: 26..27: $string1 $string2 ($num) double-sided diskette"
//                        )
//                        else -> Log.i(TAG, "Number of heads: 26..27: $string1 $string2 ($num)")
//                    }

//                    string1 = ""
//                    string2 = ""
//                    for (i in 31 downTo 28) {
//                        string1 += String.format("%02x", buffer.get(i))
//                        string2 += buffer.get(i).toInt().toChar()
//                    }
//                    num = Integer.parseInt(string1, 16)
//                    Log.i(TAG, "Number of hidden sectors: 28..31: $string1 $string2 ($num)")

//                    string1 = ""
//                    string2 = ""
//                    for (i in 35 downTo 32) {
//                        string1 += String.format("%02x", buffer.get(i))
//                        string2 += buffer.get(i).toInt().toChar()
//                    }
//                    num = Integer.parseInt(string1, 16)
//                    Log.i(
//                        TAG,
//                        "Total number of sectors in the filesystem: 32..35: $string1 $string2 ($num)"
//                    )

//                    string1 = ""
//                    string2 = ""
//                    string1 += String.format("%02x", buffer.get(36))
//                    string2 += buffer.get(36).toInt().toChar()
//                    Log.i(TAG, "Logical Drive Number: 36: $string1 $string2")
//
//                    string1 = ""
//                    string2 = ""
//                    string1 += String.format("%02x", buffer.get(37))
//                    string2 += buffer.get(37).toInt().toChar()
//                    when (string1) {
//                        "00" -> Log.i(TAG, "Reserved: 37: $string1 $string2 need disk check")
//                        "01" -> Log.i(TAG, "Reserved: 37: $string1 $string2 need surface scan")
//                        else -> Log.i(TAG, "Reserved: 37: $string1 $string2")
//                    }

//                    string1 = ""
//                    string2 = ""
//                    string1 += String.format("%02x", buffer.get(38))
//                    string2 += buffer.get(38).toInt().toChar()
//                    Log.i(TAG, "Extended signature: 38: $string1 $string2")

//                    string1 = ""
//                    string2 = ""
//                    for (i in 39..42) {
//                        string1 += String.format("%02x", buffer.get(i))
//                        string2 += buffer.get(i).toInt().toChar()
//                    }
//                    Log.i(TAG, "Serial number of partition: 39..42: $string1 $string2")

//                    string1 = ""
//                    string2 = ""
//                    for (i in 43..53) {
//                        string1 += String.format("%02x", buffer.get(i))
//                        string2 += buffer.get(i).toInt().toChar()
//                    }
//                    Log.i(TAG, "Volume label: 43..53: $string1 $string2")

//                    string1 = ""
//                    string2 = ""
//                    for (i in 54..61) {
//                        string1 += String.format("%02x", buffer.get(i))
//                        string2 += buffer.get(i).toInt().toChar()
//                    }
//                    when (string1) {
//                        "FAT12   " -> Log.i(TAG, "Filesystem type: 54..61: $string1 $string2 FAT12")
//                        "FAT16   " -> Log.i(TAG, "Filesystem type: 54..61: $string1 $string2 FAT16")
//                        "FAT     " -> Log.i(TAG, "Filesystem type: 54..61: $string1 $string2 FAT")
//                        else -> Log.i(TAG, "Filesystem type: 54..61: $string1 $string2")
//                    }

//                    string1 = ""
//                    string2 = ""
//                    for (i in 62..509) {
//                        string1 += String.format("%02x", buffer.get(i))
//                        string2 += buffer.get(i).toInt().toChar()
//                    }
//                    Log.i(TAG, "Bootstrap: 62..509: $string1\n$string2")

//                    string1 = ""
//                    string2 = ""
//                    for (i in 510..511) {
//                        string1 += String.format("%02x", buffer.get(i))
//                        string2 += buffer.get(i).toInt().toChar()
//                    }
//                    Log.i(TAG, "Signature: 510..511: $string1 $string2")
*/

                    val buffer1 = ByteBuffer.allocate(bootSector.bytesPerSectorDec * bootSector.fatCopiesDec) //2048
                    blockDevice.read(512, buffer1)
                    buffer1.flip()

                    val fat12Tables = Fat12FAT(buffer1, bootSector)
                    fat12Tables.log()

/*
//                    for (j in 1..4) {
//                        var string1 = ""
//                        var string2 = ""
//                        val k = 512 * (j - 1)
//                        for (i in (0 + k)..(511 + k) step 3) {
//                            var string3 = ""
//                            var string4 = ""
//                            var string5 = ""
//                            var string6 = ""
//
//                            for (l in 0..2) {
//                                try {
//                                    string1 += String.format("%02x", buffer1.get(i+l))
//                                    string2 += buffer1.get(i+l).toInt().toChar()
//                                } catch (e: IndexOutOfBoundsException) {
//                                }
//                            }
//
//                            try {
//                                string3 += String.format("%02x", buffer1.get(i+1))
//                            } catch (e: IndexOutOfBoundsException) {
//                            }
//                            string3 += String.format("%02x", buffer1.get(i))
//                            try {
//                                string5 += String.format("%8s", Integer.toBinaryString((buffer1.get(i+1) and 0xFF.toByte()).toInt())).replace(' ', '0')
//                            } catch (e: IndexOutOfBoundsException) {
//                            }
//                            string5 += String.format("%8s", Integer.toBinaryString((buffer1.get(i) and 0xFF.toByte()).toInt())).replace(' ', '0')
//                            try {
//                                string4 += String.format("%02x", buffer1.get(i+2))
//                            } catch (e: IndexOutOfBoundsException) {
//                            }
//                            try {
//                                string4 += String.format("%02x", buffer1.get(i+1))
//                            } catch (e: IndexOutOfBoundsException) {
//                            }
//                            try {
//                                string6 += String.format("%8s", Integer.toBinaryString((buffer1.get(i+2) and 0xFF.toByte()).toInt())).replace(' ', '0')
//                            } catch (e: IndexOutOfBoundsException) {
//                            }
//                            try {
//                                string6 += String.format("%8s", Integer.toBinaryString((buffer1.get(i+1) and 0xFF.toByte()).toInt())).replace(' ', '0')
//                            } catch (e: IndexOutOfBoundsException) {
//                            }
//
//                            val num1 = string3.toLong(16)
//                            val num2 = string4.toLong(16)
//
//                            string1 += " - $string3 $string4 ($string5 $string6) - $num1 $num2\n"
//                        }
//                        Log.i(
//                            TAG,
//                            "FAT $j: " + (512 + (j - 1) * 512) + ".." + (1023 + (j - 1) * 512) + ":"
//                        )
//                        Log.i(TAG, string1)
//                        Log.i(TAG, string2)
//                    }
*/

                    val buffer2 = ByteBuffer.allocate(512)
                    blockDevice.read(2560, buffer2)
                    buffer2.flip()

                    for (j in 0..31 step 2) {
                        var string1 = ""
                        var string2 = ""
                        var string3 = ""
                        var string4 = ""
                        for (i in 0..15) {
                            string1 += String.format("%02x", buffer2.get(j * 16 + i))
                            string2 += buffer2.get(j * 16 + i).toInt().toChar()
                            string3 += String.format("%02x", buffer2.get((j + 1) * 16 + i))
                            string4 += buffer2.get((j + 1) * 16 + i).toInt().toChar()
                        }
                        Log.i(
                            TAG,
                            "Root Directory Entry " + (j / 2) + ": " + (2560 + j * 16) + ".." + (2592 + j * 16 - 1) + ":"
                        )
                        Log.i(TAG, string1)
                        Log.i(TAG, string3)
                        Log.i(TAG, string2)
                        Log.i(TAG, string4)
                        string1 = ""
                        string2 = ""
                        for (i in 0..7)
                            string1 += buffer2.get(j * 16 + i).toInt().toChar()
                        for (i in 8..10)
                            string2 += buffer2.get(j * 16 + i).toInt().toChar()
                        Log.i(TAG, "File: $string1.$string2")
                        string1 = ""
                        string1 += String.format("%02x", buffer2.get(j * 16 + 11))
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
                        Log.i(TAG, string2)
                        string1 = ""
                        string1 += String.format("%02x", buffer2.get(j * 16 + 27))
                        string1 += String.format("%02x", buffer2.get(j * 16 + 26))
                        num = string1.toInt(16)
                        when (num) {
                            0 -> Log.i(TAG, "First Logical Cluster: $num $string1 Reserved Entry")
                            1 -> Log.i(TAG, "First Logical Cluster: $num $string1 Reserved Entry")
                            else -> Log.i(TAG, "First Logical Cluster: $num $string1")
                        }

                        string2 = ""
                        string2 += String.format("%02x", buffer2.get(j * 16 + 31))
                        string2 += String.format("%02x", buffer2.get(j * 16 + 30))
                        string2 += String.format("%02x", buffer2.get(j * 16 + 29))
                        string2 += String.format("%02x", buffer2.get(j * 16 + 28))
                        val num2 = string2.toLong(16)
                        Log.i(TAG, "File Size (in bytes): $num2 $string2")
                    }

                    val buffer3 = ByteBuffer.allocate(3072)
                    blockDevice.read(3072, buffer3)
                    buffer3.flip()

                    for (k in 0..4) {
                        var string1 = ""
                        var string2 = ""
                        val l = 512 * k
                        for (i in (0 + l)..(511 + l)) {
                            string1 += String.format("%02x", buffer3.get(i))
                            string2 += buffer3.get(i).toInt().toChar()
                        }
                        Log.i(TAG, "File Data (Buffer): $k")
                        Log.i(TAG, "File Data (in hex): $string1")
                        Log.i(TAG, "File Data (in char): $string2")
                    }


                    //                    Log.i(TAG, "FAT: 512..2048: $string1\n$string2")

                    //                    Fat32FileSystem(blockDevice, buffer)
                }
                if (!(buffer.get(54).toInt().toChar() != 'F' ||
                            buffer.get(55).toInt().toChar() != 'A' ||
                            buffer.get(56).toInt().toChar() != 'T' ||
                            buffer.get(57).toInt().toChar() != '1' ||
                            buffer.get(58).toInt().toChar() != '6' ||
                            buffer.get(59).toInt().toChar() != ' ' ||
                            buffer.get(60).toInt().toChar() != ' ' ||
                            buffer.get(61).toInt().toChar() != ' ')) {
                    Log.i(TAG, "It is FAT16!!!")
                }
                if (!(buffer.get(54).toInt().toChar() != 'F' ||
                            buffer.get(55).toInt().toChar() != 'A' ||
                            buffer.get(56).toInt().toChar() != 'T' ||
                            buffer.get(57).toInt().toChar() != ' ' ||
                            buffer.get(58).toInt().toChar() != ' ' ||
                            buffer.get(59).toInt().toChar() != ' ' ||
                            buffer.get(60).toInt().toChar() != ' ' ||
                            buffer.get(61).toInt().toChar() != ' ')) {
                    Log.i(TAG, "It is FAT!!!")
                }
                null
            } else {
                Log.i(TAG, "It is FAT32!!!")
                Fat32FileSystem(blockDevice, buffer)
                null
            }

        }
    }
}
