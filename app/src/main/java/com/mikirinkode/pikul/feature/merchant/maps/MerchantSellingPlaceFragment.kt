package com.mikirinkode.pikul.feature.merchant.maps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.databinding.DialogAddNewStopPointBinding
import com.mikirinkode.pikul.databinding.FragmentMerchantSellingPlaceBinding
import com.mikirinkode.pikul.feature.merchant.MerchantMainActivity
import com.mikirinkode.pikul.utils.MapsHelper
import com.mikirinkode.pikul.utils.TimePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MerchantSellingPlaceFragment : Fragment(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapClickListener, MerchantMainActivity.Companion.TimePickerListener {
    private var _binding: FragmentMerchantSellingPlaceBinding? = null
    private val binding get() = _binding!!
    private var dialogBinding: DialogAddNewStopPointBinding? = null

    private val viewModel: MerchantSellingPlaceViewModel by viewModels()

    @Inject
    lateinit var pref: LocalPreference


    private var addSellingPlaceDialog: AlertDialog? = null

    // used for maps
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView

    //    private var selectedCoordinate: LatLng? = null
    private var currentSelectedLocation: Marker? = null
    private var selectTimeFor: String = ""

    companion object {
        private const val START_TIME = "start_time"
        private const val END_TIME = "end_time"
    }

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
        MerchantMainActivity.timePickerListener = this
        // used for dialog
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.txt_dialog_title_add_new_stop_point))
            .setView(dialogBinding?.root)
            .setCancelable(true)
        addSellingPlaceDialog = dialogBuilder.create()
    }

    private fun updateView() {
        binding.apply {
            if (currentSelectedLocation != null) {
                btnAddStopPoint.visibility = View.VISIBLE
            } else {
                btnAddStopPoint.visibility = View.GONE
            }
        }
    }

    private fun dialogOnClickAction() {
        dialogBinding?.apply {
            btnCancel.setOnClickListener {
                addSellingPlaceDialog?.dismiss()
            }
            btnAdd.setOnClickListener {
                var isValid = true
                val name = etStopPointName.text.toString().trim()
                val startTime = etStartTime.text.toString().trim()
                val endTime = etEndTime.text.toString().trim()

                if (name.isEmpty()) {
                    isValid = false
                    etStopPointName.error = getString(R.string.empty_name)
                }

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
                    viewModel.addStopPoint(name, startTime, endTime, coordinate)
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

            tilStartTime.setOnClickListener {
                selectTimeFor = START_TIME
                val timePickerFragmentOne = TimePickerFragment()
                timePickerFragmentOne.show(requireActivity().supportFragmentManager, "TimePicker")
            }

            tilEndTime.setOnClickListener {
                selectTimeFor = END_TIME
                val timePickerFragmentOne = TimePickerFragment()
                timePickerFragmentOne.show(requireActivity().supportFragmentManager, "TimePicker")
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
        MapsHelper.navigateToLocation(mMap, latLng)
        if (currentSelectedLocation != null) {
            currentSelectedLocation?.remove()
        }
        val marker: Marker? = mMap.addMarker(
            MapsHelper.createMarker(
                latLng,
                "Lokasi Pilihan Anda",
                "Lokasi Pilihan Anda"
            )
        )
        currentSelectedLocation = marker

        updateView()
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return true
    }

    override fun onDialogTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        // Siapkan time formatter-nya terlebih dahulu
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)

        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        dialogBinding?.apply {
            if (selectTimeFor == START_TIME) {
                etStartTime.text = "Waktu mulai: ${dateFormat.format(calendar.time)}"
            } else if (selectTimeFor == END_TIME) {
                etEndTime.text = "Waktu selesai: ${dateFormat.format(calendar.time)}"
            }
        }
    }

    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }

            btnAddStopPoint.setOnClickListener {
                addSellingPlaceDialog?.show()
            }
        }
    }

}