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
class JugadoresAdapter3(private val jugadoresList: MutableList<JugadorBase>, private val fecha: String,private val horaIni:String,private val  horaFin:String) :
    RecyclerView.Adapter<JugadoresAdapter3.JugadorViewHolder>() {

    // Instancias de Firebase para la autenticación y la base de datos Firestore
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var nuevosGolesTemp: Long = 0L
    private var nuevasAsistenciasTemp: Long = 0L
    private var nuevosMvp: Long = 0L
    private var nuevosMvpTemp:Long=0L
    private var selectedImageUri: Uri? = null
    private var golesAntiguos = 0L
    private var mvpAntiguos: Long = 0L
    private var asistenciasAntiguas = 0L
    var nombre: String? = null

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
                Log.d("Firebase",asistenciasAntiguas.toString() )
                golesAntiguos = jugadorCargado?.goles ?: 0L
                Log.d("Firebase",golesAntiguos.toString() )
                mvpAntiguos = jugadorCargado?.valoracion ?: 0L

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
                            nuevosMvp = 1L
                        } else {
                            nuevosMvp = 0L
                        }
                        cargarDatosJugador(nombre!!,fecha) { jugadorCargado ->
                            asistenciasAntiguas = jugadorCargado?.asistencias ?: 0L
                            Log.d("Firebase",asistenciasAntiguas.toString() )
                            golesAntiguos = jugadorCargado?.goles ?: 0L
                            Log.d("Firebase",golesAntiguos.toString() )
                            mvpAntiguos = jugadorCargado?.valoracion ?: 0L
                            Log.d("Firebase",golesAntiguos.toString() )
                            nuevosGolesTemp = golesText.toLong() - golesAntiguos
                            Log.d("Firebase",asistenciasAntiguas.toString() )
                            nuevasAsistenciasTemp = golesText.toLong() - asistenciasAntiguas
                            nuevosMvpTemp = nuevosMvp-mvpAntiguos
                        }


                        mostrarConfirmacionCambios(
                            jugador.nombre,
                            golesText.toLong(),
                            golesText.toLong()
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
                    actualizarPartidoEnFirestore(
                        fecha,
                        nuevosGoles,
                        nuevasAsistencias,
                        nuevosMvp,
                        horaIni,
                        horaFin,
                        nombreJugador

                    )
                    actualizarDatosEnFirestoreJugadores(
                        nombreJugador,
                        nuevosGolesTemp,
                        nuevasAsistenciasTemp,
                        nuevosMvpTemp
                    )
                    notifyDataSetChanged()
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    // Descartar cambios temporales
                }
                .create()
            dialog.show()
        }
        // Función para actualizar los datos de un jugador en Firestore para un partido
        private fun actualizarPartidoEnFirestore(
            fechaPartido: String,
            nuevosGoles: Long,
            nuevasAsistencias: Long,
            nuevaValoracion: Long,
            nuevaHoraInicio: String,
            nuevaHoraFin: String,
            nombreJugador:String
        ) {
            val currentUserEmail = firebaseAuth.currentUser?.email

            if (currentUserEmail != null) {
                val partidosCollection =
                    db.collection("usuarios").document(currentUserEmail)
                        .collection("partidos")

                partidosCollection.whereEqualTo("fecha", fechaPartido).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documents = task.result

                            try {
                                if (documents != null && !documents.isEmpty) {
                                    val document = documents.documents[0]

                                    val jugadoresData =
                                        document.get("jugadores") as List<HashMap<String, Any>>?

                                    // Actualizar jugadores
                                    jugadoresData?.forEach { jugadorData ->
                                        if (jugadorData["nombre"] == nombreJugador) {
                                            jugadorData["goles"] = nuevosGoles
                                            jugadorData["asistencias"] = nuevasAsistencias
                                            jugadorData["valoracion"] = nuevaValoracion
                                        }
                                    }

                                    // Actualizar datos generales del partido
                                    document.reference.update(
                                        "horaInicio", nuevaHoraInicio,
                                        "horaFin", nuevaHoraFin,
                                        "jugadores", jugadoresData
                                    ).addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful) {
                                            Log.d("Firebase", "Datos del partido actualizados correctamente")
                                        } else {
                                            Log.e("Firebase", "Error al actualizar datos del partido", updateTask.exception)
                                        }
                                    }
                                } else {
                                    Log.d("Firebase", "No hay datos de partidos para la fecha especificada")
                                }
                            } catch (e: Exception) {
                                Log.e("Firebase", "Error al procesar datos del partido", e)
                            }
                        } else {
                            Log.e("Firebase", "Error al obtener datos del partido", task.exception)
                        }
                    }
            } else {
                Log.e("Firebase", "Usuario no autenticado")
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
                        .collection("partidos")
                var callbackInvoked = false
                jugadoresCollection.whereEqualTo("fecha", nombreJugador).get()
                    .addOnCompleteListener { task ->
                        if (!callbackInvoked) { // Evitar invocar el callback más de una vez
                            callbackInvoked = true

                            if (task.isSuccessful) {
                                val documents = task.result

                                try {
                                    if (documents != null && !documents.isEmpty) {
                                        val document = documents.documents[0] // Tomamos el primer documento (suponemos nombres únicos)

                                        // Obtener la lista de jugadores del partido
                                        val jugadoresData =
                                            document.get("jugadores") as List<HashMap<String, Any>>?

                                        // Buscar el jugador por nombre en la lista
                                        val jugadorData = jugadoresData?.find {
                                            it["nombre"] == fechaPartido
                                        }

                                        if (jugadorData != null) {
                                            val nombre = jugadorData["nombre"] as String
                                            val valoracion = jugadorData["valoracion"] as Long
                                            val posicion = jugadorData["posicion"] as String
                                            val goles = jugadorData["goles"] as Long
                                            val asistencias = jugadorData["asistencias"] as Long
                                            // Agrega logs para verificar los valores



                                            val jugador: JugadorBase = when (posicion) {
                                                "Portero" -> Portero(valoracion, nombre, posicion, goles, asistencias, selectedImageUri.toString())

                                                "Jugador Normal" -> Jugadores(valoracion, nombre, posicion, goles, asistencias, selectedImageUri.toString())
                                                else -> throw IllegalArgumentException("Posición de jugador desconocida: $posicion")
                                            }

                                            callback.invoke(jugador)
                                        } else {
                                            Log.d("Firebase", "El jugador no existe en la lista")
                                            callback.invoke(null)
                                        }
                                    } else {
                                        Log.d("Firebase", "No hay datos de jugadores para la fecha especificada")
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
            nuevaValoracion: Long
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


