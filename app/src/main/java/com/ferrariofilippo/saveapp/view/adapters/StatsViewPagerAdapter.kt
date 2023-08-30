package com.ferrariofilippo.saveapp.view.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ferrariofilippo.saveapp.view.StatsByMonthFragment
import com.ferrariofilippo.saveapp.view.StatsByTagFragment
import com.ferrariofilippo.saveapp.view.StatsByYearFragment

class StatsViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> StatsByTagFragment()
            1 -> StatsByMonthFragment()
            else -> StatsByYearFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}
