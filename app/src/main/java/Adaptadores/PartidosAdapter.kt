package Adaptadores

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.footballmatchmanager.JugadorBase
import com.example.footballmatchmanager.Jugadores
import com.example.footballmatchmanager.Partido
import com.example.footballmatchmanager.Portero
import com.example.footballmatchmanager.databinding.RecyclerPartidosBinding
import com.example.footballmatchmanager.databinding.DialogagregarjugadoresBinding
import com.google.android.play.core.integrity.e
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
                leerPartidosDelUsuario(partido.fecha)

            }
            binding.btnBorrarPartido.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Llamar al método para borrar el partido
                    borrarPartido(position)
                }
            }

        }

        private fun leerPartidosDelUsuario(fechaPartido: String) {
            val currentUserEmail = firebaseAuth.currentUser?.email

            if (currentUserEmail != null) {
                val partidosCollection =
                    db.collection("usuarios").document(currentUserEmail).collection("partidos")

                partidosCollection.whereEqualTo("fecha", fechaPartido).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val jugadoresList = mutableListOf<JugadorBase>()

                            for (document in task.result!!) {
                                try {
                                    // Obtener los datos del partido del documento


                                    // Obtener la lista de jugadores
                                    val jugadoresData =
                                        document.get("jugadores") as List<HashMap<String, Any>>?
                                    val jugadores = jugadoresData?.map { jugadorData ->
                                        JugadorBase(
                                            jugadorData["valoracion"] as Double,
                                            jugadorData["nombre"].toString(),
                                            jugadorData["posicion"].toString(),
                                            jugadorData["goles"] as Long,
                                            jugadorData["asistencias"] as Long
                                            // Otros campos según tu modelo de datos para JugadorBase
                                        )
                                    }

                                    // Agregar los jugadores de este partido a la lista general
                                    jugadores?.let {
                                        jugadoresList.addAll(it)
                                    }


                                    // Crear el objeto Partido y agregarlo a la lista de partidos


                                } catch (e: Exception) {
                                    Log.e("Firebase", "Error al convertir documento a Partido", e)
                                }
                            }

                            // Notificar al adaptador sobre el cambio en los datos


                            // Mostrar los jugadores de este partido
                            Log.d("Firebase", "Jugadores del partido $fechaPartido: $jugadoresList")
                            cargarJugadoresEnDialog(jugadoresList)
                        } else {
                            Log.e("Firebase", "Error al obtener los partidos", task.exception)
                        }
                    }
            } else {
                Log.e("Firebase", "El email del usuario es nulo")
                // Puedes mostrar un mensaje o realizar alguna acción adecuada si el email es nulo
            }
        }

        private fun cargarJugadoresEnDialog(jugadores: MutableList<JugadorBase>) {


            // Configurar el botón "Ver Jugadores"
            binding.btnVerJugadores.setOnClickListener {
                // Configurar el diálogo para agregar jugadores
                val inflater = LayoutInflater.from(binding.root.context)
                val dialogBinding = DialogagregarjugadoresBinding.inflate(inflater)
                val builder = AlertDialog.Builder(binding.root.context)
                builder.setView(dialogBinding.root)

                // Configurar el RecyclerView en el diálogo con el JugadoresAdapter3
                val jugadoresAdapter = JugadoresAdapter3(jugadores)
                // Cargar los datos de Firebase en el RecyclerView del diálogo
                dialogBinding.recyclerViewJugadoresDialog.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = jugadoresAdapter
                }

                // Configurar el botón "Cancelar"
                builder.setPositiveButton("Cancelar") { _, _ ->
                    // Realiza las acciones necesarias antes de cerrar el diálogo
                    // (En este punto, los jugadores seleccionados han sido eliminados temporalmente del RecyclerView)

                    // Cierra el diálogo después de realizar las acciones necesarias
                    builder.create().dismiss()
                }

                // Mostrar el diálogo correctamente
                builder.show()
            }
        }
        fun borrarPartido(position: Int) {
            val partidoBorrado = partidosList[position]
            val currentUserEmail = firebaseAuth.currentUser?.email

            if (currentUserEmail != null) {
                val partidosCollection =
                    db.collection("usuarios").document(currentUserEmail).collection("partidos")

                partidosCollection.whereEqualTo("fecha", partidoBorrado.fecha).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result!!) {
                                // Borrar el documento correspondiente al partido en Firebase
                                document.reference.delete()
                            }

                            // Borrar el partido de la lista local
                            partidosList.removeAt(position)

                            // Notificar al adaptador sobre el cambio en los datos
                            notifyDataSetChanged()
                        } else {
                            Log.e("Firebase", "Error al obtener los partidos", task.exception)
                        }
                    }
            } else {
                Log.e("Firebase", "El email del usuario es nulo")
                // Puedes mostrar un mensaje o realizar alguna acción adecuada si el email es nulo
            }
        }
    }

}


