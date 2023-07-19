package com.mikirinkode.pikul.feature.customer.transaction

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mikirinkode.pikul.feature.customer.transaction.history.OrderHistoryFragment
import com.mikirinkode.pikul.feature.customer.transaction.order.OngoingOrderFragment

class TransactionPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position){
            0 -> fragment = OngoingOrderFragment()
            1 -> fragment = OrderHistoryFragment()
        }
        return fragment as Fragment
    }

}
