package com.mikirinkode.pikul.feature.merchant.maps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.data.model.maps.SellingPlace
import com.mikirinkode.pikul.databinding.DialogAddNewStopPointBinding
import com.mikirinkode.pikul.databinding.FragmentMerchantSellingPlaceBinding
import com.mikirinkode.pikul.feature.merchant.MerchantMainActivity
import com.mikirinkode.pikul.utils.MapsHelper
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * TODO: ADD RULES ONLY ALLOW TO CREATE ONE PLACE
 */
/**
 * TODO:
 * ADD LOADING INDICATOR
 * ON ERROR INDICATOR
 */
@AndroidEntryPoint
class MerchantSellingPlaceFragment : Fragment(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private var _binding: FragmentMerchantSellingPlaceBinding? = null
    private val binding get() = _binding!!
    private var dialogBinding: DialogAddNewStopPointBinding? = null

    private val viewModel: MerchantSellingPlaceViewModel by viewModels()

    @Inject
    lateinit var pref: LocalPreference

    private val user: UserAccount? by lazy {
        pref?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
    }

    private var addSellingPlaceDialog: AlertDialog? = null

    // used for maps
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView

    //    private var selectedCoordinate: LatLng? = null
    private var currentSelectedLocation: Marker? = null
    private var currentSelectedAddress: String = ""
    private var totalSellingPlace: Int = 0

    private val args: MerchantSellingPlaceFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMerchantSellingPlaceBinding.inflate(inflater, container, false)
        dialogBinding = DialogAddNewStopPointBinding.inflate(layoutInflater)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeUserPlaces()
        observeSellingPlaces()
        onClickAction()
        dialogOnClickAction()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {
        val provinces = resources.getStringArray(R.array.provinces)
        val arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            provinces
        )
        val userProvince = user?.province
        dialogBinding?.actvProvince?.setAdapter(arrayAdapter)
        dialogBinding?.actvProvince?.setText(userProvince)


        // used for dialog
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.txt_dialog_title_add_new_stop_point))
            .setView(dialogBinding?.root)
            .setCancelable(true)
        addSellingPlaceDialog = dialogBuilder.create()
    }

    private fun observeUserPlaces() {
        viewModel.getUserPlaceCoordinates().observe(viewLifecycleOwner) { result ->
            when (result) {
                is PikulResult.Loading -> {}
                is PikulResult.LoadingWithProgress -> {}
                is PikulResult.Error -> {
                    Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is PikulResult.Success -> {
                    createAllUserPlaces(result.data)
                }
            }
        }
    }

    private fun observeSellingPlaces() {
        viewModel.getOwnerSellingPlaces().observe(viewLifecycleOwner) { result ->
            when (result) {
                is PikulResult.Loading -> {}
                is PikulResult.LoadingWithProgress -> {}
                is PikulResult.Error -> {
                    Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is PikulResult.Success -> {
                    totalSellingPlace = result.data.size

                    if (result.data.isNotEmpty()) {
                        val sellingPlace = result.data.first()
                        val latLng = MapsHelper.getLatLngFromString(sellingPlace.coordinate)
                        if (latLng != null) {
                            val markerOptions =
                                MapsHelper.createSellingPlaceMarker(
                                    latLng,
                                    sellingPlace.placeId ?: ""
                                )
                            val marker = mMap.addMarker(markerOptions)

                            binding.apply {
                                cardSellingPointDetail.visibility = View.VISIBLE
                                tvPlaceNote.text = sellingPlace.placeNoteForCustomer
                                tvPlaceAddress.text = sellingPlace.placeAddress
                                tvTime.text = "${sellingPlace.startTime} - ${sellingPlace.endTime}"

                                cardSellingPointDetail.setOnClickListener {
                                    val latLng =
                                        MapsHelper.getLatLngFromString(sellingPlace.coordinate)
                                    if (latLng != null) {
                                        MapsHelper.navigateToLocation(mMap, latLng)
                                    }
                                }

                                btnDelete.setOnClickListener {
                                    if (marker != null) {
                                        onDeletePlaceClicked(marker, sellingPlace.placeId ?: "")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createAllUserPlaces(coordinates: List<String>) {
        for (coordinate in coordinates) {
            if (coordinate.isNotEmpty()) {
                val latLng = MapsHelper.getLatLngFromString(coordinate)
                if (latLng != null) {
                    val marker =
                        MapsHelper.createCustomerMarker(latLng)
                    mMap.addMarker(marker)
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

    private fun updateView() {
        binding.apply {
            if (currentSelectedLocation != null) {
                fabAddSellingPlace.visibility = View.VISIBLE
            } else {
                fabAddSellingPlace.visibility = View.GONE
            }
        }
    }

    private fun dialogOnClickAction() {
        dialogBinding?.apply {
            btnCancel.setOnClickListener {
                addSellingPlaceDialog?.dismiss()
            }
            btnAdd.setOnClickListener {
                if (totalSellingPlace > 0) {
                    Toast.makeText(
                        requireContext(),
                        "Anda hanya dapat menambahkan 1 Lokasi",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    var isValid = true
                    val placeNote = etPlaceNote.text.toString().trim()
//                    val province = actvProvince.text.toString().trim()
                    val address = etAddress.text.toString().trim()
                    val startTime = etStartTime.text.toString().trim()
                    val endTime = etEndTime.text.toString().trim()


                    if (startTime.isEmpty() || endTime.isEmpty() || startTime == "Waktu Mulai" || endTime == "Waktu Selesai") {
                        isValid = false
                        Toast.makeText(
                            requireContext(),
                            "Mohon pilih waktu mulai dan selesai",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (currentSelectedLocation == null) {
                        isValid = false
                        Toast.makeText(
                            requireContext(),
                            "Mohon pilih lokasi terlebih dahulu",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (isValid) {
                        val coordinate =
                            "${currentSelectedLocation?.position?.latitude}, ${currentSelectedLocation?.position?.longitude}"
                        viewModel.addStopPoint(
                            args.businessId,
                            placeNote,
                            address,
                            startTime,
                            endTime,
                            coordinate
                        )
                            .observe(viewLifecycleOwner) { result ->
                                // reset data
                                addSellingPlaceDialog?.dismiss()
                                if (currentSelectedLocation != null) {
                                    currentSelectedLocation?.remove()
                                    currentSelectedLocation = null
                                }
                                updateView()

                                // calculate data
                                when (result) {
                                    is PikulResult.Loading -> {
                                        binding.layoutLoading.visibility = View.VISIBLE
                                    }
                                    is PikulResult.LoadingWithProgress -> {}
                                    is PikulResult.Error -> {
                                        binding.layoutLoading.visibility = View.GONE
                                        Toast.makeText(
                                            requireContext(),
                                            "Gagal menambah data",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    is PikulResult.Success -> {
                                        dialogBinding?.etPlaceNote?.setText("")
                                        dialogBinding?.etStartTime?.setText("")
                                        dialogBinding?.etEndTime?.setText("")
                                        binding.layoutLoading.visibility = View.GONE
                                        Toast.makeText(
                                            requireContext(),
                                            "Berhasil menambah titik berhenti",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                    }
                }
            }

            tilStartTime.setOnClickListener {
                val startTimePicker =
                    MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(12)
                        .setMinute(10)
                        .setTitleText("Pilih Waktu Mulai")
                        .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                        .build()

                startTimePicker.show(childFragmentManager, "START_TIME_PICKER_TAG")
                startTimePicker.addOnPositiveButtonClickListener {
                    val hour = String.format("%02d", startTimePicker.hour)
                    val minute = String.format("%02d", startTimePicker.minute)
                    etStartTime.setText("$hour:$minute")
                }
            }

            tilEndTime.setOnClickListener {
                val endTimePicker =
                    MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(12)
                        .setMinute(10)
                        .setTitleText("Pilih Waktu Selesai")
                        .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                        .build()

                endTimePicker.show(childFragmentManager, "END_TIME_PICKER_TAG")
                endTimePicker.addOnPositiveButtonClickListener {
                    val hour = String.format("%02d", endTimePicker.hour)
                    val minute = String.format("%02d", endTimePicker.minute)
                    etEndTime.setText("$hour:$minute")
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)
        val initialLocation = LatLng(-4.375726916664182, 117.53723749844212)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialLocation))

        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = true
    }

    override fun onMapClick(latLng: LatLng) {
        binding.cardSellingPointDetail.visibility = View.GONE


        MapsHelper.navigateToLocation(mMap, latLng)
        if (currentSelectedLocation != null) {
            currentSelectedLocation?.remove()
        }
        val marker: Marker? = mMap.addMarker(MapsHelper.createMarkerNewSellingPlace(latLng))
        currentSelectedLocation = marker

        updateView()

        viewModel.getAddressFromCoordinates(requireContext(), latLng)
            .observe(viewLifecycleOwner) { result ->
                currentSelectedAddress = result
                dialogBinding?.etAddress?.setText(result)
            }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        binding.cardSellingPointDetail.visibility = View.GONE
        MapsHelper.navigateToLocation(mMap, marker.position)
        if (marker.title != null) {
            viewModel.getSellingPlaceById(marker.title!!).observe(viewLifecycleOwner) { result ->
                if (result != null) {
                    binding.apply {
                        cardSellingPointDetail.visibility = View.VISIBLE
                        tvPlaceNote.text = result.placeNoteForCustomer
                        tvPlaceAddress.text = result.placeAddress
                        tvTime.text = "${result.startTime} - ${result.endTime}"

                        cardSellingPointDetail.setOnClickListener {
                            val latLng = MapsHelper.getLatLngFromString(result.coordinate)
                            if (latLng != null) {
                                MapsHelper.navigateToLocation(mMap, latLng)
                            }
                        }

                        btnDelete.setOnClickListener {
                            onDeletePlaceClicked(marker, result.placeId ?: "")
                        }
                    }
                }
            }
        }
        return true
    }

    private fun onDeletePlaceClicked(marker: Marker, placeId: String) {
        binding.apply {
            viewModel.deleteSellingPlaceById(placeId ?: "")
                .observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is PikulResult.Loading -> {
                            layoutLoading.visibility = View.VISIBLE
                        }
                        is PikulResult.LoadingWithProgress -> {}
                        is PikulResult.Error -> {
                            layoutLoading.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                result.errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is PikulResult.Success -> {
                            totalSellingPlace -= 1
                            marker.remove()
                            cardSellingPointDetail.visibility = View.GONE
                            layoutLoading.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Berhasil Menghapus Data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }
    }


    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }

            fabAddSellingPlace.setOnClickListener {
                if (totalSellingPlace > 0) {
                    Toast.makeText(
                        requireContext(),
                        "Anda hanya dapat menambahkan 1 Lokasi",
                        Toast.LENGTH_LONG
                    ).show()

                } else {
                    if (currentSelectedAddress != "") {
                        dialogBinding?.etAddress?.setText(currentSelectedAddress)
                    }
                    addSellingPlaceDialog?.show()
                }
            }
        }
    }

}