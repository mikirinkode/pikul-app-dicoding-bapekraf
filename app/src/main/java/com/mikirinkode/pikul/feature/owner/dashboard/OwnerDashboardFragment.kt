package com.mikirinkode.pikul.feature.owner.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.FragmentOwnerDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OwnerDashboardFragment : Fragment() {
    private var _binding: FragmentOwnerDashboardBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var pref: LocalPreference

    private val user: UserAccount? by lazy {
        pref?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
    }

    private val viewModel: OwnerDashboardViewModel by viewModels()

    private var sellingMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOwnerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeBusinessData()
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView(){
        binding.apply {
//            if (user != null){
//                tvUserName.text = user?.name
//                tvAddress.text = user?.province
//            }
        }
    }

    private fun observeBusinessData(){
        binding.apply {
            viewModel.getBusinessData().observe(viewLifecycleOwner){result ->
                when (result){
                    is PikulResult.Loading -> {
                        layoutLoading.visibility = View.VISIBLE
                    }
                    is PikulResult.LoadingWithProgress -> {}
                    is PikulResult.Error -> {
                        layoutLoading.visibility = View.GONE
                        Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show()
                    }
                    is PikulResult.Success -> {
                        layoutLoading.visibility = View.GONE
                        val businessData = result.data
                        sellingMode = businessData.sellingMode == true
                        tvUserName.text = businessData.businessName
                        tvAddress.text = businessData.businessAddress

                        if (businessData.sellingMode == true){
                            layoutSellingModeOnActive.visibility = View.VISIBLE
                            tvSellingModeStatusActive.visibility = View.VISIBLE
                            tvSellingModeStatusDummyActive.visibility = View.VISIBLE
                            tvSellingModeStatusNotActive.visibility = View.GONE
                            tvSellingModeStatusDummyNotActive.visibility = View.GONE
                        } else {
                            layoutSellingModeOnActive.visibility = View.GONE
                            tvSellingModeStatusActive.visibility = View.GONE
                            tvSellingModeStatusDummyActive.visibility = View.GONE
                            tvSellingModeStatusNotActive.visibility = View.VISIBLE
                            tvSellingModeStatusDummyNotActive.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun showSellingModeDialog(){
        val title = if (sellingMode) "Non-Aktifkan Mode Berjualan?" else "Aktifkan Mode Berjualan?"
        val message = if (sellingMode) "- Kami akan berhenti menampilkan akun anda sebagai pedagang. \n- Anda tidak bisa menerima pesanan lagi." else "- Dengan mengaktifkan mode jualan, akun anda akan tampil sebagai Pemilik Bisnis dan Pedagang. \n- Anda dapat menerima pesanan produk dari pelanggan. \n- Anda juga tetap dapat mengelola pedagang lain untuk menjual produk anda."
        val positiveButtonText = if (sellingMode) "Non-Aktifkan" else "Aktifkan"
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(positiveButtonText) { _, _ ->
                viewModel.updateSellingModeStatus(!sellingMode).observe(viewLifecycleOwner){ result ->
                    when (result) {
                        is PikulResult.Loading -> {
                            binding.layoutLoading.visibility = View.VISIBLE
                        }
                        is PikulResult.LoadingWithProgress -> {}
                        is PikulResult.Error -> {}
                        is PikulResult.Success -> {
                            binding.layoutLoading.visibility = View.GONE
                            Toast.makeText(requireContext(), "Berhasil memperbarui data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                sellingMode = !sellingMode
            }
            .setNegativeButton(getString(R.string.cancel)){ dialogInterface, _ ->
                dialogInterface.cancel()
            }
        val sellingModeDialog = dialogBuilder.create()
        sellingModeDialog.show()
    }

    private fun onClickAction() {
        binding.apply {
            // header
            layoutUserProfile.setOnClickListener {
                val action = OwnerDashboardFragmentDirections.actionOpenProfile()
                Navigation.findNavController(binding.root).navigate(action) }
            btnOpenChat.setOnClickListener {
                val action = OwnerDashboardFragmentDirections.actionOpenChat()
                Navigation.findNavController(binding.root).navigate(action)
            }

            // selling menu
            layoutSellingMode.setOnClickListener {
                showSellingModeDialog()
            }
            cardManageStock.setOnClickListener {
                val action = OwnerDashboardFragmentDirections.actionManageStock()
                Navigation.findNavController(binding.root).navigate(action)
            }

            cardTotalOrder.setOnClickListener {  } // TODO

            cardSellingPlace.setOnClickListener {
                val action = OwnerDashboardFragmentDirections.actionOpenMerchantSellingPlace()
                Navigation.findNavController(binding.root).navigate(action)
            }

            // other menu
            cardProduct.setOnClickListener {
                val action = OwnerDashboardFragmentDirections.actionOpenProductList()
                Navigation.findNavController(binding.root).navigate(action)
            }
            cardMerchant.setOnClickListener {
                val action = OwnerDashboardFragmentDirections.actionManageMerchant()
                Navigation.findNavController(binding.root).navigate(action)
            } // TODO
            cardTransaction.setOnClickListener {  } // TODO
        }
    }
}