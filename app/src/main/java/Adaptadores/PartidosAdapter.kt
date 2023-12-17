package Adaptadores

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.footballmatchmanager.JugadorBase
import com.example.footballmatchmanager.Jugadores
import com.example.footballmatchmanager.Partido
import com.example.footballmatchmanager.Portero
import com.example.footballmatchmanager.databinding.RecyclerPartidosBinding
import com.example.footballmatchmanager.databinding.DialogagregarjugadoresBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PartidosAdapter(private val partidosList: MutableList<Partido>) :
    RecyclerView.Adapter<PartidosAdapter.PartidoViewHolder>() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartidoViewHolder {
        val binding =
            RecyclerPartidosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PartidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PartidoViewHolder, position: Int) {
        holder.bind(partidosList[position])
    }

    override fun getItemCount(): Int {
        return partidosList.size
    }

    inner class PartidoViewHolder(private val binding: RecyclerPartidosBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                Toast.makeText(
                    binding.root.context,
                    "Debe seleccionar un partido",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun bind(partido: Partido) {
            binding.textViewFecha.text = "Fecha: ${partido.fecha}"
            binding.textViewHoraInicio.text = "Hora de inicio: ${partido.horaInicio}"
            binding.textViewHoraFin.text = "Hora de fin: ${partido.horaFin}"

            binding.btnVerJugadores.setOnClickListener {
                cargarJugadoresDesdeFirebase(partido.fecha)
            }
        }

        private fun cargarJugadoresDesdeFirebase(fechaInicioPartido: String) {
            val currentUserEmail = firebaseAuth.currentUser?.email
            if (currentUserEmail != null) {
                val jugadoresCollection = db.collection("usuarios").document(currentUserEmail)
                    .collection("partidos").document(currentUserEmail)
                    .collection("jugadores")
                    .whereEqualTo(
                        "fecha",
                        fechaInicioPartido
                    )  // Ajusta el nombre del campo según tu estructura

                jugadoresCollection.get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val jugadores = mutableListOf<JugadorBase>()

                            for (document in task.result!!) {
                                try {
                                    // Obtener los datos del jugador del documento
                                    val nombre = document.getString("nombre")
                                    val valoracion = document.getDouble("valoracion")
                                    val posicion = document.getString("posicion")
                                    val goles = document.getLong("goles")
                                    val asistencias = document.getLong("asistencias")

                                    // Crear la instancia del jugador
                                    val jugador: JugadorBase? = when (posicion) {
                                        "Portero" -> Portero(
                                            valoracion!!,
                                            nombre!!,
                                            posicion!!,
                                            goles!!,
                                            asistencias!!
                                        )

                                        "Jugador Normal" -> Jugadores(
                                            valoracion!!,
                                            nombre!!,
                                            posicion!!,
                                            goles!!,
                                            asistencias!!
                                        )

                                        else -> null
                                    }

                                    // Agregar el jugador a la lista si se creó correctamente
                                    jugador?.let {
                                        jugadores.add(it)
                                    }
                                } catch (e: Exception) {
                                    Log.e(
                                        "Firebase",
                                        "Error al convertir documento a JugadorBase",
                                        e
                                    )
                                }
                            }

                            // Log para verificar el número de jugadores obtenidos
                            Log.d("Firebase", "Número de jugadores obtenidos: ${jugadores.size}")

                            // Mostrar los jugadores en un diálogo
                            cargarJugadoresEnDialog(jugadores)
                        } else {
                            // Log para manejar el error al obtener los jugadores desde Firebase
                            Log.e(
                                "Firebase",
                                "Error al obtener los jugadores desde Firebase",
                                task.exception
                            )

                            // Aquí puedes manejar el error de alguna manera, como mostrar un mensaje al usuario
                        }
                    }
            }
        }



        private fun cargarJugadoresEnDialog(jugadores: MutableList<JugadorBase>) {
            // Obtén el LayoutInflater del contexto del adaptador
            val inflater = LayoutInflater.from(binding.root.context)
            val dialogBinding = DialogagregarjugadoresBinding.inflate(inflater)

            // Configura el RecyclerView dentro del diálogo
            val recyclerViewJugadores = dialogBinding.recyclerViewJugadoresDialog
            val btnAgregarJugadores = dialogBinding.btnAgregarJugadores

            val adapter = JugadoresAdapter3(jugadores)
            recyclerViewJugadores.adapter = adapter

            btnAgregarJugadores.setOnClickListener {
                // Aquí puedes implementar la lógica para agregar jugadores
                // Puedes mostrar otro diálogo, hacer alguna acción, etc.

                // Cierra el diálogo después de realizar las acciones necesarias
                (it.context as? AlertDialog)?.dismiss()
            }

            // Crea el AlertDialog y configura la vista
            val builder = AlertDialog.Builder(binding.root.context)
                .setView(dialogBinding.root)
                .setTitle("Detalles del Jugador")

            builder.setPositiveButton("Eliminar") { _, _ ->
                // Realiza las acciones necesarias antes de cerrar el diálogo
                // (En este punto, los jugadores seleccionados han sido eliminados temporalmente del RecyclerView)

                // Cierra el diálogo después de realizar las acciones necesarias
                builder.create().dismiss()
            }

            // Configurar el botón "Cancelar"
            builder.setNegativeButton("Cancelar") { _, _ ->
                // Puedes realizar acciones adicionales o dejarlo vacío según sea necesario
            }

            // Mostrar el diálogo correctamente
            builder.show()
        }
    }
}



