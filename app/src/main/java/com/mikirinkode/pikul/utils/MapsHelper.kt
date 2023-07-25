package com.mikirinkode.pikul.utils

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mikirinkode.pikul.R
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sqrt


object MapsHelper {
    fun getLatLngFromString(coordinate: String?): LatLng? {
        var result: LatLng? = null
        val latLngArray = coordinate?.split(",")

        if (latLngArray != null){
            if (latLngArray.size == 2) {
                val latitude = latLngArray[0].trim().toDouble()
                val longitude = latLngArray[1].trim().toDouble()
                val latLng = LatLng(latitude, longitude)
                result = latLng
            }
        }

        return result
    }

    fun createMarker(latLng: LatLng, title: String, snippet: String): MarkerOptions {
        return MarkerOptions().position(latLng).title(title).snippet(snippet)
    }

    fun createSellingPlaceMarker(latLng: LatLng, placeId: String): MarkerOptions {
        return MarkerOptions().position(latLng).title(placeId)
    }

    fun createUserMarker(latLng: LatLng): MarkerOptions {
        val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_user_location_marker)
        return MarkerOptions().position(latLng).title("Lokasi Anda").icon(markerIcon).snippet("test")
    }

    fun navigateToLocation(map: GoogleMap, latLng: LatLng, zoom: Float = 14.0f) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    fun getPlaceDistance(place: LatLng, user: LatLng): Double {
        var p = 0.017453292519943295;
        var result = 0.5 -
                cos((place.latitude - user.latitude) * p) / 2 +
                cos(user.latitude * p) *
                cos(place.latitude * p) *
                (1 - cos((place.longitude - user.longitude) * p)) /
                2;
        return 12742 * asin(sqrt(result));
        
    }
}