package com.tqmane.filmsim.di

import android.content.Context
import com.tqmane.filmsim.util.ReleaseInfo
import com.tqmane.filmsim.util.UpdateChecker as UpdateCheckerObj
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injectable wrapper around the static [UpdateCheckerObj].
 * Holds the application [Context] and a pre-configured [OkHttpClient] (with certificate pinning
 * from [NetworkModule]) so that no duplicate HTTP client instances exist in the app.
 */
@Singleton
class UpdateCheckerWrapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient
) {
    suspend fun checkForUpdate(force: Boolean = false): ReleaseInfo? =
        UpdateCheckerObj.checkForUpdate(context, httpClient, force)
}
