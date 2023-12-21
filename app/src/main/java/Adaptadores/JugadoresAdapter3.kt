package Adaptadores

import android.app.Activity
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
    private var golesModificar = 0L
    private var assistenciasModi = 0L
    private var mvp = 0

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
            binding.textViewNombre.text = "Nombre:" + jugador.nombre
            cargarDatosJugador(jugador.nombre) { jugadorCargado ->
                jugadorCargado?.let {
                    // Jugador encontrado, actualizar la interfaz de usuario
                    if (jugador is Portero) {
                        binding.imageJugador.setImageResource(R.drawable.karius)
                    } else {
                        binding.imageJugador.setImageResource(R.drawable.pedroleon)
                    }

                    binding.textViewNombre.text = "Nombre:" + jugador.nombre
                    binding.textViewDetalle.text = "Goles: ${jugadorCargado.goles}"
                    binding.asistencias.text = "Asistencias:+ ${jugadorCargado.asistencias}"
                    binding.posicion.text = "Posicion:+ ${jugadorCargado.posicion}"
                    notifyDataSetChanged()

                }
            }
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
                            nombre = jugador.nombre,
                            goles = golesText.toLong(),
                            asistencias = asistenciasText.toLong(),
                            posicion = jugador.posicion
                        )

                        golesModificar = jugadorModificado.goles?.minus(jugador.goles!!)!!
                        assistenciasModi =
                            jugadorModificado.asistencias?.minus(jugador.asistencias!!)!!

                        // Verificar si el jugador ya existe
                        existeJugadorEnFirestore(jugadorModificado.nombre) { jugadorExiste ->
                            if (jugadorExiste) {
                                actualizarJugadorEnFirestoreParaPartido(jugadorModificado)
                            } else {
                                guardarJugadorEnFirestoreParaPartido(jugadorModificado)
                            }

                            // Actualizar el objeto en la lista
                            binding.textViewNombre.text = "Nombre:" + jugador.nombre
                            binding.textViewDetalle.text = "Goles: ${jugadorModificado.goles}"
                            binding.asistencias.text = "Asistencias:+ ${jugadorModificado.asistencias}"
                            binding.posicion.text = "Posicion:+ ${jugadorModificado.posicion}"
                            notifyDataSetChanged()

                        }



                        // Actualizar la vista directamente en el hilo principal

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

        private fun existeJugadorEnFirestore(nombreJugador: String, callback: (Boolean) -> Unit) {
            val currentUserEmail = firebaseAuth.currentUser?.email
            if (currentUserEmail != null) {
                val jugadoresCollection = db.collection("usuarios").document(currentUserEmail)
                    .collection("datosjugadorespartido")

                jugadoresCollection.whereEqualTo("nombre", nombreJugador).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documents = task.result?.documents
                            val jugadorExiste = documents != null && documents.isNotEmpty()
                            callback.invoke(jugadorExiste)
                        } else {
                            Log.e(
                                "Firebase",
                                "Error al comprobar existencia del jugador en Firestore",
                                task.exception
                            )
                            callback.invoke(false)
                        }
                    }
            } else {
                Log.e("Firebase", "Usuario no autenticado")
                callback.invoke(false)
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
                        Log.d(
                            "Firebase",
                            "Datos del jugador para el partido guardados en Firestore"
                        )
                    }
                    .addOnFailureListener {
                        Log.e(
                            "Firebase",
                            "Error al guardar los datos del jugador para el partido en Firestore"
                        )
                    }
            }
        }

        private fun actualizarJugadorEnFirestoreParaPartido(jugador: JugadorBase) {
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

                jugadoresCollection.whereEqualTo("nombre", jugador.nombre)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            Log.e("Firebase", "El documento del jugador no existe para actualizar")
                        } else {
                            val documentId = documents.documents[0].id
                            jugadoresCollection.document(documentId)
                                .set(jugadorData)
                                .addOnSuccessListener {
                                    Log.d(
                                        "Firebase",
                                        "Datos del jugador para el partido actualizados en Firestore"
                                    )
                                }
                                .addOnFailureListener {
                                    Log.e(
                                        "Firebase",
                                        "Error al actualizar los datos del jugador para el partido en Firestore"
                                    )
                                }
                        }
                    }
                    .addOnFailureListener {
                        Log.e("Firebase", "Error al obtener datos del jugador para actualizar", it)
                    }
            }
        }

        private fun cargarDatosJugador(nombreJugador: String, callback: (JugadorBase?) -> Unit) {
            val currentUserEmail = firebaseAuth.currentUser?.email
            if (currentUserEmail != null) {
                val jugadoresCollection = db.collection("usuarios").document(currentUserEmail)
                    .collection("datosjugadorespartido")

                jugadoresCollection.whereEqualTo("nombre", nombreJugador).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documents = task.result?.documents

                            if (documents != null && documents.isNotEmpty()) {
                                val document = documents[0]

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

                                    callback.invoke(jugador)
                                } catch (e: Exception) {
                                    Log.e("Firebase", "Error al cargar datos del jugador", e)
                                    callback.invoke(null)
                                }
                            } else {
                                Log.d("Firebase", "El documento del jugador no existe")
                                callback.invoke(null)
                            }
                        } else {
                            Log.e("Firebase", "Error al obtener datos del jugador", task.exception)
                            callback.invoke(null)
                        }
                    }
            } else {
                Log.e("Firebase", "Usuario no autenticado")
                callback.invoke(null)
            }
        }

    }
}







