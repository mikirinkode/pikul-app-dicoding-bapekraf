package com.mikirinkode.pikul.feature.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.databinding.ActivityDetailPickupPointMapsBinding
import com.mikirinkode.pikul.utils.MapsHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailPickupPointMapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {

    private val binding: ActivityDetailPickupPointMapsBinding by lazy {
        ActivityDetailPickupPointMapsBinding.inflate(layoutInflater)
    }

    private lateinit var mMap: GoogleMap

    companion object {
        const val EXTRA_INTENT_COORDINATES = "EXTRA_INTENT_COORDINATES"
        const val EXTRA_INTENT_ADDRESS = "EXTRA_INTENT_ADDRESS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = true

        val coordinates = intent.getStringExtra(EXTRA_INTENT_COORDINATES)
        val address = intent.getStringExtra(EXTRA_INTENT_ADDRESS)
        binding.tvMerchantAddress.text = address

        val latLng = MapsHelper.getLatLngFromString(coordinates)

        if (latLng != null) {
            mMap.addMarker(MapsHelper.createSellingPlaceMarker(latLng, ""))
            MapsHelper.navigateToLocation(mMap, latLng)
            binding.tvMerchantAddress.setOnClickListener {
                MapsHelper.navigateToLocation(mMap, latLng)
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        MapsHelper.navigateToLocation(mMap, marker.position)
        return true
    }
}