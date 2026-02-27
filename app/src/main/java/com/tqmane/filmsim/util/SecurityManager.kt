package com.tqmane.filmsim.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.util.Base64
import android.util.Log
import com.tqmane.filmsim.BuildConfig
import java.io.File
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

    // Cached result to avoid expensive re-computation on every call.
    // Re-verified periodically via `invalidateCache()`.
    @Volatile
    private var cachedTrustResult: Boolean? = null
    @Volatile
    private var lastCheckTimestamp: Long = 0L
    private const val CACHE_TTL_MS = 60_000L // Re-verify every 60 seconds

    /**
     * Comprehensive environment trust check.
     * Combines signature verification + anti-tamper environment checks.
     *
     * In DEBUG builds, always returns true and logs diagnostics.
     * In RELEASE builds, enforces all checks strictly.
     */
    fun isEnvironmentTrusted(context: Context): Boolean {
        if (BuildConfig.DEBUG) return true

        val now = System.currentTimeMillis()
        val cached = cachedTrustResult
        if (cached != null && (now - lastCheckTimestamp) < CACHE_TTL_MS) {
            return cached
        }

        val result = verifySignature(context) &&
                !isRootedDevice() &&
                !isHookingFrameworkPresent() &&
                !isDebuggerAttached(context)

        cachedTrustResult = result
        lastCheckTimestamp = now

        if (!result) {
            Log.e(TAG, "Environment trust check FAILED")
        }
        return result
    }

    /**
     * Force re-evaluation on next call to [isEnvironmentTrusted].
     */
    fun invalidateCache() {
        cachedTrustResult = null
        lastCheckTimestamp = 0L
    }

    // ─── Signature verification ─────────────────────────────

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

    // ─── Root detection ─────────────────────────────────────

    /**
     * Checks for common indicators of a rooted device.
     * Detects: su binary, Magisk, common root management apps.
     */
    private fun isRootedDevice(): Boolean {
        // Check for su binary in common locations
        val suPaths = arrayOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/su", "/su/bin/su",
            // Magisk paths
            "/sbin/.magisk", "/data/adb/magisk"
        )
        for (path in suPaths) {
            if (File(path).exists()) return true
        }

        // Check dangerous system properties
        try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val result = process.inputStream.bufferedReader().readText().trim()
            process.destroy()
            if (result.isNotEmpty()) return true
        } catch (_: Exception) {
            // Expected on non-rooted devices
        }

        return false
    }

    // ─── Hooking framework detection ────────────────────────

    /**
     * Detects Frida, Xposed, and other common hooking frameworks.
     * Checks: loaded native libraries, stack traces, well-known package names.
     */
    private fun isHookingFrameworkPresent(): Boolean {
        // Check for Frida server (default port or named pipes)
        val fridaIndicators = arrayOf(
            "frida-server", "frida-agent", "frida-gadget",
            "gmain", "linjector"
        )

        // Check loaded native libraries via /proc/self/maps
        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                val content = mapsFile.readText()
                for (indicator in fridaIndicators) {
                    if (content.contains(indicator, ignoreCase = true)) return true
                }
                // Xposed framework libraries
                if (content.contains("XposedBridge", ignoreCase = true)) return true
                if (content.contains("libxposed", ignoreCase = true)) return true
            }
        } catch (_: Exception) {
            // Some ROMs restrict /proc access
        }

        // Check stack trace for Xposed hooks
        try {
            val stackTrace = Thread.currentThread().stackTrace
            for (element in stackTrace) {
                val className = element.className
                if (className.contains("de.robv.android.xposed", ignoreCase = true)) return true
                if (className.contains("com.saurik.substrate", ignoreCase = true)) return true
            }
        } catch (_: Exception) {
            // Ignore
        }

        return false
    }

    // ─── Debugger detection ─────────────────────────────────

    /**
     * Checks if a debugger is attached or if the app is flagged as debuggable.
     */
    private fun isDebuggerAttached(context: Context): Boolean {
        // Check Java debugger
        if (Debug.isDebuggerConnected()) return true

        // Check if the app has debuggable flag set (re-packaged APK)
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName, 0
            )
            if (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                // In release builds, a debuggable flag means tampered APK
                return !BuildConfig.DEBUG
            }
        } catch (_: Exception) {
            // Ignore
        }

        return false
    }
}
