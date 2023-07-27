package com.mikirinkode.pikul.feature.startbusiness

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.constants.PikulRole
import com.mikirinkode.pikul.databinding.FragmentSelectBusinessRoleBinding
import com.mikirinkode.pikul.feature.chat.room.ChatRoomFragmentDirections

// TODO: CHECK AGAIN
class SelectBusinessRoleFragment : Fragment() {
    private var _binding: FragmentSelectBusinessRoleBinding? = null
    private val binding get() = _binding!!

    private var selectedRole: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSelectBusinessRoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateButtonView(){
        binding.apply {
            if (!selectedRole.isNullOrBlank()){
                btnNext.isEnabled = true
                when (selectedRole){
                    PikulRole.OWNER.toString() -> {
                        btnNext.text = "Lanjutkan sebagai Pemilik Bisnis"
                    }
                    PikulRole.MERCHANT.toString() -> {
                        btnNext.text = "Lanjutkan sebagai Pedagang"
                    }
                }
            } else {
                btnNext.isEnabled = false
            }
        }
    }

    private fun onClickAction() {
        binding.apply {
            btnBack.setOnClickListener {
                requireActivity().onBackPressed()
            }

            layoutBusinessOwner.setOnClickListener {
                onBusinessOwnerSelected.visibility = View.VISIBLE
                tvBusinessOwnerOnSelected.visibility = View.VISIBLE
                layoutBusinessOwnerDesc.visibility = View.VISIBLE
                onMerchantSelected.visibility = View.GONE
                tvMerchantOnSelected.visibility = View.GONE
                layoutMerchantDesc.visibility = View.GONE
                selectedRole = PikulRole.OWNER.toString()
                updateButtonView()
            }
            layoutMerchant.setOnClickListener {
                onBusinessOwnerSelected.visibility = View.GONE
                tvBusinessOwnerOnSelected.visibility = View.GONE
                layoutBusinessOwnerDesc.visibility = View.GONE
                onMerchantSelected.visibility = View.VISIBLE
                tvMerchantOnSelected.visibility = View.VISIBLE
                layoutMerchantDesc.visibility = View.VISIBLE
                selectedRole = PikulRole.MERCHANT.toString()
                updateButtonView()
            }

            btnNext.setOnClickListener {
                when (selectedRole){
                    PikulRole.OWNER.toString() -> {
                        val action = SelectBusinessRoleFragmentDirections.actionOwnerRegister()
                        Navigation.findNavController(binding.root).navigate(action)
                    }
                    PikulRole.MERCHANT.toString() -> {
                        val action = SelectBusinessRoleFragmentDirections.actionMerchantRegister()
                        Navigation.findNavController(binding.root).navigate(action)
                    }
                }
            }
        }
    }
}