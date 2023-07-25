package com.mikirinkode.pikul.feature.owner.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.local.LocalPreferenceConstants
import com.mikirinkode.pikul.data.model.UserAccount
import com.mikirinkode.pikul.databinding.FragmentJobVacancyBinding
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
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView(){
        binding.apply {
            if (user != null){
                tvUserName.text = user?.name
                tvAddress.text = user?.province
            }
        }
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
            layoutSellingMode.setOnClickListener {}
            cardSellingPlace.setOnClickListener {
                val action = OwnerDashboardFragmentDirections.actionOpenMerchantSellingPlace()
                Navigation.findNavController(binding.root).navigate(action)
            }
            cardTotalOrder.setOnClickListener {  }

            // other menu
            cardProduct.setOnClickListener {
                val action = OwnerDashboardFragmentDirections.actionCreateProduct()
                Navigation.findNavController(binding.root).navigate(action) }
            cardMerchant.setOnClickListener {  }
            cardTransaction.setOnClickListener {  }
        }
    }
}