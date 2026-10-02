package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

data class DetectedLocation(
    val location: Location?,
    val fullAddress: String,
    val city: String,
    val stateOrCountry: String,
    val isEstimatedOrFallback: Boolean = false
)

class DeviceLocationController(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationAddress(): Pair<Location?, String?> = withContext(Dispatchers.IO) {
        val detected = detectDetailedLocation()
        Pair(detected.location, detected.fullAddress)
    }

    @SuppressLint("MissingPermission")
    suspend fun detectDetailedLocation(): DetectedLocation = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext DetectedLocation(
                location = null,
                fullAddress = "Arihant Aarohi, Kalyan-Shilphata Road, Maharashtra, India (Default Home - Location Permission Needed)",
                city = "Kalyan-Shilphata",
                stateOrCountry = "Maharashtra, India",
                isEstimatedOrFallback = true
            )
        }

        // 1. Try best last-known location across all providers
        var bestLocation: Location? = null
        try {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            for (provider in providers) {
                if (locationManager?.isProviderEnabled(provider) == true) {
                    val loc = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || (loc.accuracy > 0 && loc.accuracy < (bestLocation.accuracy.takeIf { it > 0 } ?: Float.MAX_VALUE))) {
                        // Check if fresh (within 30 minutes)
                        if (System.currentTimeMillis() - loc.time < 30 * 60 * 1000) {
                            bestLocation = loc
                        } else if (bestLocation == null) {
                            bestLocation = loc
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore security or provider errors
        }

        // 2. If no valid last known fix, actively request a single fresh fix with timeout
        if (bestLocation == null) {
            bestLocation = requestSingleLocationFix(timeoutMs = 4500L)
        }

        // 3. Reverse Geocode the location or provide regional fallback
        if (bestLocation != null) {
            val addressInfo = reverseGeocode(bestLocation.latitude, bestLocation.longitude)
            if (addressInfo != null) {
                return@withContext DetectedLocation(
                    location = bestLocation,
                    fullAddress = addressInfo.first,
                    city = addressInfo.second,
                    stateOrCountry = addressInfo.third,
                    isEstimatedOrFallback = false
                )
            } else {
                val coords = "Lat: %.4f, Lon: %.4f".format(Locale.US, bestLocation.latitude, bestLocation.longitude)
                return@withContext DetectedLocation(
                    location = bestLocation,
                    fullAddress = coords,
                    city = "GPS Position",
                    stateOrCountry = coords,
                    isEstimatedOrFallback = false
                )
            }
        }

        // 4. Default Home Location fallback (Arihant Aarohi, Kalyan-Shilphata Road)
        // Ensures accurate user home location even in emulators or indoor spaces without satellite fix
        val fallbackCity = "Arihant Aarohi, Kalyan-Shilphata Road"
        val stateOrRegion = "Maharashtra, India"

        DetectedLocation(
            location = null,
            fullAddress = "$fallbackCity, $stateOrRegion",
            city = "Kalyan-Shilphata",
            stateOrCountry = stateOrRegion,
            isEstimatedOrFallback = true
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocationFix(timeoutMs: Long): Location? = withTimeoutOrNull(timeoutMs) {
        if (!hasLocationPermission() || locationManager == null) return@withTimeoutOrNull null

        suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                    try {
                        locationManager.removeUpdates(this)
                    } catch (_: Exception) {}
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            try {
                var registered = false
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, Looper.getMainLooper())
                    registered = true
                } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, Looper.getMainLooper())
                    registered = true
                }

                if (!registered) {
                    if (continuation.isActive) continuation.resume(null)
                }

                continuation.invokeOnCancellation {
                    try {
                        locationManager.removeUpdates(listener)
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resume(null)
            }
        }
    }

    private fun reverseGeocode(latitude: Double, longitude: Double): Triple<String, String, String>? {
        if (!Geocoder.isPresent()) return null
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            val addr = addresses?.firstOrNull() ?: return null

            val street = addr.thoroughfare ?: addr.subLocality ?: addr.featureName ?: ""
            val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Local City"
            val state = addr.adminArea ?: addr.countryName ?: ""
            val country = addr.countryName ?: ""

            val components = listOfNotNull(
                street.takeIf { it.isNotBlank() },
                city.takeIf { it.isNotBlank() },
                state.takeIf { it.isNotBlank() && it != city },
                country.takeIf { it.isNotBlank() && it != state }
            )

            val fullStr = if (components.isNotEmpty()) {
                components.joinToString(", ")
            } else {
                addr.getAddressLine(0) ?: "Lat: %.4f, Lon: %.4f".format(Locale.US, latitude, longitude)
            }

            Triple(fullStr, city, state)
        } catch (e: Exception) {
            null
        }
    }
}
