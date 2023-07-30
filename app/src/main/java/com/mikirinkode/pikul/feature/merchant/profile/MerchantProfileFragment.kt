package com.mikirinkode.pikul.feature.merchant.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.local.MAIN_VIEW
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.DialogAddNewStopPointBinding
import com.mikirinkode.pikul.databinding.FragmentMerchantProfileBinding
import com.mikirinkode.pikul.feature.customer.main.MainActivity
import com.mikirinkode.pikul.utils.MoneyHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * TODO: UPDATE VIEW, LOOKS SAME WITH THE CUSTOMER
 */

/**
 * TODO:
 * ADD LOADING INDICATOR
 * ON ERROR INDICATOR
 */
@AndroidEntryPoint
class MerchantProfileFragment : Fragment() {
    private var _binding: FragmentMerchantProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MerchantProfileViewModel by viewModels()

    @Inject
    lateinit var pref: LocalPreference

//    private val user: UserAccount? by lazy {
//        pref?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
//    }

    private val args: MerchantProfileFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMerchantProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        onClickAction()
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {
        binding.apply {
        }
    }

    private fun observeData(){
        binding.apply {
            viewModel.getMerchantData().observe(viewLifecycleOwner) {result ->
                when (result) {
                    is PikulResult.Loading -> {}
                    is PikulResult.LoadingWithProgress -> {} // TODO
                    is PikulResult.Error -> {}
                    is PikulResult.Success -> {
                        val user = result.data
                        tvUserName.text = user?.name
                        tvDummyRole.text = "Pedagang"
                        tvRole.text = "Pedagang"
                        tvEmail.text = user?.email

                        if (user?.avatarUrl.isNullOrBlank()) {
                            Glide.with(requireContext())
                                .load(R.drawable.ic_default_user_avatar)
                                .into(ivUserAvatar)
                        } else {
                            Glide.with(requireContext())
                                .load(user?.avatarUrl)
                                .placeholder(R.drawable.progress_animation)
                                .into(ivUserAvatar)
                        }
                    }
                }
            }

            if (isHaveAgreement()){
                viewModel.getTransactionList().observe(viewLifecycleOwner) {result ->
                    when (result) {
                        is PikulResult.Loading -> {}
                        is PikulResult.LoadingWithProgress -> {} // TODO
                        is PikulResult.Error -> {}
                        is PikulResult.Success -> {
                            val list = result.data
                            tvTotalTransaction.text = list.size.toString()
                            var totalRevenue = 0f
                            for (transaction in list){
                                totalRevenue += transaction.totalBilling ?: 0f
                            }
                            tvTotalRevenue.text = MoneyHelper.getFormattedPrice(totalRevenue)
                        }
                    }
                }

                args.agreement?.businessPartnerId?.let { businessId ->
                    viewModel.getBusinessData(businessId).observe(viewLifecycleOwner) {result ->
                        when (result) {
                            is PikulResult.Loading -> {}
                            is PikulResult.LoadingWithProgress -> {} // TODO
                            is PikulResult.Error -> {}
                            is PikulResult.Success -> {
                                layoutBusinessOwner.visibility = View.VISIBLE
//                                layoutBusinessInformation.visibility = View.VISIBLE // TODO
                                val businessData = result.data
                                tvBusinessName.text = businessData.businessName
                                tvBusinessSince.text = "Mitra sejak ${args.agreement?.partnerSince}"

                                if (businessData.businessPhoto != null && businessData.businessPhoto != "") {
                                    Glide.with(requireContext())
                                        .load(businessData.businessPhoto)
                                        .placeholder(R.drawable.progress_animation)
                                        .into(ivUserAvatar)
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    private fun isHaveAgreement(): Boolean {
        return (args.agreement != null && args.agreement?.businessPartnerId != null)
    }

    private fun onClickAction() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }

            btnOpenCustomerView.setOnClickListener {
                pref.saveString(
                    LocalPreferenceConstants.SELECTED_MAIN_VIEW,
                    MAIN_VIEW.CUSTOMER_VIEW.toString()
                )
                startActivity(Intent(requireContext(), MainActivity::class.java))
                finishAffinity(requireActivity())
            }
            btnManageStock.setOnClickListener {
                if (isHaveAgreement()){
                    val action = MerchantProfileFragmentDirections.actionManageStock(
                        args.agreement?.businessPartnerId ?: "" // TODO: CHECK AGAIN LATER
                    )
                    Navigation.findNavController(binding.root).navigate(action)
                } else {
                    Toast.makeText(requireContext(), "Anda belum memiliki mitra dagang", Toast.LENGTH_SHORT).show()
                }
            }
            btnAddSellingPlace.setOnClickListener {
                if (isHaveAgreement()){
                    val action = MerchantProfileFragmentDirections.actionOpenStopPoint(
                        args.agreement?.businessPartnerId ?: "" // TODO: CHECK AGAIN LATER
                    )
                    Navigation.findNavController(binding.root).navigate(action)
                } else {
                    Toast.makeText(requireContext(), "Anda belum memiliki mitra dagang", Toast.LENGTH_SHORT).show()
                }
            }
            layoutBusinessInformation.setOnClickListener {
                if (isHaveAgreement()){
                } else {
                    Toast.makeText(requireContext(), "Anda belum memiliki mitra dagang", Toast.LENGTH_SHORT).show()
                }
            }
            btnTransaction.setOnClickListener {
                if (isHaveAgreement()){

                } else {
                    Toast.makeText(requireContext(), "Anda belum memiliki mitra dagang", Toast.LENGTH_SHORT).show()
                }
            }

            btnUserReview.setOnClickListener {} // currently set to invinsible

            btnLogout.setOnClickListener {
                viewModel.logout().observe(viewLifecycleOwner){ result ->
                    when (result) {
                        is PikulResult.Loading -> {} // TODO
                        is PikulResult.LoadingWithProgress -> {}
                        is PikulResult.Error -> {}
                        is PikulResult.Success -> {
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            finishAffinity(requireActivity())
                        }
                    }
                }
            }
        }
    }
}