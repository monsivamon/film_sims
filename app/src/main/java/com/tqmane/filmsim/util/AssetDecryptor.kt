package com.tqmane.filmsim.util

import com.tqmane.filmsim.BuildConfig
import java.io.InputStream

object AssetDecryptor {
    
    private val masterKeyBytes: ByteArray = BuildConfig.ASSET_KEY.toByteArray(Charsets.UTF_8)
    private val keyLen: Int = masterKeyBytes.size

    /**
     * Wrap an InputStream with an RC4 decryption layer on the fly.
     * The file payload now starts with an 8-byte salt to prevent known-plaintext reuse.
     */
    fun decryptStream(inputStream: InputStream): InputStream {
        if (BuildConfig.ASSET_KEY == "placeholder_key" || keyLen == 0) {
            return inputStream
        }

        // Read 8-byte salt
        val salt = ByteArray(8)
        var bytesRead = 0
        while (bytesRead < 8) {
            val read = inputStream.read(salt, bytesRead, 8 - bytesRead)
            if (read == -1) break
            bytesRead += read
        }
        
        // If file is smaller than salt, this isn't encrypted normally
        if (bytesRead < 8) {
             return inputStream // Fallback
        }
        
        // Derive unique key for this stream: SHA-256(masterKey + salt)
        val md = java.security.MessageDigest.getInstance("SHA-256")
        md.update(masterKeyBytes)
        val fileKey = md.digest(salt)
        
        // Initialize RC4 KSA
        val sBox = IntArray(256) { it }
        var j = 0
        for (i in 0 until 256) {
            j = (j + sBox[i] + (fileKey[i % fileKey.size].toInt() and 0xFF)) and 0xFF
            val temp = sBox[i]
            sBox[i] = sBox[j]
            sBox[j] = temp
        }

        return object : InputStream() {
            private var iState = 0
            private var jState = 0

            override fun read(): Int {
                val b = inputStream.read()
                if (b == -1) return -1
                
                iState = (iState + 1) and 0xFF
                jState = (jState + sBox[iState]) and 0xFF
                val temp = sBox[iState]
                sBox[iState] = sBox[jState]
                sBox[jState] = temp
                
                val k = sBox[(sBox[iState] + sBox[jState]) and 0xFF]
                return b xor k
            }

            override fun read(b: ByteArray, off: Int, len: Int): Int {
                val readLen = inputStream.read(b, off, len)
                if (readLen <= 0) return readLen
                
                for (offset in 0 until readLen) {
                    iState = (iState + 1) and 0xFF
                    jState = (jState + sBox[iState]) and 0xFF
                    val temp = sBox[iState]
                    sBox[iState] = sBox[jState]
                    sBox[jState] = temp
                    
                    val k = sBox[(sBox[iState] + sBox[jState]) and 0xFF]
                    b[off + offset] = (b[off + offset].toInt() xor k).toByte()
                }
                return readLen
            }

            override fun close() {
                inputStream.close()
            }
        }
    }
}
