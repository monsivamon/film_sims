package com.tqmane.filmsim.util

import com.tqmane.filmsim.BuildConfig
import java.io.InputStream
import java.security.MessageDigest

object AssetDecryptor {

    private val masterKey: String = BuildConfig.ASSET_KEY

    /**
     * Wrap an InputStream with an RC4 decryption layer on the fly.
     *
     * Format: [8-byte salt][encrypted payload]
     *
     * Key derivation: SHA-256(masterKey || salt)
     * This must match the scheme used when the .enc assets were produced.
     * Changing this requires re-encrypting every asset file.
     */
    fun decryptStream(inputStream: InputStream): InputStream {
        if (BuildConfig.ASSET_KEY == "placeholder_key" || masterKey.isEmpty()) {
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

        if (bytesRead < 8) return inputStream // File too small — not a valid encrypted asset

        // Derive 32-byte key: SHA-256(masterKey || salt)
        val fileKey = deriveKey(masterKey, salt)

        // Initialize RC4 KSA
        val sBox = IntArray(256) { it }
        var j = 0
        for (i in 0 until 256) {
            j = (j + sBox[i] + (fileKey[i % fileKey.size].toInt() and 0xFF)) and 0xFF
            val temp = sBox[i]; sBox[i] = sBox[j]; sBox[j] = temp
        }

        return object : InputStream() {
            private var iState = 0
            private var jState = 0

            override fun read(): Int {
                val b = inputStream.read()
                if (b == -1) return -1
                iState = (iState + 1) and 0xFF
                jState = (jState + sBox[iState]) and 0xFF
                val temp = sBox[iState]; sBox[iState] = sBox[jState]; sBox[jState] = temp
                return b xor sBox[(sBox[iState] + sBox[jState]) and 0xFF]
            }

            override fun read(b: ByteArray, off: Int, len: Int): Int {
                val readLen = inputStream.read(b, off, len)
                if (readLen <= 0) return readLen
                for (offset in 0 until readLen) {
                    iState = (iState + 1) and 0xFF
                    jState = (jState + sBox[iState]) and 0xFF
                    val temp = sBox[iState]; sBox[iState] = sBox[jState]; sBox[jState] = temp
                    b[off + offset] = (b[off + offset].toInt() xor sBox[(sBox[iState] + sBox[jState]) and 0xFF]).toByte()
                }
                return readLen
            }

            override fun close() = inputStream.close()
        }
    }

    /**
     * Derive a 32-byte key from the master key and per-file salt.
     * Algorithm: SHA-256(masterKeyBytes || salt)
     *
     * WARNING: Do not change this algorithm without also re-encrypting all .enc assets.
     */
    private fun deriveKey(masterKey: String, salt: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(masterKey.toByteArray(Charsets.UTF_8))
        md.update(salt)
        return md.digest()
    }
}

