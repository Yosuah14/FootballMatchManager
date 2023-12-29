// Importaciones necesarias para el adaptador de jugadores
package Adaptadores

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.footballmatchmanager.JugadorBase
import com.example.footballmatchmanager.Jugadores
import com.example.footballmatchmanager.Portero
import com.example.footballmatchmanager.databinding.DialogActulaizaJugadoresBinding
import com.example.footballmatchmanager.databinding.RecyclerModificarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Definición del adaptador de jugadores
class JugadoresAdapter3(private val jugadoresList: MutableList<JugadorBase>, private val fecha: String) :
    RecyclerView.Adapter<JugadoresAdapter3.JugadorViewHolder>() {

    // Instancias de Firebase para la autenticación y la base de datos Firestore
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var nuevosGolesTemp: Long = 0L
    private var nuevasAsistenciasTemp: Long = 0L
    private var nuevosMvp: Double = 0.0

    private var selectedImageUri: Uri? = null
    private var mvp = 0.0
    private var golesAntiguos = 0L
    private var mvpAntiguos: Double = 0.0
    private var asistenciasAntiguas = 0L
    var nombre: String? = null
    var modificado = false

    // ViewHolder para cada elemento de la lista de jugadores
    // Función para crear un ViewHolder basado en el diseño de la fila del RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadorViewHolder {
        val binding =
            RecyclerModificarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JugadorViewHolder(binding)
    }

    // Función para vincular datos a un ViewHolder específico en una posición dada
    override fun onBindViewHolder(holder: JugadorViewHolder, position: Int) {
        holder.bind(jugadoresList[position])
    }

    // Función que devuelve la cantidad de elementos en la lista
    override fun getItemCount(): Int {
        return jugadoresList.size
    }

    inner class JugadorViewHolder(private val binding: RecyclerModificarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Configuración del clic largo en el botón de cada elemento
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

        // Función para vincular datos de un jugador al ViewHolder
        fun bind(jugador: JugadorBase) {
            binding.textViewNombre.text = "Nombre:" + jugador.nombre
            cargarDatosJugador(jugador.nombre,fecha) { jugadorCargado ->
                jugadorCargado?.let {
                    asistenciasAntiguas = jugadorCargado.asistencias!!
                    golesAntiguos = jugadorCargado.goles!!
                    mvpAntiguos = jugadorCargado.valoracion

                    binding.textViewNombre.text = "Nombre:" + jugador.nombre
                    binding.textViewDetalle.text = "Goles: ${jugadorCargado.goles}"
                    binding.asistencias.text = "Asistencias:+ ${jugadorCargado.asistencias}"
                    binding.posicion.text = "Posicion:+ ${jugadorCargado.posicion}"
                    binding.mvp.text = "Mvp:+${jugadorCargado.valoracion}"
                    notifyDataSetChanged()
                }
            }
        }
        // Función para mostrar un cuadro de diálogo con detalles y opciones de modificación del jugador
        private fun mostrarDetallesJugador(jugador: JugadorBase) {
            val inflater = LayoutInflater.from(binding.root.context)
            val dialogBinding = DialogActulaizaJugadoresBinding.inflate(inflater)
            val checkBoxMvp = dialogBinding.checkBoxMVP

            val textoCompleto = binding.textViewNombre.text.toString()

            val partes = textoCompleto.split(":")

            if (partes.size >= 2) {
                nombre = partes[1].trim()
            }

            cargarDatosJugador(nombre!!,fecha) { jugadorCargado ->
                asistenciasAntiguas = jugadorCargado?.asistencias ?: 0L
                golesAntiguos = jugadorCargado?.goles ?: 0L
                mvpAntiguos = jugadorCargado?.valoracion ?: 0.0
            }

            val dialog = AlertDialog.Builder(binding.root.context)
                .setView(dialogBinding.root)
                .setPositiveButton("Modificar") { _, _ ->
                    val golesText = dialogBinding.editTextGolesDialog.text.toString()
                    val asistenciasText = dialogBinding.editTextAsistenciasDialog.text.toString()

                    if (golesText.isNotEmpty() && golesText.toInt() >= 0 &&
                        asistenciasText.isNotEmpty() && asistenciasText.toInt() >= 0
                    ) {
                        if (checkBoxMvp.isChecked) {
                            nuevosMvp = 1.0
                        } else {
                            nuevosMvp = 0.0
                        }

                        val golesRestar = golesAntiguos
                        val asistenciasRestar = asistenciasAntiguas

                        nuevosGolesTemp = golesText.toLong() - golesRestar
                        nuevasAsistenciasTemp = asistenciasText.toLong() - asistenciasRestar

                        mostrarConfirmacionCambios(
                            jugador.nombre,
                            nuevosGolesTemp,
                            nuevasAsistenciasTemp
                        )
                    } else {
                        Toast.makeText(
                            binding.root.context,
                            "Ingresa números válidos para goles y asistencias",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .create()

            dialog.show()
        }

        // Función para mostrar un cuadro de diálogo de confirmación antes de aplicar los cambios
        private fun mostrarConfirmacionCambios(
            nombreJugador: String,
            nuevosGoles: Long,
            nuevasAsistencias: Long
        ) {
            val dialog = AlertDialog.Builder(binding.root.context)
                .setMessage("¿Confirmar cambios?")
                .setPositiveButton("Confirmar") { _, _ ->
                    actualizarJugadorEnFirestoreParaPartido(
                        nombreJugador,
                        nuevosGoles,
                        nuevasAsistencias
                    )
                    actualizarDatosEnFirestoreJugadores(
                        nombreJugador,
                        nuevosGoles,
                        nuevasAsistencias,
                        mvp
                    )
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    // Descartar cambios temporales
                }
                .create()

            dialog.show()
        }

        // Función para actualizar los datos de un jugador en Firestore para un partido
        private fun actualizarJugadorEnFirestoreParaPartido(
            nombreJugador: String,
            nuevosGoles: Long,
            nuevasAsistencias: Long
        ) {
            val currentUserEmail = firebaseAuth.currentUser?.email
            if (currentUserEmail != null) {
                val jugadoresCollection = db.collection("usuarios").document(currentUserEmail)
                    .collection("partidos")

                val jugadorData = hashMapOf(
                    "nombre" to nombreJugador,
                    "valoracion" to 0.0,  // Ajusta según tu modelo de datos
                    "posicion" to "",    // Ajusta según tu modelo de datos
                    "goles" to nuevosGoles,
                    "asistencias" to nuevasAsistencias
                    // Otros campos según tu modelo de datos
                )

                jugadoresCollection.whereArrayContains("jugadores", nombreJugador)
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

        // Función para cargar los datos de un jugador desde Firestore para un partido
        private fun cargarDatosJugador(
            fechaPartido: String,
            nombreJugador: String,
            callback: (JugadorBase?) -> Unit
        ) {
            val currentUserEmail = firebaseAuth.currentUser?.email

            if (currentUserEmail != null) {
                val jugadoresCollection =
                    db.collection("usuarios").document(currentUserEmail)
                        .collection("partidos").document(fechaPartido)
                        .collection("jugadores")

                Log.d("Firebase", "Fecha del partido: $nombreJugador, Nombre del jugador: $fechaPartido")

                jugadoresCollection.whereEqualTo("nombre", fechaPartido).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documents = task.result

                            try {
                                if (documents != null && !documents.isEmpty) {
                                    val document = documents.documents[0] // Tomamos el primer documento (suponemos nombres únicos)

                                    val nombre = document.getString("nombre")
                                    val valoracion = document.getDouble("valoracion") ?: 0.0
                                    val posicion = document.getString("posicion") ?: ""
                                    val goles = document.getLong("goles") ?: 0
                                    val asistencias = document.getLong("asistencias") ?: 0

                                    Log.d("Firebase", "Datos del jugador encontrados. Nombre: $nombre")

                                    val jugador: JugadorBase = when (posicion) {
                                        "Portero" -> Portero(valoracion, nombre ?: "", posicion, goles, asistencias, selectedImageUri.toString())
                                        "Jugador Normal" -> Jugadores(valoracion, nombre ?: "", posicion, goles, asistencias, selectedImageUri.toString())
                                        else -> throw IllegalArgumentException("Posición de jugador desconocida: $posicion")
                                    }

                                    callback.invoke(jugador)
                                } else {
                                    Log.d("Firebase", "El jugador no existe en la lista")
                                    callback.invoke(null)
                                }
                            } catch (e: Exception) {
                                Log.e("Firebase", "Error al procesar datos del jugador", e)
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



        // Función para actualizar los datos de un jugador en Firestore (totales acumulados)
        private fun actualizarDatosEnFirestoreJugadores(
            nombreJugador: String,
            nuevosGoles: Long,
            nuevasAsistencias: Long,
            nuevaValoracion: Double
        ) {
            val currentUserEmail = firebaseAuth.currentUser?.email

            if (currentUserEmail != null) {
                val jugadoresCollection = db.collection("usuarios").document(currentUserEmail)
                    .collection("jugadores")

                jugadoresCollection.whereEqualTo("nombre", nombreJugador)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val documentId = documents.documents[0].id
                            val jugadorActual =
                                documents.documents[0].toObject(JugadorBase::class.java)

                            val nuevosDatos = hashMapOf(
                                "goles" to nuevosGoles + jugadorActual?.goles!!,
                                "asistencias" to nuevasAsistencias + jugadorActual?.asistencias!!,
                                "valoracion" to nuevaValoracion + jugadorActual?.valoracion!!
                            )

                            jugadoresCollection.document(documentId)
                                .update(nuevosDatos as Map<String, Any>)
                                .addOnSuccessListener {
                                    Log.d(
                                        "Firestore",
                                        "Datos del jugador actualizados en Firestore"
                                    )
                                }
                                .addOnFailureListener {
                                    Log.e(
                                        "Firestore",
                                        "Error al actualizar los datos del jugador en Firestore",
                                        it
                                    )
                                }
                        } else {
                            Log.e("Firestore", "No se encontró el jugador en Firestore")
                        }
                    }
                    .addOnFailureListener {
                        Log.e("Firestore", "Error al buscar el jugador en Firestore", it)
                    }
            }
        }

    }


}


