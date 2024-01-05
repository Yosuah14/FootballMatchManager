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


class RankingAsistencias : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var jugadoresAdapter: JugadoresAdapter
    private val jugadoresList = mutableListOf<JugadorBase>()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "RankingAsistenciasFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ranking_asistencias, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewAsistencias)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated llamado")

        // Configurar el adaptador y el RecyclerView
        jugadoresAdapter = JugadoresAdapter(jugadoresList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = jugadoresAdapter

        // Obtener y ordenar la lista de jugadores desde Firestore por la cantidad de asistencias
        leerJugadoresDelUsuarioPorAsistencias()
    }

    private fun leerJugadoresDelUsuarioPorAsistencias() {
        // Similar a la implementación de leerJugadoresDelUsuario en RankingGoles
        // Pero ordenar por asistencias en lugar de goles
        val currentUserEmail = firebaseAuth.currentUser?.email
        Log.d("Firebase", "Email del usuario: $currentUserEmail")

        if (currentUserEmail != null) {
            val jugadoresCollection = db.collection("usuarios").document(currentUserEmail)
                .collection("jugadores")

            jugadoresCollection.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase", "Obtención de jugadores exitosa")
                    for (document in task.result!!) {
                        try {
                            val nombre = document.getString("nombre")
                            val valoracion = document.getLong("valoracion")
                            val posicion = document.getString("posicion")
                            val goles = document.getLong("goles")
                            val asistencias = document.getLong("asistencias")
                            val imageUrl = document.getString("imageUrl")

                            val jugador: JugadorBase? = when (posicion) {
                                "Portero" -> Portero(
                                    valoracion!!,
                                    nombre!!,
                                    posicion!!,
                                    goles!!,
                                    asistencias!!,
                                    imageUrl ?: ""
                                )
                                "Jugador Normal" -> Jugadores(
                                    valoracion!!,
                                    nombre!!,
                                    posicion!!,
                                    goles!!,
                                    asistencias!!,
                                    imageUrl ?: ""
                                )
                                else -> null
                            }

                            jugador?.let {
                                jugadoresList.add(it)
                            }
                        } catch (e: Exception) {
                            Log.e("Firebase", "Error al convertir documento a JugadorBase", e)
                        }
                    }

                    // Ordenar la lista de jugadores por la cantidad de asistencias de mayor a menor
                    jugadoresList.sortByDescending { it.asistencias }

                    jugadoresAdapter.notifyDataSetChanged()
                } else {
                    Log.e("Firebase", "Error al obtener los jugadores", task.exception)
                }
            }
        } else {
            Log.e("Firebase", "El email del usuario es nulo")
            // Puedes mostrar un mensaje o realizar alguna acción adecuada si el email es nulo
        }
    }
}
