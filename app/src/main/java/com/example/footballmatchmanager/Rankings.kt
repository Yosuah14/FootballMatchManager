package com.example.footballmatchmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class Rankings : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rankings)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // Crea una instancia del adaptador para ViewPager
        val pagerAdapter = RankingsPagerAdapter(supportFragmentManager)

        // Establece el adaptador para ViewPager
        viewPager.adapter = pagerAdapter

        // Conecta TabLayout con ViewPager
        tabLayout.setupWithViewPager(viewPager)
    }
}

