package com.example.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class DetectedLocation(
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String,
    val locality: String = "",
    val adminArea: String = "",
    val postalCode: String = "",
    val isGeocoded: Boolean = true
)

sealed class LocationDetectResult {
    data class Success(val location: DetectedLocation) : LocationDetectResult()
    data class Error(val message: String) : LocationDetectResult()
}

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun detectCurrentLocation(context: Context): LocationDetectResult {
        if (!hasLocationPermission(context)) {
            return LocationDetectResult.Error("Location permission is required to detect your current address.")
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return LocationDetectResult.Error("Location service is unavailable on this device.")

        val gpsEnabled = runCatching { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)
        val netEnabled = runCatching { lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)

        if (!gpsEnabled && !netEnabled) {
            return LocationDetectResult.Error("Device GPS / Location is turned off. Please enable Location services and try again.")
        }

        // Try live current location first (prefer GPS if enabled, then Network)
        var bestLocation: Location? = null
        val providersToTry = buildList {
            if (gpsEnabled) add(LocationManager.GPS_PROVIDER)
            if (netEnabled) add(LocationManager.NETWORK_PROVIDER)
        }

        for (provider in providersToTry) {
            val fresh = requestSingleCurrentLocation(context, lm, provider, timeoutMs = 8000L)
            if (fresh != null) {
                bestLocation = chooseBetterLocation(bestLocation, fresh)
                if (provider == LocationManager.GPS_PROVIDER) break
            }
        }

        // Fall back to the freshest last-known location across providers
        if (bestLocation == null) {
            val lastKnownCandidates = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            ).mapNotNull { provider ->
                runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
            }
            bestLocation = lastKnownCandidates.maxByOrNull { it.time }
        }

        val resolved = bestLocation
            ?: return LocationDetectResult.Error(
                "Unable to acquire live GPS signal right now. Please move near an open area or enter your address manually."
            )

        val geocodedAddress = reverseGeocode(context, resolved.latitude, resolved.longitude)
        return if (geocodedAddress != null) {
            LocationDetectResult.Success(
                formatAddressResult(resolved.latitude, resolved.longitude, geocodedAddress)
            )
        } else {
            val coordsOnly = String.format(
                Locale.US,
                "GPS Coordinates: %.5f, %.5f",
                resolved.latitude,
                resolved.longitude
            )
            LocationDetectResult.Success(
                DetectedLocation(
                    latitude = resolved.latitude,
                    longitude = resolved.longitude,
                    formattedAddress = coordsOnly,
                    isGeocoded = false
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleCurrentLocation(
        context: Context,
        lm: LocationManager,
        provider: String,
        timeoutMs: Long
    ): Location? = withTimeoutOrNull(timeoutMs) {
        suspendCancellableCoroutine { cont ->
            val signal = CancellationSignal()
            cont.invokeOnCancellation { signal.cancel() }
            try {
                LocationManagerCompat.getCurrentLocation(
                    lm,
                    provider,
                    signal,
                    ContextCompat.getMainExecutor(context)
                ) { loc ->
                    if (cont.isActive) {
                        cont.resume(loc)
                    }
                }
            } catch (_: Exception) {
                if (cont.isActive) cont.resume(null)
            }
        }
    }

    private fun chooseBetterLocation(current: Location?, candidate: Location): Location {
        if (current == null) return candidate
        val timeDelta = candidate.time - current.time
        if (timeDelta > 60_000L) return candidate
        if (timeDelta < -60_000L) return current
        return if (candidate.hasAccuracy() && current.hasAccuracy()) {
            if (candidate.accuracy <= current.accuracy) candidate else current
        } else {
            candidate
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(context: Context, lat: Double, lon: Double): Address? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            withTimeoutOrNull(5000L) {
                suspendCancellableCoroutine { cont ->
                    try {
                        geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (cont.isActive) cont.resume(addresses.firstOrNull())
                            }

                            override fun onError(errorMessage: String?) {
                                if (cont.isActive) cont.resume(null)
                            }
                        })
                    } catch (_: Exception) {
                        if (cont.isActive) cont.resume(null)
                    }
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                try {
                    geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    internal fun formatAddressResult(lat: Double, lon: Double, address: Address): DetectedLocation {
        val parts = linkedSetOf<String>()
        val line0 = address.getAddressLine(0)?.trim().orEmpty()
        if (line0.isNotBlank()) {
            parts.add(line0)
        } else {
            listOfNotNull(
                address.featureName?.takeIf { it.isNotBlank() && it != address.thoroughfare },
                address.thoroughfare?.takeIf { it.isNotBlank() },
                address.subLocality?.takeIf { it.isNotBlank() },
                address.locality?.takeIf { it.isNotBlank() },
                address.subAdminArea?.takeIf { it.isNotBlank() && it != address.locality },
                address.adminArea?.takeIf { it.isNotBlank() },
                address.postalCode?.takeIf { it.isNotBlank() },
                address.countryName?.takeIf { it.isNotBlank() }
            ).forEach { parts.add(it) }
        }

        val coords = String.format(Locale.US, "%.5f, %.5f", lat, lon)
        val formatted = if (parts.isNotEmpty()) {
            "${parts.joinToString(", ")} (GPS: $coords)"
        } else {
            "GPS Coordinates: $coords"
        }

        return DetectedLocation(
            latitude = lat,
            longitude = lon,
            formattedAddress = formatted,
            locality = address.locality ?: address.subAdminArea ?: "",
            adminArea = address.adminArea ?: "",
            postalCode = address.postalCode ?: "",
            isGeocoded = parts.isNotEmpty()
        )
    }
}
