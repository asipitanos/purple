package com.example.tides.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class LocationRepository(context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context, Locale.getDefault()) // Create Geocoder instance once

    // This function gets the current location just once.
    // It assumes permissions have already been granted by the UI layer.
    @SuppressLint("MissingPermission") // We check permissions in the UI, not here.
    suspend fun getCurrentLocation(): Location? {
        val cancellationTokenSource = CancellationTokenSource()
        return try {
            // The modern way to get a single location update
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token,
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Performs reverse geocoding to get a place name from coordinates.
     * This must be called from a coroutine.
     * @return A formatted place name string (e.g., "Mountain View, CA") or null if not found.
     */
    suspend fun getPlaceName(
        latitude: Double,
        longitude: Double,
    ): String? {
        // Geocoder can be slow and must run on a background thread.
        return withContext(Dispatchers.IO) {
            try {
                // For Android Tiramisu (API 33) and above, a new callback-based API is used.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Use the modern API with a coroutine wrapper
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            if (continuation.isActive) { // Ensure coroutine is still active
                                val bestResult = addresses.firstOrNull()
                                continuation.resume(formatAddress(bestResult))
                            }
                        }
                    }
                } else {
                    // Use the deprecated but necessary API for older versions
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val bestResult = addresses?.firstOrNull()
                    formatAddress(bestResult)
                }
            } catch (e: Exception) {
                Log.e("LocationRepository", "Geocoder failed", e)
                null // Return null if an error occurs
            }
        }
    }

    // Helper function to build a clean address string
    private fun formatAddress(address: Address?): String? {
        if (address == null) return null

        // Build a readable string from the available address components
        return listOfNotNull(
            address.locality, // City (e.g., "Mountain View")
            address.adminArea, // State or province (e.g., "CA")
        ).joinToString(", ")
            .ifEmpty { address.countryName } // Fallback to country name if city/state are null
    }
}
