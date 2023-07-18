package com.mikirinkode.pikul.feature.transaction

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mikirinkode.pikul.feature.transaction.history.HistoryFragment
import com.mikirinkode.pikul.feature.transaction.order.OngoingOrderFragment

class TransactionPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position){
            0 -> fragment = OngoingOrderFragment()
            1 -> fragment = HistoryFragment()
        }
        return fragment as Fragment
    }

}
