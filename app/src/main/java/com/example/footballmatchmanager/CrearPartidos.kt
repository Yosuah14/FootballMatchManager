package com.example.footballmatchmanager

import Adaptadores.PartidosAdapter
import JugadoresAdapter2
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footballmatchmanager.databinding.ActivityCrearPartidosBinding
import com.example.footballmatchmanager.databinding.DialogCrearPartidosBinding
import com.example.footballmatchmanager.databinding.DialogagregarjugadoresBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CrearPartidos : AppCompatActivity() {
    private lateinit var partidosList: MutableList<Partido>
    private lateinit var binding: ActivityCrearPartidosBinding
    private lateinit var jugadoresList: MutableList<JugadorBase>
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val jugadoresPartidos: MutableList<JugadorBase> = mutableListOf()
    private var partidosAdapter: PartidosAdapter? = null
    var cargado = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPartidosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        leerPartidosDelUsuario()
        jugadoresList = mutableListOf()
        partidosList = mutableListOf()
        partidosAdapter = PartidosAdapter(partidosList)
        binding.recyclerViewPartidos.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPartidos.adapter = partidosAdapter

        binding.btnCrearPartido.setOnClickListener {
            mostrarDialogoCrearPartido()
        }
        // Configurar la Toolbar con la flecha de retroceso
        setSupportActionBar(binding.toolbarPartidos)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarPartidos.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    private fun mostrarDialogoCrearPartido() {
        val builder = AlertDialog.Builder(this)
        val dialogBinding = DialogCrearPartidosBinding.inflate(layoutInflater)
        builder.setView(dialogBinding.root)
        var jugadoresSeleccionados = false
        dialogBinding.btnAgregarJugadores.setOnClickListener {
            val inflater = LayoutInflater.from(this)
            val dialogBinding = DialogagregarjugadoresBinding.inflate(inflater)
            val builder = AlertDialog.Builder(this)
            builder.setView(dialogBinding.root)

            val jugadoresAdapter = JugadoresAdapter2(jugadoresList)
            dialogBinding.recyclerViewJugadoresDialog.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = jugadoresAdapter
            }
            val jugadoresSeleccionados = jugadoresAdapter.getJugadoresSeleccionados().toMutableList()
            jugadoresPartidos.addAll(jugadoresSeleccionados)

            if (!cargado) {
                cargarDatosFirebaseEnDialog(jugadoresAdapter)
                cargado = true
            } else {
                cargado = true
            }
            builder.setPositiveButton("Eliminar") { _, _ ->
                dialogBinding.root.postDelayed(
                    { builder.create().dismiss() },
                    500
                )
            }
            builder.setNegativeButton("Cancelar") { _, _ ->
            }
            val dialog = builder.create()
            dialog.show()
        }
        builder.setPositiveButton("Crear") { _, _ ->
            val fecha = dialogBinding.editTextFecha.text.toString()
            val horaInicio = dialogBinding.editTextHora.text.toString()

            if (fecha.isNotEmpty() && horaInicio.isNotEmpty()) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN)
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMAN)
                    val fechaActual = Date()
                    val fechaSeleccionada = dateFormat.parse(fecha)
                    val horaInicioSeleccionada = timeFormat.parse(horaInicio)

                    // Combina la fecha y la hora para obtener una nueva fecha con la hora especificada
                    val calendar = Calendar.getInstance()
                    calendar.time = fechaSeleccionada ?: Date() // Usa la fecha actual si fechaSeleccionada es null
                    calendar.set(Calendar.HOUR_OF_DAY, horaInicioSeleccionada?.hours ?: 0)
                    calendar.set(Calendar.MINUTE, horaInicioSeleccionada?.minutes ?: 0)

                    // El objeto Date combinado
                    val fechaConHora = calendar.time

                    if (fechaSeleccionada != null && fechaConHor >= fechaActual) {
                        val horaFin: String? = sumarUnaHoraObtenerHora(horaInicioSeleccionada)
                        val horaFin2 = horaFin.toString()

                        if (horaFin != null) {
                            jugadoresSeleccionados = !jugadoresList.isEmpty()

                            partidoExistenteEnFirestore(fecha) { partidoExistente ->
                                if (!partidoExistente) {
                                    if (jugadoresSeleccionados) {
                                        val nuevoPartido = Partido(
                                            fecha = fecha,
                                            horaInicio = horaInicio,
                                            horaFin = horaFin2,
                                            jugadores = jugadoresList
                                        )
                                        partidosList.add(nuevoPartido)
                                        guardarPartidoEnFirestore(nuevoPartido)

                                        partidosAdapter?.notifyDataSetChanged()
                                        jugadoresList.clear()
                                        cargado = false
                                    } else {
                                        mostrarMensajeError("Debes seleccionar al menos un jugador")
                                    }
                                } else {
                                    mostrarMensajeError("Ya tienes un partido para esta fecha")
                                }
                            }
                        }
                    } else {
                        mostrarMensajeError("La fecha debe ser mayor o igual a la fecha actual")
                        jugadoresList.clear()
                        cargado = false
                    }
                } catch (e: Exception) {
                    mostrarMensajeError("Error al procesar la información del partido")
                    jugadoresList.clear()
                    cargado = false
                }
            } else {
                mostrarMensajeError("Todos los campos obligatorios deben estar rellenos")
                jugadoresList.clear()
                cargado = false
            }
        }
        builder.setNegativeButton("Cancelar", null)
        val dialog = builder.create()
        dialog.show()
    }
    private fun cargarDatosFirebaseEnDialog(adapter: JugadoresAdapter2) {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val jugadoresCollection =
                db.collection("usuarios").document(currentUserEmail).collection("jugadores")

            jugadoresCollection.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
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
                                        imageUrl
                                    )
                                    "Jugador Normal" -> Jugadores(
                                        valoracion!!,
                                        nombre!!,
                                        posicion!!,
                                        goles!!,
                                        asistencias!!,
                                        imageUrl
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
                        adapter.notifyDataSetChanged()
                    } else {
                        Log.e("Firebase", "Error al obtener los jugadores", task.exception)
                        Toast.makeText(this, "Error al obtener los jugadores", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
    private fun sumarUnaHoraObtenerHora(fecha: Date?): String? {
        if (fecha == null) {
            return null
        }
        val calendar = Calendar.getInstance()
        calendar.time = fecha
        calendar.add(Calendar.HOUR_OF_DAY, 1)

        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatoHora.format(calendar.time)
    }
    private fun mostrarMensajeError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
    private fun guardarPartidoEnFirestore(partido: Partido) {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val partidosCollection =
                db.collection("usuarios").document(currentUserEmail).collection("partidos")
            val jugadoresData = partido.jugadores?.map { jugador ->
                mapOf(
                    "nombre" to jugador.nombre,
                    "valoracion" to 0L,
                    "posicion" to jugador.posicion,
                    "goles" to 0L,
                    "asistencias" to 0L,
                    "imageUrl" to jugador.imagenUrl
                )
            }
            val partidoData = hashMapOf(
                "fecha" to partido.fecha,
                "horaInicio" to partido.horaInicio,
                "horaFin" to partido.horaFin,
                "jugadores" to jugadoresData
            )

            partidosCollection.add(partidoData)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Partido guardado exitosamente en Firestore",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Error al guardar el partido en Firestore",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
    private fun leerPartidosDelUsuario() {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val partidosCollection =
                db.collection("usuarios").document(currentUserEmail).collection("partidos")

            partidosCollection.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            try {
                                Log.d("Firebase", "Antes de la línea 301")
                                val fecha = document.getString("fecha")
                                val horaInicio = document.getString("horaInicio")
                                val horaFin = document.getString("horaFin")
                                Log.d("Firebase", fecha+horaInicio+horaFin)

                                if (fecha != null && horaInicio != null && horaFin != null) {
                                    val jugadoresData =
                                        document.get("jugadores") as? List<HashMap<String, Any>>?

                                    val jugadores = jugadoresData?.mapNotNull { jugadorData ->
                                        try {
                                            JugadorBase(
                                                jugadorData["valoracion"] as? Long ?: 0,
                                                jugadorData["nombre"]?.toString() ?: "",
                                                jugadorData["posicion"]?.toString() ?: "",
                                                jugadorData["goles"] as? Long ?: 0,
                                                jugadorData["asistencias"] as? Long ?: 0,
                                                jugadorData["imageUrl"]?.toString()
                                            )
                                        } catch (e: Exception) {
                                            Log.e("Firebase", "Error al convertir jugadorData a JugadorBase", e)
                                            null
                                        }
                                    }

                                    val partido = Partido(fecha, horaInicio, horaFin, jugadores)
                                    partidosList.add(partido)

                                    Log.d("Firebase", "Partido agregado correctamente")

                                } else {
                                    Log.e("Firebase", "Alguno de los campos fecha, horaInicio, horaFin es null")
                                }

                                Log.d("Firebase", "Después de la línea 301")

                            } catch (e: Exception) {
                                Log.e("Firebase", "Error al convertir documento a Partido", e)
                            }
                        }

                        partidosAdapter?.notifyDataSetChanged()
                    } else {
                        Log.e("Firebase", "Error al obtener los partidos", task.exception)
                        Toast.makeText(this, "Error al obtener los partidos", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        } else {
            Log.e("Firebase", "El email del usuario es nulo")
        }
    }
    private fun partidoExistenteEnFirestore(fecha: String, onComplete: (Boolean) -> Unit) {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val partidosCollection =
                db.collection("usuarios").document(currentUserEmail).collection("partidos")

            partidosCollection.whereEqualTo("fecha", fecha).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val partidos = task.result?.toObjects(Partido::class.java)
                        onComplete(partidos?.isNotEmpty() == true)
                    } else {
                        onComplete(false)
                        Log.e(
                            "Firebase",
                            "Error al comprobar partido existente en Firestore",
                            task.exception
                        )
                    }
                }
        } else {
            onComplete(false)
            Log.e("Firebase", "El email del usuario es nulo")
        }
    }
}




