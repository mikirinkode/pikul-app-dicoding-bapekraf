package com.mikirinkode.pikul.feature.transaction

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mikirinkode.pikul.feature.transaction.history.HistoryFragment
import com.mikirinkode.pikul.feature.transaction.order.OrderFragment

class TransactionPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position){
            0 -> fragment = OrderFragment()
            1 -> fragment = HistoryFragment()
        }
        return fragment as Fragment
    }

}
