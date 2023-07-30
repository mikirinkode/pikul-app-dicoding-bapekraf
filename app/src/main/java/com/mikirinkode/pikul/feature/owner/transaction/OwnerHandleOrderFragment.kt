package com.mikirinkode.pikul.feature.owner.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.tabs.TabLayoutMediator
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.databinding.FragmentOwnerHandleOrderBinding
import com.mikirinkode.pikul.feature.merchant.dashboard.MerchantTransactionPagerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OwnerHandleOrderFragment : Fragment() {
    private var _binding: FragmentOwnerHandleOrderBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentOwnerHandleOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()

        binding.topAppBar.setNavigationOnClickListener {
            Navigation.findNavController(binding.root).navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupTabs() {
        binding.apply {
            val merchantTransactionPagerAdapter = MerchantTransactionPagerAdapter(this@OwnerHandleOrderFragment)
            viewPager.adapter = merchantTransactionPagerAdapter
            TabLayoutMediator(tabs, viewPager) { tab, position ->
                tab.text = resources.getString(TAB_TITLES[position])
            }.attach()
        }
    }
}