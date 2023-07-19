package com.mikirinkode.pikul.feature.customer.maps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.databinding.FragmentMapsBinding
import com.mikirinkode.pikul.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapClickListener {

    @Inject
    lateinit var preferences: LocalPreference

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    // used for detect user location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // used for maps
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onClickAction()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
//        observeSavedLocation()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)
//        try {
//            // check preference for dark mode
//            val isDark = preferences.getBooleanValues(Preferences.DARK_MODE_PREF)
//            if (isDark) {
//                mMap.setMapStyle(
//                    MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_night)
//                )
//            } else {
//                mMap.setMapStyle(
//                    MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_light)
//                )
//            }
//        } catch (e: Exception) {
//            Log.e("SelectLocation", "${e.message}")
//        }
        val initialLocation = LatLng(-4.375726916664182, 117.53723749844212)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialLocation))

        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = true

        observeSavedLocation()
    }

    override fun onMapClick(p0: LatLng) {

    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return true
    }

    private fun observeSavedLocation(){
        // get last location
        val latitude = preferences.getString(LocalPreferenceConstants.USER_LAST_LATITUDE)
        val longitude = preferences.getString(LocalPreferenceConstants.USER_LAST_LONGITUDE)

        if (!latitude.isNullOrEmpty() && !longitude.isNullOrEmpty()) {
            val marker: Marker? =
                mMap.addMarker(createMarker(LatLng(latitude.toDouble(), longitude.toDouble())))
        }

        // observe for real location
        // check the permission
        if (PermissionHelper.isLocationPermissionGranted(requireContext())) {
            observeNewLocation()
        } else {
            PermissionHelper.requestLocationPermission(requireActivity())
        }
    }

    private fun observeNewLocation(){
        // check the location service
        if (isLocationServiceEnabled(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                PermissionHelper.requestLocationPermission(requireActivity())
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Use the retrieved location to navigate on Google Maps
                    location?.let {
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        val marker = createMarker(userLatLng)
                        preferences.saveString(LocalPreferenceConstants.USER_LAST_LATITUDE, it.latitude.toString())
                        preferences.saveString(LocalPreferenceConstants.USER_LAST_LONGITUDE, it.longitude.toString())
                        mMap.addMarker(marker)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))
//                        navigateToLocation(userLatLng)
                    }
                }
                .addOnFailureListener { exception: Exception ->
                    // Handle location retrieval failure
                    Toast.makeText(
                        requireContext(),
                        "${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            showEnableLocationServiceDialog()
            Toast.makeText(
                requireContext(),
                "Location Service is Disabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isLocationServiceEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showEnableLocationServiceDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.txt_dialog_title_enable_location))
        builder.setMessage(getString(R.string.txt_dialog_desc_enable_location_service))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.txt_dialog_btn_open_settings)) { _, _ ->
                // Open settings screen to enable location services
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun createMarker(latLng: LatLng): MarkerOptions {
        val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_user_location_marker)
        return MarkerOptions().position(latLng).title(getString(R.string.txt_your_location)).icon(markerIcon)
    }

    private fun navigateToLocation(latLng: LatLng, zoom: Float = 14.0f) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }
    
    private fun onClickAction(){}


    // handle the request permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.LOCATION_REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                for (result in grantResults) {
                    if (result == AppCompatActivity.RESULT_OK) {
                        observeNewLocation()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.txt_location_permission_granted),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}