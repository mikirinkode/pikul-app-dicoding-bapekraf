package com.mikirinkode.pikul.feature.owner.merchant

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.databinding.FragmentJobVacancyBinding
import com.mikirinkode.pikul.databinding.FragmentManageMerchantBinding
import dagger.hilt.android.AndroidEntryPoint

// TODO: UNIMPLEMENTED
@AndroidEntryPoint
class ManageMerchantFragment : Fragment() {
    private var _binding: FragmentManageMerchantBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ManageMerchantViewModel by viewModels()

    private var openJobVacancy: Boolean = false

    private val adapter: MyMerchantAdapter by lazy {
        MyMerchantAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentManageMerchantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        observeBusinessData()
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun initRecyclerView() {
        binding.apply {
            rvMerchants.layoutManager = LinearLayoutManager(requireContext())
            rvMerchants.adapter = adapter
        }
    }

    private fun observeBusinessData() {
        binding.apply {
            viewModel.getMerchantPartnerList().observe(viewLifecycleOwner) { result ->
                when (result) {
                    is PikulResult.Loading -> {
                        layoutLoading.visibility = View.VISIBLE
                    }
                    is PikulResult.LoadingWithProgress -> {}
                    is PikulResult.Error -> {
                        layoutLoading.visibility = View.GONE
                        Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                    is PikulResult.Success -> {
                        layoutLoading.visibility = View.GONE
                        adapter.setData(result.data)
                        if (result.data.isEmpty()){
                            layoutOnEmptyData.visibility = View.VISIBLE
                        } else {
                            layoutOnEmptyData.visibility = View.GONE
                        }
                    }
                }
            }

            viewModel.getBusinessData().observe(viewLifecycleOwner) { result ->
                when (result) {
                    is PikulResult.Loading -> {
                        layoutLoading.visibility = View.VISIBLE
                    }
                    is PikulResult.LoadingWithProgress -> {}
                    is PikulResult.Error -> {
                        layoutLoading.visibility = View.GONE
                        Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                    is PikulResult.Success -> {
                        layoutLoading.visibility = View.GONE
                        val businessData = result.data
                        openJobVacancy = businessData.openJobVacancy == true

                        if (businessData.openJobVacancy == true) {
                            tvOpenJobActive.visibility = View.VISIBLE
                            tvDummyOpenJobActive.visibility = View.VISIBLE
                            tvOpenJobNotActive.visibility = View.GONE
                            tvDummyOpenJobNotActive.visibility = View.GONE
                            btnGoToJobVacancyView.visibility = View.VISIBLE
                        } else {
                            btnGoToJobVacancyView.visibility = View.GONE
                            tvOpenJobActive.visibility = View.GONE
                            tvDummyOpenJobActive.visibility = View.GONE
                            tvOpenJobNotActive.visibility = View.VISIBLE
                            tvDummyOpenJobNotActive.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }


    private fun showOpenJobVacancyDialog() {
        val title = if (openJobVacancy) "Tutuk Lowongan Pekerjaan?" else "Buka Lowongan Pekerjaan?"
        val message =
            if (openJobVacancy) "- Dengan menutup lowongan pekerjaan, akun bisnis anda tidak akan tampil di laman lowongan kerja. \n-Namun, anda tetap dapat menambahkan pedagang untuk menjadi mitra anda." else "- Dengan membuka lowongan pekerjaan, akun bisnis anda akan muncul pada laman lowongan kerja. -\nPedagang dapat menghubungi anda untuk dapat mendaftar pekerjaan."
        val positiveButtonText = if (openJobVacancy) "Tutup" else "Buka"
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(positiveButtonText) { _, _ ->
                viewModel.updateJobVacancy(!openJobVacancy).observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is PikulResult.Loading -> {
                            binding.layoutLoading.visibility = View.VISIBLE
                        }
                        is PikulResult.LoadingWithProgress -> {}
                        is PikulResult.Error -> {}
                        is PikulResult.Success -> {
                            binding.layoutLoading.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Berhasil memperbarui data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                openJobVacancy = !openJobVacancy
            }
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }
        val openJobVacancyDialog = dialogBuilder.create()
        openJobVacancyDialog.show()
    }

    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }

            layoutOpenJobVacancy.setOnClickListener {
                showOpenJobVacancyDialog()
            }

            btnGoToJobVacancyView.setOnClickListener {
                val action = ManageMerchantFragmentDirections.actionOpenJobVacancyScreen()
                Navigation.findNavController(binding.root).navigate(action)
            }

            fabAddMerchant.setOnClickListener {
                val action = ManageMerchantFragmentDirections.actionAddMerchant()
                Navigation.findNavController(binding.root).navigate(action)
            }
        }
    }
}