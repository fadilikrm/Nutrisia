package com.example.nutrisia

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingPagerAdapter(activity: FragmentActivity, private val items: List<OnboardingItem>) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        val item = items[position]
        return OnboardingFragment.newInstance(item.title, item.description, item.imageRes)
    }
}

data class OnboardingItem(val title: String, val description: String, val imageRes: Int)
