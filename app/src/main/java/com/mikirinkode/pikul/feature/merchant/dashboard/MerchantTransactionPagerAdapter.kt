package com.mikirinkode.pikul.feature.merchant.dashboard

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mikirinkode.pikul.feature.customer.transaction.history.OrderHistoryFragment
import com.mikirinkode.pikul.feature.customer.transaction.order.OngoingOrderFragment

class MerchantTransactionPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {


    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) { // TODO: change
            0 -> fragment = MerchantOnGoingTransactionFragment()
            1 -> fragment = MerchantHistoryTransactionFragment()
        }

        return fragment as Fragment
    }

    override fun getItemCount(): Int {
        return 2
    }
}