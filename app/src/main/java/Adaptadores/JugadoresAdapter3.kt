package Adaptadores

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.footballmatchmanager.JugadorBase
import com.example.footballmatchmanager.Jugadores
import com.example.footballmatchmanager.Portero
import com.example.footballmatchmanager.R
import com.example.footballmatchmanager.databinding.DialogActulaizaJugadoresBinding
import com.example.footballmatchmanager.databinding.RecyclerModificarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class JugadoresAdapter3(private val jugadoresList: MutableList<JugadorBase>) :

    RecyclerView.Adapter<JugadoresAdapter3.JugadorViewHolder>() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadorViewHolder {
        val binding =
            RecyclerModificarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JugadorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JugadorViewHolder, position: Int) {
        holder.bind(jugadoresList[position])
    }

    override fun getItemCount(): Int {
        return jugadoresList.size
    }

    inner class JugadorViewHolder(private val binding: RecyclerModificarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Configurar el clic largo en el botón
            binding.btnmodi.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    mostrarDetallesJugador(jugadoresList[adapterPosition])
                } else {
                    Toast.makeText(
                        binding.root.context,
                        "Debes mantener pulsado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true // Indica que se ha manejado el evento
            }
        }

        fun bind(jugador: JugadorBase) {
            if (jugador is Portero) {
                binding.imageJugador.setImageResource(R.drawable.karius)
            } else {
                binding.imageJugador.setImageResource(R.drawable.pedroleon)
            }

            binding.textViewNombre.text = "Nombre:" + jugador.nombre
            binding.textViewDetalle.text = "Goles: ${jugador.goles}"
            binding.asistencias.text = "Asistencias:+ ${jugador.asistencias}"
            binding.posicion.text = "Posicion:+ ${jugador.posicion}"
        }

        private fun mostrarDetallesJugador(jugador: JugadorBase) {
            val inflater = LayoutInflater.from(binding.root.context)
            val dialogBinding = DialogActulaizaJugadoresBinding.inflate(inflater)

            val dialog = AlertDialog.Builder(binding.root.context)
                .setView(dialogBinding.root)
                .setPositiveButton("Modificar") { _, _ ->
                    // Validar y realizar acciones de modificación
                    val golesText = dialogBinding.editTextGolesDialog.text.toString()
                    val asistenciasText = dialogBinding.editTextAsistenciasDialog.text.toString()

                    if (golesText.isNotEmpty() && golesText.toInt() >= 0 &&
                        asistenciasText.isNotEmpty() && asistenciasText.toInt() >= 0
                    ) {
                        // Crear un nuevo objeto JugadorBase con los datos modificados
                        val jugadorModificado = JugadorBase(
                            nombre = binding.textViewNombre.text.toString(),
                            goles = golesText.toLong(),
                            asistencias = asistenciasText.toLong(),
                            posicion = jugador.posicion
                        )
                        guardarJugadorEnFirestoreParaPartido(jugadorModificado)
                        // Actualizar el objeto en la lista
                        jugadoresList[adapterPosition] = jugadorModificado

                        // Actualizar la vista del RecyclerView
                        notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            binding.root.context,
                            "Ingresa números válidos para goles y asistencias",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .create()

            dialog.show()
        }
    }

    private fun guardarJugadorEnFirestoreParaPartido(jugador: JugadorBase) {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val jugadoresCollection = db.collection("usuarios").document(currentUserEmail)
                .collection("datosjugadorespartido")

            val jugadorData = hashMapOf(
                "nombre" to jugador.nombre,
                "valoracion" to jugador.valoracion,
                "posicion" to jugador.posicion,
                "goles" to jugador.goles,
                "asistencias" to jugador.asistencias
                // Otros campos según tu modelo de datos
            )

            jugadoresCollection.add(jugadorData)
                .addOnSuccessListener {
                    Log.e(
                        "Firebase",
                        "Error al guardar los datos en firestore",

                        )
                }
                .addOnFailureListener {
                    Log.e(
                        "Firebase",
                        "Error al guardar los datos en firestore",

                    )
                }
        }
    }

    private fun leerJugadoresDelUsuarioParaPartido(callback: (List<JugadorBase>) -> Unit) {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val jugadoresCollection = db.collection("usuarios").document(currentUserEmail)
                .collection("datosjugadorespartido")

            jugadoresCollection.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val jugadoresList = mutableListOf<JugadorBase>()

                        for (document in task.result!!) {
                            try {
                                val nombre = document.getString("nombre")
                                val valoracion = document.getDouble("valoracion")
                                val posicion = document.getString("posicion")
                                val goles = document.getLong("goles")
                                val asistencias = document.getLong("asistencias")

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

                                jugador?.let {
                                    jugadoresList.add(it)
                                }
                            } catch (e: Exception) {
                                Log.e("Firebase", "Error al convertir documento a JugadorBase", e)
                            }
                        }

                        callback.invoke(jugadoresList)
                    } else {
                        Log.e(
                            "Firebase",
                            "Error al obtener los jugadores para el partido",
                            task.exception
                        )

                    }
                }
        }
    }
}





