package com.example.ghostapp.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ghostapp.MapActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class MapServices {
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    fun getUserLocationWithoutMap(context: Context, activity: Activity, onLocationReceived: (LatLng?) -> Unit) {
        if (!hasLocationPermission(context)) {
            requestLocationPermission(activity)
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    onLocationReceived(userLatLng)
                } else {
                    Toast.makeText(context, "Impossible d'obtenir votre position actuelle", Toast.LENGTH_SHORT).show()
                    onLocationReceived(null)
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, "Erreur d'accès à la localisation", Toast.LENGTH_SHORT).show()
            onLocationReceived(null)
            return
        }
    }


}