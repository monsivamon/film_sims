package com.tqmane.filmsim.data

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Manages user subscription state and license verification.
 * Note: Server-side validation is temporarily bypassed in debug builds
 * to facilitate local UI testing and layout adjustments.
 */
@Singleton
class ProUserRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "ProUserRepository"
    }

    // Initialize based on build type: unlocked for local debug/UI testing, restricted for release.
    private val isDebugMode = com.tqmane.filmsim.BuildConfig.DEBUG

    private val _isProUser = MutableStateFlow(isDebugMode)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    private val _licenseMismatchVersion = MutableStateFlow<String?>(null)
    val licenseMismatchVersion: StateFlow<String?> = _licenseMismatchVersion.asStateFlow()

    private val _isPermanentLicense = MutableStateFlow(isDebugMode)
    val isPermanentLicense: StateFlow<Boolean> = _isPermanentLicense.asStateFlow()

    /**
     * Verifies the user's Pro status.
     * In debug mode, this grants access automatically so contributors can
     * easily test premium UI components without needing a valid Firebase session.
     */
    suspend fun checkProStatus(email: String?) {
        Log.d(TAG, "Checking pro status for user: $email")
        
        if (com.tqmane.filmsim.BuildConfig.DEBUG) {
            Log.d(TAG, "Debug build detected: granting temporary Pro access for UI testing.")
            _isProUser.value = true
            _licenseMismatchVersion.value = null
            _isPermanentLicense.value = true
            return
        }

        // TODO: Restore actual server-side Firebase verification here for release builds.
        // For now, default to restricted access in non-debug environments.
        _isProUser.value = false
        _licenseMismatchVersion.value = null
        _isPermanentLicense.value = false
    }

    /**
     * Clears the user's Pro status upon sign-out.
     */
    fun clearProStatus() {
        if (com.tqmane.filmsim.BuildConfig.DEBUG) {
            Log.d(TAG, "Debug build detected: maintaining Pro access for offline testing.")
            _isProUser.value = true
            _licenseMismatchVersion.value = null
            _isPermanentLicense.value = true
            return
        }

        _isProUser.value = false
        _licenseMismatchVersion.value = null
        _isPermanentLicense.value = false
    }
}