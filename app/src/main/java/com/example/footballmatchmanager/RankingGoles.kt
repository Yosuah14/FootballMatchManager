package com.example.footballmatchmanager
import Adaptadores.JugadoresAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.footballmatchmanager.JugadorBase
import com.example.footballmatchmanager.Jugadores
import com.example.footballmatchmanager.Portero
import com.example.footballmatchmanager.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RankingGoles : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var jugadoresAdapter: JugadoresAdapter
    private val jugadoresList = mutableListOf<JugadorBase>()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ranking_goles, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewJugadores)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MenuOpciones", "onCreate llamado")
        // Configurar el adaptador y el RecyclerView
        jugadoresAdapter = JugadoresAdapter(jugadoresList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = jugadoresAdapter

        // Obtener y ordenar la lista de jugadores desde Firestore por la cantidad de goles
        cargarDatosDesdeFirestore()
    }
    override fun onDestroyView() {
        // Detener cualquier tarea asincrónica o animación aquí
        super.onDestroyView()
    }

    private fun cargarDatosDesdeFirestore() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        if (currentUserEmail != null) {
            val jugadoresCollection = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(currentUserEmail)
                .collection("jugadores")

            jugadoresCollection.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val jugadores = task.result?.toObjects(JugadorBase::class.java)
                    jugadores?.let {
                        // Ordenar la lista de jugadores por la cantidad de goles de mayor a menor
                        jugadoresList.addAll(it.sortedByDescending { jugador ->
                            when (jugador) {
                                is Jugadores -> jugador.goles
                                is Portero -> jugador.goles
                                else -> 0
                            }
                        })
                        // Notificar al adaptador sobre el cambio en los datos
                        jugadoresAdapter.notifyDataSetChanged()
                    }
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
