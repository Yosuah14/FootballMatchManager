package com.example.footballmatchmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.footballmatchmanager.databinding.ActivityRankingsBinding
import com.google.android.material.tabs.TabLayoutMediator

class Rankings : AppCompatActivity() {

    private lateinit var binding: ActivityRankingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val pagerAdapter = RankingsPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Usar TabLayoutMediator para sincronizar el TabLayout y ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()
    }

    private fun getTabTitle(position: Int): String {
        return when (position) {
            0 -> "Goles"
            1 -> "Asistencias"
            2 -> "MVP"
            else -> "Pestaña $position"
        }
    }
}

