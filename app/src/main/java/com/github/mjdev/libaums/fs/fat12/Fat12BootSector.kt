package com.github.mjdev.libaums.fs.fat12

import java.nio.ByteBuffer

class Fat12BootSector(buffer: ByteBuffer) {
    private val TAG = this::class.java.simpleName

    private var bootstrap = ""
    private var version = ""
    private var bytesPerSector = ""
    private var sectorsPerCluster = ""
    private var reservedSectors = ""

    private var fatCopies = ""
    private var directoryEntries = ""
    private var sectorsInFilesystem = ""
    private var mediaDescriptorType = ""
    private var sectorsPerFat = ""
    private var sectorsPerTrack = ""
    private var numberOfHeads = ""
    private var hiddenSectors = ""
    private var totalSectorsInFilesystem = ""
    private var logicalDriveNumber = ""
    private var reserved = ""
    private var extendedSignature = ""
    private var serialNumber = ""
    private var volumeLabel = ""
    private var filesystemType = ""
    private var bottomBootstrap = ""
    private var signature = ""

    private var bootstrapHex = ""
    private var versionHex = ""
    private var bytesPerSectorHex = ""
    private var sectorsPerClusterHex = ""
    private var reservedSectorsHex = ""

    private var bytesPerSectorDec = 0
    private var sectorsPerClusterDec = 0
    private var reservedSectorsDec = 0

    private var reservedSectorsText = ""

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

    }

    override fun toString(): String {
        return "Fat12BootSector(" +
                "Bootstrap: 0..2: '$bootstrap' - '$bootstrapHex', " +
                "OEM name/version: 3..10: '$version' - '$versionHex', " +
                "Number of bytes per sector: 11..12: '$bytesPerSector' - '$bytesPerSectorHex' - '$bytesPerSectorDec', " +
                "Number of sectors per cluster: 13: '$sectorsPerCluster' - '$sectorsPerClusterHex' - '$sectorsPerClusterDec', " +
                "Number of reserved sectors: 14..15: '$reservedSectors' - '$reservedSectorsHex'" +
                (if (reservedSectorsText.equals("")) "" else " - '\$reservedSectorsText'") + ", " +
                "fatCopies='$fatCopies', " +
                "directoryEntries='$directoryEntries', " +
                "sectorsInFilesystem='$sectorsInFilesystem', " +
                "mediaDescriptorType='$mediaDescriptorType', " +
                "sectorsPerFat='$sectorsPerFat', " +
                "sectorsPerTrack='$sectorsPerTrack', " +
                "numberOfHeads='$numberOfHeads', " +
                "hiddenSectors='$hiddenSectors', " +
                "totalSectorsInFilesystem='$totalSectorsInFilesystem', " +
                "logicalDriveNumber='$logicalDriveNumber', " +
                "reserved='$reserved', " +
                "extendedSignature='$extendedSignature', " +
                "serialNumber='$serialNumber', " +
                "volumeLabel='$volumeLabel', " +
                "filesystemType='$filesystemType', " +
                "bottomBootstrap='$bottomBootstrap', " +
                "signature='$signature', " +
                ")"
    }


}