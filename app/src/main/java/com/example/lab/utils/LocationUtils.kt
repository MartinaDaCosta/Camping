package com.example.lab.utils

import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.example.lab.data.Campings
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

@SuppressLint("MissingPermission")
fun getUserLocation(
    context: android.content.Context,
    onResult: (Location?) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location -> onResult(location) }
        .addOnFailureListener { onResult(null) }
}

fun geocodeCamping(
    context: android.content.Context,
    camping: Campings,
    onResult: (Double?, Double?) -> Unit
) {
    if (!Geocoder.isPresent()) { onResult(null, null); return }

    val query = listOf(camping.direccion, camping.municipio, camping.provincia, "España")
        .filter { it.isNotBlank() }.joinToString(", ")

    val geocoder = Geocoder(context, Locale.getDefault())

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<android.location.Address>) {
                val address = addresses.firstOrNull()
                onResult(address?.latitude, address?.longitude)
            }
            override fun onError(errorMessage: String?) { onResult(null, null) }
        })
    } else {
        try {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query, 1)
            val address = addresses?.firstOrNull()
            onResult(address?.latitude, address?.longitude)
        } catch (e: Exception) { onResult(null, null) }
    }
}

fun calculateDistanceKm(
    userLat: Double, userLon: Double,
    campingLat: Double, campingLon: Double
): Float {
    val results = FloatArray(1)
    Location.distanceBetween(userLat, userLon, campingLat, campingLon, results)
    return results[0] / 1000f
}