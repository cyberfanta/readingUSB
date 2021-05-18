package com.github.mjdev.libaums.fs.fat12

import android.util.Log
import java.nio.ByteBuffer

class Fat12BootSector(buffer: ByteBuffer) {
    private val TAG = this::class.java.simpleName

    var bootstrap = ""
    var version = ""
    var bytesPerSector = ""
    var sectorsPerCluster = ""
    var reservedSectors = ""

    var fatCopies = ""
    var directoryEntries = ""
    var sectorsInFilesystem = ""
    var mediaDescriptorType = ""
    var sectorsPerFat = ""
    var sectorsPerTrack = ""
    var numberOfHeads = ""
    var hiddenSectors = ""
    var sectorsInFat32Filesystem = ""
    var logicalDriveNumber = ""
    var reserved = ""
    var extendedSignature = ""
    var serialNumber = ""
    var volumeLabel = ""
    var filesystemType = ""
    var bottomBootstrap = ""
    var signature = ""

    var bootstrapHex = ""
    var versionHex = ""
    var bytesPerSectorHex = ""
    var sectorsPerClusterHex = ""
    var reservedSectorsHex = ""
    var fatCopiesHex = ""
    var directoryEntriesHex = ""
    var sectorsInFilesystemHex = ""
    var mediaDescriptorTypeHex = ""
    var sectorsPerFatHex = ""
    var sectorsPerTrackHex = ""
    var numberOfHeadsHex = ""
    var hiddenSectorsHex = ""
    var sectorsInFat32FilesystemHex = ""
    var logicalDriveNumberHex = ""
    var reservedHex = ""
    var extendedSignatureHex = ""
    var serialNumberHex = ""
    var volumeLabelHex = ""
    var filesystemTypeHex = ""
    var bottomBootstrapHex = ""
    var signatureHex = ""

    var bytesPerSectorDec = 0
    var sectorsPerClusterDec = 0
    var reservedSectorsDec = 0
    var fatCopiesDec = 0
    var directoryEntriesDec = 0
    var sectorsInFilesystemDec = 0
    var sectorsPerFatDec = 0
    var sectorsPerTrackDec = 0
    var numberOfHeadsDec = 0
    var hiddenSectorsDec = 0
    var sectorsInFat32FilesystemDec = 0

    var reservedSectorsText = ""
    var directoryEntriesText = ""
    var mediaDescriptorTypeText = ""
    var sectorsPerFatText = ""
    var numberOfHeadsText = ""
    var reservedText = ""
    var filesystemTypeText = ""

    init {
        var string1 = ""
        var string2 = ""
        for (i in 0..2) {
            string1 += String.format("%02x", buffer.get(i))
            string2 += buffer.get(i).toInt().toChar()
        }
        this.bootstrap = string1
        this.bootstrapHex = string2

        string1 = ""
        string2 = ""
        for (i in 3..10) {
            string1 += String.format("%02x", buffer.get(i))
            string2 += buffer.get(i).toInt().toChar()
        }
        this.version = string1
        this.versionHex = string2

        string1 = ""
        string2 = ""
        string1 += String.format("%02x", buffer.get(12))
        string1 += String.format("%02x", buffer.get(11))
        string2 += buffer.get(12).toInt().toChar()
        string2 += buffer.get(11).toInt().toChar()
        var num = Integer.parseInt(string1, 16)
        this.bytesPerSector = string1
        this.bytesPerSectorHex = string2
        this.bytesPerSectorDec = num

        string1 = ""
        string2 = ""
        string1 += String.format("%02x", buffer.get(13))
        string2 += buffer.get(13).toInt().toChar()
        num = Integer.parseInt(string1, 16)
        this.sectorsPerCluster = string1
        this.sectorsPerClusterHex = string2
        this.sectorsPerClusterDec = num

        string1 = ""
        string2 = ""
        string1 += String.format("%02x", buffer.get(15))
        string1 += String.format("%02x", buffer.get(14))
        string2 += buffer.get(15).toInt().toChar()
        string2 += buffer.get(14).toInt().toChar()
        num = Integer.parseInt(string1, 16)
        this.reservedSectors = string1
        this.reservedSectorsHex = string2
        this.reservedSectorsDec = num
        when (num) {
            1 -> this.reservedSectorsText = "(FAT12 or FAT16)"
            32 -> this.reservedSectorsText = "(FAT32)"
        }

        string1 = ""
        string2 = ""
        string1 += String.format("%02x", buffer.get(16))
        string2 += buffer.get(16).toInt().toChar()
        num = Integer.parseInt(string1, 16)
        this.fatCopies = string1
        this.fatCopiesHex = string2
        this.fatCopiesDec = num

        string1 = ""
        string2 = ""
        string1 += String.format("%02x", buffer.get(18))
        string1 += String.format("%02x", buffer.get(17))
        string2 += buffer.get(18).toInt().toChar()
        string2 += buffer.get(17).toInt().toChar()
        num = Integer.parseInt(string1, 16)
        this.directoryEntries = string1
        this.directoryEntriesHex = string2
        this.directoryEntriesDec = num
        when (num) {
            0 -> this.directoryEntriesText = "(FAT32)"
            512 -> this.directoryEntriesText = "(FAT16)"
        }

        string1 = ""
        string2 = ""
        string1 += String.format("%02x", buffer.get(20))
        string1 += String.format("%02x", buffer.get(19))
        string2 += buffer.get(20).toInt().toChar()
        string2 += buffer.get(19).toInt().toChar()
        num = Integer.parseInt(string1, 16)
        this.sectorsInFilesystem = string1
        this.sectorsInFilesystemHex = string2
        this.sectorsInFilesystemDec = num

        string1 = ""
        string2 = ""
        string1 += String.format("%02x", buffer.get(21))
        string2 += buffer.get(21).toInt().toChar()
        this.mediaDescriptorType = string1
        this.mediaDescriptorTypeHex = string2
        when (string1) {
            "f0" -> this.mediaDescriptorTypeText = "(1.4 MB 3.5\" floppy)"
            "f8" -> this.mediaDescriptorTypeText = "(hard disk)"
        }

        string1 = ""
        string2 = ""
        string1 += String.format("%02x", buffer.get(23))
        string1 += String.format("%02x", buffer.get(22))
        string2 += buffer.get(23).toInt().toChar()
        string2 += buffer.get(22).toInt().toChar()
        num = Integer.parseInt(string1, 16)
        this.sectorsPerFat = string1
        this.sectorsPerFatHex = string2
        this.sectorsPerFatDec = num
        when (num) {
            0 -> this.sectorsPerFatText = "(FAT32)"
        }

        string1 = ""
        string2 = ""
        string1 += String.format("%02x", buffer.get(25))
        string1 += String.format("%02x", buffer.get(24))
        string2 += buffer.get(25).toInt().toChar()
        string2 += buffer.get(24).toInt().toChar()
        num = Integer.parseInt(string1, 16)
        this.sectorsPerTrack = string1
        this.sectorsPerTrackHex = string2
        this.sectorsPerTrackDec = num

        string1 = ""
        string2 = ""
        string1 += String.format("%02x", buffer.get(27))
        string1 += String.format("%02x", buffer.get(26))
        string2 += buffer.get(27).toInt().toChar()
        string2 += buffer.get(26).toInt().toChar()
        num = Integer.parseInt(string1, 16)
        this.numberOfHeads = string1
        this.numberOfHeadsHex = string2
        this.numberOfHeadsDec = num
        when (num) {
            2 -> this.numberOfHeadsText = "(double-sided diskette)"
        }

        string1 = ""
        string2 = ""
        for (i in 31 downTo 28) {
            string1 += String.format("%02x", buffer.get(i))
            string2 += buffer.get(i).toInt().toChar()
        }
        num = Integer.parseInt(string1, 16)
        this.hiddenSectors = string1
        this.hiddenSectorsHex = string2
        this.hiddenSectorsDec = num

        string1 = ""
        string2 = ""
        for (i in 35 downTo 32) {
            string1 += String.format("%02x", buffer.get(i))
            string2 += buffer.get(i).toInt().toChar()
        }
        num = Integer.parseInt(string1, 16)
        this.sectorsInFat32Filesystem = string1
        this.sectorsInFat32FilesystemHex = string2
        this.sectorsInFat32FilesystemDec = num

        string1 = ""
        string2 = ""
        string1 += String.format("%02x", buffer.get(36))
        string2 += buffer.get(36).toInt().toChar()
        this.logicalDriveNumber = string1
        this.logicalDriveNumberHex = string2

        string1 = ""
        string2 = ""
        string1 += String.format("%02x", buffer.get(37))
        string2 += buffer.get(37).toInt().toChar()
        this.reserved = string1
        this.reservedHex = string2
        when (string1) {
            "00" -> this.reservedText = "(need disk check)"
            "01" -> this.reservedText = "(need surface scan)"
        }

        string1 = ""
        string2 = ""
        string1 += String.format("%02x", buffer.get(38))
        string2 += buffer.get(38).toInt().toChar()
        this.extendedSignature = string1
        this.extendedSignatureHex = string2

        string1 = ""
        string2 = ""
        for (i in 39..42) {
            string1 += String.format("%02x", buffer.get(i))
            string2 += buffer.get(i).toInt().toChar()
        }
        this.serialNumber = string1
        this.serialNumberHex = string2

        string1 = ""
        string2 = ""
        for (i in 43..53) {
            string1 += String.format("%02x", buffer.get(i))
            string2 += buffer.get(i).toInt().toChar()
        }
        this.volumeLabel = string1
        this.volumeLabelHex = string2

        string1 = ""
        string2 = ""
        for (i in 54..61) {
            string1 += String.format("%02x", buffer.get(i))
            string2 += buffer.get(i).toInt().toChar()
        }
        this.filesystemType = string1
        this.filesystemTypeHex = string2
        when (string1) {
            "FAT12   " -> this.filesystemTypeText = "(FAT12)"
            "FAT16   " -> this.filesystemTypeText = "(FAT16)"
            "FAT     " -> this.filesystemTypeText = "(FAT)"
        }

        string1 = ""
        string2 = ""
        for (i in 62..509) {
            string1 += String.format("%02x", buffer.get(i))
            string2 += buffer.get(i).toInt().toChar()
        }
        this.bottomBootstrap = string1
        this.bottomBootstrapHex = string2

        string1 = ""
        string2 = ""
        for (i in 510..511) {
            string1 += String.format("%02x", buffer.get(i))
            string2 += buffer.get(i).toInt().toChar()
        }
        this.signature = string1
        this.signatureHex = string2
    }

    override fun toString(): String {
        return "Fat12BootSector(" +
                "Bootstrap: 0..2: '$bootstrap' - '$bootstrapHex', " +
                "OEM name/version: 3..10: '$version' - '$versionHex', " +
                "Number of bytes per sector: 11..12: '$bytesPerSector' - '$bytesPerSectorHex' - '$bytesPerSectorDec', " +
                "Number of sectors per cluster: 13: '$sectorsPerCluster' - '$sectorsPerClusterHex' - '$sectorsPerClusterDec', " +
                "Number of reserved sectors: 14..15: '$reservedSectors' - '$reservedSectorsHex'" +
                (if (reservedSectorsText == "") ", " else " - '$reservedSectorsText', ") +
                "Number of FAT copies: 16: '$fatCopies' - '$fatCopiesHex' - '$fatCopiesDec', " +
                "Number of root directory entries: 17..18: '$directoryEntries' - '$directoryEntriesHex'" +
                (if (directoryEntriesText == "") ", " else " - '$directoryEntriesText', ") +
                "Total number of sectors in the filesystem: 19..20: '$sectorsInFilesystem' - '$sectorsInFilesystemHex' - '$sectorsInFilesystemDec', " +
                "Media descriptor type: 21: '$mediaDescriptorType' - '$mediaDescriptorTypeHex' - '$mediaDescriptorTypeText', " +
                "Number of sectors per FAT: 22..23: '$sectorsPerFat' - '$sectorsPerFatHex' - '$sectorsPerFatDec', " +
                (if (sectorsPerFatText == "") ", " else " - '$sectorsPerFatText', ") +
                "Number of sectors per track: 24..25: '$sectorsPerTrack' - '$sectorsPerTrackHex' - '$sectorsPerTrackDec', " +
                "Number of heads: 26..27: '$numberOfHeads' - '$numberOfHeadsHex' - '$numberOfHeadsDec', " +
                (if (numberOfHeadsText == "") ", " else " - '$numberOfHeadsText', ") +
                "Number of hidden sectors: 28..31: '$hiddenSectors' - '$hiddenSectorsHex' - '$hiddenSectorsDec', " +
                "Total number of sectors in the filesystem: 32..35: '$sectorsInFat32Filesystem' - '$sectorsInFat32FilesystemHex' - '$sectorsInFat32FilesystemDec', " +
                "Logical Drive Number: 36: '$logicalDriveNumber' - '$logicalDriveNumberHex', " +
                "Reserved: 37: '$reserved' - '$reservedHex'" +
                (if (reservedText == "") ", " else " - '$reservedText', ") +
                "Extended signature: 38: '$extendedSignature' - '$extendedSignatureHex', " +
                "Serial number of partition: 39..42: '$serialNumber' - '$serialNumberHex', " +
                "Volume label: 43..53: '$volumeLabel' - '$volumeLabelHex', " +
                "Filesystem type: 54..61: '$filesystemType' - '$filesystemTypeHex'" +
                (if (filesystemTypeText == "") ", " else " - '$filesystemTypeText', ") +
                "Bootstrap: 62..509: '$bottomBootstrap' - '$bottomBootstrapHex', " +
                "Signature: 510..511: '$signature' - '$signatureHex', " +
                ")"
    }

    fun log() {
        Log.i(TAG, this.toString())
    }
}