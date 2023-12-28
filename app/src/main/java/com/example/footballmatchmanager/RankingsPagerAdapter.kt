package com.example.footballmatchmanager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class RankingsPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        // Número total de pestañas
        return 3  // Ajusta el número según sea necesario (Goles, Asistencias, MVP)
    }

    override fun createFragment(position: Int): Fragment {
        // Devuelve el fragmento correspondiente a la posición
        return when (position) {
            0 -> RankingGoles()
            1 -> RankingAsistencias()
            2 -> Rankingmvp()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}

