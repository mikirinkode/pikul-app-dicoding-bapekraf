package com.mikirinkode.pikul.feature.customer.maps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
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
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.maps.SellingPlace
import com.mikirinkode.pikul.databinding.FragmentMapsBinding
import com.mikirinkode.pikul.feature.merchant.maps.SellingPlaceAdapter
import com.mikirinkode.pikul.utils.MapsHelper
import com.mikirinkode.pikul.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * TODO:
 * SHOW USER LOCATION AND SAVE IT TO FIRESTORE
 */
/**
 * TODO:
 * ADD LOADING INDICATOR
 * ON ERROR INDICATOR
 */
@AndroidEntryPoint
class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapClickListener {

    @Inject
    lateinit var pref: LocalPreference

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    // used for detect user location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // used for maps
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private var userMarker: Marker? = null

    private val viewModel: MapsViewModel by viewModels()

    private lateinit var adapter: SellingPlaceAdapter

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

        initView()
        observeSellingPlaces()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun initView() {
        binding.apply {
            adapter = SellingPlaceAdapter(pref)

            rvSellingPlaces.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvSellingPlaces.adapter = adapter


            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(rvSellingPlaces)

            rvSellingPlaces.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val position =
                            (rvSellingPlaces.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()

                        if (position >= 0) {
                            val item: SellingPlace = adapter.getDataByPosition(position)
                            val latLng = MapsHelper.getLatLngFromString(item.coordinate)
                            if (latLng != null) {
                                MapsHelper.navigateToLocation(mMap, latLng)
                            }
                        }
                    }
                }
            })
        }
    }

    private fun observeSellingPlaces() {
        viewModel.getSellingPlaces().observe(viewLifecycleOwner) { result ->
            when (result) {
                is PikulResult.Loading -> {}
                is PikulResult.LoadingWithProgress -> {}
                is PikulResult.Error -> {
                    Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is PikulResult.Success -> {
                    if (result.data.isNotEmpty()) {
                        adapter.setData(result.data)
                        createAllSellingPlaces(result.data)
                    }
                }
            }
        }
    }

    private fun createAllSellingPlaces(list: List<SellingPlace>) {
        for (place in list) {
            val latLng = MapsHelper.getLatLngFromString(place.coordinate)
            if (latLng != null) {
                val marker =
                    MapsHelper.createSellingPlaceMarker(latLng, place.placeId ?: "")
                mMap.addMarker(marker)
            }
        }
    }

    private fun observeSavedLocation() {
        // get last location
        val latitude = pref.getString(LocalPreferenceConstants.USER_LAST_LATITUDE)
        val longitude = pref.getString(LocalPreferenceConstants.USER_LAST_LONGITUDE)

        if (!latitude.isNullOrEmpty() && !longitude.isNullOrEmpty()) {
            handleUserMarker(latitude.toDouble(), longitude.toDouble())
        }

        // observe for real location
        // check the permission
        if (PermissionHelper.isLocationPermissionGranted(requireContext())) {
            observeNewLocation()
        } else {
            PermissionHelper.requestLocationPermission(requireActivity())
            observeNewLocation()
        }
    }

    private fun observeNewLocation() {
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
                        // save to local
                        pref.saveString(
                            LocalPreferenceConstants.USER_LAST_LATITUDE,
                            it.latitude.toString()
                        )
                        pref.saveString(
                            LocalPreferenceConstants.USER_LAST_LONGITUDE,
                            it.longitude.toString()
                        )

                        handleUserMarker(it.latitude, it.longitude)
                        val isLoggedIn = pref.getBoolean(LocalPreferenceConstants.IS_LOGGED_IN)

                        if (isLoggedIn == true) {
                            viewModel.saveUserCoordinate(it.latitude, it.longitude)
                        }
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

    private fun handleUserMarker(latitude: Double, longitude: Double) {
        // check for previous marker
        if (userMarker != null) {
            userMarker?.remove()
            userMarker = null
        }
        val userLatLng = LatLng(latitude, longitude)
        val marker = mMap.addMarker(MapsHelper.createUserMarker(userLatLng))
        userMarker = marker

        MapsHelper.navigateToLocation(mMap, userLatLng)
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)

        val initialLocation = LatLng(-4.375726916664182, 117.53723749844212)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 4f))

        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = true

        observeSavedLocation() // TODO
    }

    override fun onMapClick(p0: LatLng) {

    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val placeId = marker.title
        if (placeId != null) {
            val position = adapter.getDataPositionById(placeId)
            if (position != RecyclerView.NO_POSITION) {
                binding.rvSellingPlaces.smoothScrollToPosition(position)
            }
        }
        MapsHelper.navigateToLocation(mMap, marker.position)
        return true
    }


    private fun onClickAction() {
        binding.apply {
            fabUserLocation.setOnClickListener {
                observeNewLocation()
            }
        }
    }


    // handle the request permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.e("MapsFragment", "onRequestPermissionsResult")
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