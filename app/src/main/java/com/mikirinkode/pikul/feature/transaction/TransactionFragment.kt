package com.mikirinkode.pikul.feature.transaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.google.android.material.tabs.TabLayoutMediator
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.databinding.FragmentTransactionBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionBinding? = null
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
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            val sectionsPagerAdapter = TransactionPagerAdapter(this@TransactionFragment)
            viewPager.adapter = sectionsPagerAdapter
            TabLayoutMediator(tabs, viewPager) { tab, position ->
                tab.text = resources.getString(TAB_TITLES[position])
            }.attach()
        }
        onClickAction()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun onClickAction() {

    }
}