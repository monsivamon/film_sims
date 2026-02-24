package com.tqmane.filmsim.util

import android.content.Context
import android.graphics.Typeface
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream

object AssetUtil {
    /**
     * Helper to open an asset that might be encrypted.
     * Handles both dynamically listed paths (already ending in .enc) and hardcoded paths.
     */
    fun openAsset(context: Context, path: String): InputStream {
        // 1. If the path already has .enc, just open and decrypt
        if (path.endsWith(".enc", ignoreCase = true)) {
            val stream = context.assets.open(path)
            return AssetDecryptor.decryptStream(stream)
        }
        
        // 2. Try adding .enc for hardcoded paths (e.g., logos, jsons)
        try {
            val stream = context.assets.open("$path.enc")
            return AssetDecryptor.decryptStream(stream)
        } catch (e: FileNotFoundException) {
            // 3. Fallback to unencrypted path if .enc does not exist
            return context.assets.open(path)
        }
    }

    /**
     * Loads a Typeface from an potentially encrypted asset.
     */
    fun loadTypeface(context: Context, path: String): Typeface {
        var tempFile: File? = null
        try {
            val inputStream = openAsset(context, path)
            tempFile = File.createTempFile("font_", ".tmp", context.cacheDir)
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            return Typeface.createFromFile(tempFile)
        } finally {
            tempFile?.delete()
        }
    }
}
