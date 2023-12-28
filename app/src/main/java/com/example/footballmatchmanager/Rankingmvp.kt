package com.example.footballmatchmanager

import Adaptadores.JugadoresAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Rankingmvp : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var jugadoresAdapter: JugadoresAdapter
    private val jugadoresList = mutableListOf<JugadorBase>()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "RankingMVPFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rankingmvp, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewmvp)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated llamado")

        // Configurar el adaptador y el RecyclerView
        jugadoresAdapter = JugadoresAdapter(jugadoresList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = jugadoresAdapter

        // Obtener y ordenar la lista de jugadores desde Firestore por la valoración (MVP)
        leerJugadoresDelUsuarioPorMVP()
    }

    private fun leerJugadoresDelUsuarioPorMVP() {
        // Similar a la implementación de leerJugadoresDelUsuario en RankingGoles
        // Pero ordenar por valoración (MVP) en lugar de goles
    }
}
