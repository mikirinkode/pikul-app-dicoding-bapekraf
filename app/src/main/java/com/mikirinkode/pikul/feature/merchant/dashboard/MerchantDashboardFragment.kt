package com.mikirinkode.pikul.feature.merchant.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.google.android.material.tabs.TabLayoutMediator
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.data.local.LocalPreference
import com.mikirinkode.pikul.data.model.MerchantAgreement
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.databinding.FragmentMerchantDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// TODO: SHOW TRANSACTIONS
@AndroidEntryPoint
class MerchantDashboardFragment : Fragment() {
    private var _binding: FragmentMerchantDashboardBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var pref: LocalPreference

//    private val user: UserAccount? by lazy {
//        pref?.getObject(LocalPreferenceConstants.USER, UserAccount::class.java)
//    }

    private val viewModel: MerchantDashboardViewModel by viewModels()

    private var agreement: MerchantAgreement? = null

    companion object {
        @StringRes
        private val TAB_TITLES = intArrayOf(
            R.string.txt_tab_title_ongoing_order,
            R.string.txt_tab_title_completed_order
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMerchantDashboardBinding.inflate(inflater, container, false)
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
//            if (user != null) {
//                tvUserName.text = user?.name
//                tvAddress.text = user?.province
//            }
        }
    }

    private fun observeData() {
        binding.apply {
            viewModel.getMerchantData().observe(viewLifecycleOwner) { result ->
                when (result) {
                    is PikulResult.Loading -> {}
                    is PikulResult.LoadingWithProgress -> {} // TODO
                    is PikulResult.Error -> {}
                    is PikulResult.Success -> {
                        val user = result.data
                        tvUserName.text = user.name
                        tvAddress.text = user.province
                    }
                }
            }

            viewModel.getAgreement().observe(viewLifecycleOwner) { result ->
                when (result) {
                    is PikulResult.Loading -> {}
                    is PikulResult.LoadingWithProgress -> {} // TODO
                    is PikulResult.Error -> {}
                    is PikulResult.Success -> {
                        agreement = result.data
                        if (agreement == null) {
                            cardNotHavePartner.visibility = View.VISIBLE
                        } else {
                            if (agreement?.businessPartnerId != null) {
                                cardNotHavePartner.visibility = View.GONE
                                setupTabs()
                            } else {
                                cardNotHavePartner.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupTabs() {
        binding.apply {
            val merchantTransactionPagerAdapter = MerchantTransactionPagerAdapter(this@MerchantDashboardFragment)
            viewPager.adapter = merchantTransactionPagerAdapter
            TabLayoutMediator(tabs, viewPager) { tab, position ->
                tab.text = resources.getString(TAB_TITLES[position])
            }.attach()
        }
    }

    private fun onClickAction() {
        binding.apply {
            layoutUserProfile.setOnClickListener {
                val action =
                    MerchantDashboardFragmentDirections.actionOpenMerchantProfile(agreement)
                Navigation.findNavController(binding.root).navigate(action)
            }

            btnOpenChat.setOnClickListener {
                val action = MerchantDashboardFragmentDirections.actionOpenMerchantChats()
                Navigation.findNavController(binding.root).navigate(action)
            }
        }
    }
}