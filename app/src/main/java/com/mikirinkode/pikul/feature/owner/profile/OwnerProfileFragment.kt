package com.mikirinkode.pikul.feature.owner.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.local.MAIN_VIEW
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.databinding.FragmentOwnerProfileBinding
import com.mikirinkode.pikul.feature.customer.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OwnerProfileFragment : Fragment() {
    private var _binding: FragmentOwnerProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var pref: LocalPreference

    private val viewModel: OwnerProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOwnerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeBusinessData()
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                        val sellingMode = businessData.sellingMode == true
                        tvUserName.text = businessData.businessName
                        tvBusinessEmail.text = businessData.businessEmail
                        tvBusinessPhone.text = businessData.businessPhoneNumber
                        tvRole.text = "Pemilik Bisnis"
                        tvDummyRole.text = "Pemilik Bisnis"

                        if (businessData.businessPhoto != null && businessData.businessPhoto != ""){
                            Glide.with(requireContext())
                                .load(businessData.businessPhoto)
                                .into(ivUserAvatar)
                        } else {

                        }

                        if (businessData.sellingMode == true){
                        } else {
                        }
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

            btnOpenCustomerView.setOnClickListener {
                pref.saveString(
                    LocalPreferenceConstants.SELECTED_MAIN_VIEW,
                    MAIN_VIEW.CUSTOMER_VIEW.toString()
                )
                startActivity(Intent(requireContext(), MainActivity::class.java))
                ActivityCompat.finishAffinity(requireActivity())
            }
        }
    }
}