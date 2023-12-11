package com.uni.astro.activitesfragments.comments.voice

import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.IOException

object MP3Encoder {
    @JvmStatic
    fun encodeMP3ToBase64(mp3File: File): String? {
        try {
            val inputStream = FileInputStream(mp3File)
            val mp3Bytes = ByteArray(mp3File.length().toInt())
            val bytesRead = inputStream.read(mp3Bytes)
            inputStream.close()
            if (bytesRead > 0) {
                return Base64.encodeToString(mp3Bytes, 0, bytesRead, Base64.DEFAULT)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}
