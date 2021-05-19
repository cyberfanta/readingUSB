package com.github.mjdev.libaums.fs.fat12

import java.io.*

class Fat12SaveFile (path: String, fileName: String, fat12FileData: Fat12FileData) {
    init {
        val file = File(path, fileName)
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(fat12FileData.cluster.toByteArray())
        fileOutputStream.close()

//        try {
//            val bufferedWriter = BufferedWriter(FileWriter(file))
//            val size = fat12FileData.clusterAmount
//
//            for (i in 0 until size)
//                bufferedWriter.write(fat12FileData.cluster[i])
//
//            bufferedWriter.close()
//        } catch (ignored: IOException) {
//        }
    }
}