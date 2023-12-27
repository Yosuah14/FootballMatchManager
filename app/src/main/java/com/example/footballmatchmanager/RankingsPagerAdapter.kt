package com.example.footballmatchmanager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class RankingsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> RankingGoles()  // Tu fragmento existente
            // Agrega más casos si tienes fragmentos adicionales
            else -> throw IllegalArgumentException("Posición no válida: $position")
        }
    }

    override fun getCount(): Int {
        return 1  // Ajusta el recuento según la cantidad de fragmentos que tengas
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Ranking Goles"  // Establece los títulos de las pestañas según sea necesario
            // Agrega más títulos si tienes fragmentos adicionales
            else -> null
        }
    }
}