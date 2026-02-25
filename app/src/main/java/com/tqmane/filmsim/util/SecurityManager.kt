package com.tqmane.filmsim.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import com.tqmane.filmsim.BuildConfig
import java.security.MessageDigest

object SecurityManager {
    private const val TAG = "SecurityManager"

    // Release signing hash (SHA-256 Base64 encoded).
    // The debug hash is intentionally excluded from the release binary to reduce
    // the information surface available to reverse engineers.
    private val RELEASE_SIGNATURE_HASH = "07D6Pj199ET0XVf2+Ui/ZB+veLHMPph1mLzMWSAeW/w="

    // Debug-only hash — only compiled into debug builds.
    private val DEBUG_SIGNATURE_HASH = if (BuildConfig.DEBUG) {
        "xgnLChOLC3w983F5Z95nUeMKO14IhATfWBy4tl69ZvM="
    } else {
        null
    }

    /**
     * Verifies if the application's signature matches the expected official developer signature.
     * In DEBUG builds, logs the current hash to help configure it and always returns true.
     * In RELEASE builds, enforces the check strictly — FLAG_DEBUGGABLE alone does not bypass it.
     *
     * @return true if the signature matches a known valid hash. false if tampered.
     */
    @SuppressLint("PackageManagerGetSignatures")
    fun verifySignature(context: Context): Boolean {
        val isDebugBuild = BuildConfig.DEBUG

        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) {
                Log.e(TAG, "No signatures found!")
                return isDebugBuild
            }

            for (signature in signatures) {
                val md = MessageDigest.getInstance("SHA-256")
                md.update(signature.toByteArray())
                val currentHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)

                if (isDebugBuild) {
                    Log.d(TAG, "Current App Signature Hash (SHA-256 Base64): $currentHash")
                }

                if (currentHash == RELEASE_SIGNATURE_HASH) return true
                if (isDebugBuild && currentHash == DEBUG_SIGNATURE_HASH) return true
            }

            Log.e(TAG, "Signature verification failed! The app might be modified.")
            // In release builds, never fall back to returning true on failure.
            return isDebugBuild

        } catch (e: Exception) {
            Log.e(TAG, "Error generating signature hash", e)
            return isDebugBuild
        }
    }
}
