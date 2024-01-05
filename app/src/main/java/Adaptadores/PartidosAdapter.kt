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

class PartidosAdapter(
    private val partidosList: MutableList<Partido>
) : RecyclerView.Adapter<PartidosAdapter.PartidoViewHolder>() {


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
        private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
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
                leerJugadoresDePartido(partido.fecha, partido.horaInicio,partido.horaFin)
            }
            binding.btnBorrarPartido.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Llamar al método para borrar el partido
                    borrarPartido(position)
                }
            }
        }
        private fun leerJugadoresDePartido(fechaPartido: String,horaIni:String,horaFin:String) {
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
                                    // Obtener la lista de jugadores específicos de este partido
                                    val jugadoresData =
                                        document.get("jugadores") as List<HashMap<String, Any>>?
                                    val jugadores = jugadoresData?.map { jugadorData ->
                                        JugadorBase(
                                            jugadorData["valoracion"] as Long,
                                            jugadorData["nombre"].toString(),
                                            jugadorData["posicion"].toString(),
                                            jugadorData["goles"] as Long,
                                            jugadorData["asistencias"] as Long
                                        )
                                    }
                                    // Agregar los jugadores de este partido a la lista general
                                    jugadores?.let {
                                        jugadoresList.addAll(it)
                                    }
                                } catch (e: Exception) {
                                    Log.e("Firebase", "Error al convertir documento a Partido", e)
                                }
                            }

                            // Crear la instancia del adaptador con la fecha del partido
                            val jugadoresAdapter = JugadoresAdapter3(jugadoresList, fechaPartido,horaIni,horaFin )

                            // Mostrar los jugadores en el diálogo
                            cargarJugadoresEnDialog(jugadoresAdapter)
                        } else {
                            Log.e("Firebase", "Error al obtener los jugadores del partido", task.exception)
                        }
                    }
            } else {
                Log.e("Firebase", "El email del usuario es nulo")

            }
        }
        private fun cargarJugadoresEnDialog(jugadoresAdapter: JugadoresAdapter3?) {
            if (jugadoresAdapter != null) {
                // Configurar el diálogo para agregar jugadores
                val inflater = LayoutInflater.from(binding.root.context)
                val dialogBinding = DialogagregarjugadoresBinding.inflate(inflater)
                val builder = AlertDialog.Builder(binding.root.context)
                builder.setView(dialogBinding.root)
                // Configurar el RecyclerView en el diálogo con el JugadoresAdapter3
                dialogBinding.recyclerViewJugadoresDialog.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = jugadoresAdapter
                }

                // Configurar el botón "Cancelar"
                builder.setPositiveButton("Cancelar") { _, _ ->
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



