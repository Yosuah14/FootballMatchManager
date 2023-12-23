package com.example.footballmatchmanager

import Adaptadores.JugadoresAdapter
import Adaptadores.JugadoresAdapter3

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
    private var agregados = false
    private var partidosAdapter: PartidosAdapter? = null
    var cargado=false
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
        var nuevoPartido = Partido()
        //var nuevoPartido: Partido? = null
        var jugadoresSeleccionados =
            false // Variable para rastrear si se han seleccionado jugadores
        dialogBinding.btnAgregarJugadores.setOnClickListener {

                // Configurar el diálogo para agregar jugadores
                val inflater = LayoutInflater.from(this)
                val dialogBinding = DialogagregarjugadoresBinding.inflate(inflater)
                val builder = AlertDialog.Builder(this)
                builder.setView(dialogBinding.root)
                // Configurar el RecyclerView en el diálogo con el JugadoresAdapter2
                val jugadoresAdapter = JugadoresAdapter2(jugadoresList)
                // Cargar los datos de Firebase en el RecyclerView del diálogo
                dialogBinding.recyclerViewJugadoresDialog.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = jugadoresAdapter
                }
                val jugadoresSeleccionados =
                    jugadoresAdapter.getJugadoresSeleccionados().toMutableList()
                Log.d("JugadoresPartidos", jugadoresSeleccionados.toString())
                jugadoresPartidos.addAll(jugadoresSeleccionados)
                Log.d("JugadoresPartidos", jugadoresPartidos.toString())
                if (!cargado){
                    cargarDatosFirebaseEnDialog(jugadoresAdapter)
                    cargado=true
                }else{
                    cargado=true
                }
                builder.setPositiveButton("Eliminar") { _, _ ->

                    // Cerrar el diálogo después de realizar las acciones necesarias
                    // (En este punto, los jugadores seleccionados han sido eliminados temporalmente del RecyclerView)
                    dialogBinding.root.postDelayed(
                        { builder.create().dismiss() },
                        500
                    ) // Cerrar el diálogo con un retraso de 500 milisegundos
                }
                // Configurar el botón "Cancelar"
                builder.setNegativeButton("Cancelar") { _, _ ->
                }
                // Mostrar el diálogo correctamente
                val dialog = builder.create()
                dialog.show()
            }

        builder.setPositiveButton("Crear") { _, _ ->
            val fecha = dialogBinding.editTextFecha.text.toString()
            val horaInicio = dialogBinding.editTextHora.text.toString()
            Log.d("JugadoresPartidos", jugadoresSeleccionados.toString())

            if (fecha.isNotEmpty() && horaInicio.isNotEmpty()) {
                try {
                    // Validar la fecha y la hora
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val fechaActual = Date()
                    val fechaSeleccionada = dateFormat.parse(fecha)
                    val horaInicioSeleccionada = timeFormat.parse(horaInicio)

                    // Verificar que la fecha sea mayor o igual a la fecha actual
                    if (fechaSeleccionada != null && fechaSeleccionada >= fechaActual) {
                        // Validar que la hora final sea una hora después de la hora inicial
                        val horaFin: String? = sumarUnaHoraObtenerHora(horaInicioSeleccionada)
                        val horaFin2 = horaFin.toString()

                        if (horaFin != null) {
                            // Actualizar la variable jugadoresSeleccionados
                            jugadoresSeleccionados = !jugadoresList.isEmpty()
                            Log.d("JugadoresPartidos", jugadoresSeleccionados.toString())
                            Log.d("JugadoresPartidos", jugadoresList.toString())

                            // Verificar si ya existe un partido para la fecha seleccionada
                            partidoExistenteEnFirestore(fecha) { partidoExistente ->
                                if (!partidoExistente) {
                                    // Crear una nueva instancia de partido solo si hay jugadores seleccionados
                                    if (jugadoresSeleccionados) {
                                        val nuevoPartido = Partido(
                                            fecha = fecha,
                                            horaInicio = horaInicio,
                                            horaFin = horaFin2,
                                            jugadores = jugadoresList
                                        )

                                        // Agregar el nuevo partido a la lista de partidos
                                        partidosList.add(nuevoPartido)

                                        // Guardar el partido en Firestore
                                        guardarPartidoEnFirestore(nuevoPartido)

                                        // Notificar al adaptador sobre el cambio en los datos
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
                        cargado = false
                    }
                } catch (e: Exception) {
                    mostrarMensajeError("Error al procesar la información del partido")
                    cargado = false
                }
            } else {
                mostrarMensajeError("Todos los campos obligatorios deben estar rellenos")
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
            val jugadoresCollection = db.collection("usuarios").document(currentUserEmail).collection("jugadores")
            jugadoresCollection.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
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
                                    "Portero" -> Portero(valoracion!!, nombre!!, posicion!!, goles!!, asistencias!!)
                                    "Jugador Normal" -> Jugadores(valoracion!!, nombre!!, posicion!!, goles!!, asistencias!!)
                                    else -> null
                                }
                                // Agregar el jugador a la lista si se creó correctamente
                                jugador?.let {
                                    jugadoresList.add(it)
                                }
                            } catch (e: Exception) {
                                Log.e("Firebase", "Error al convertir documento a JugadorBase", e)
                            }
                        }
                        // Notificar al adaptador sobre el cambio en los datos
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

        // Obtener solo la hora en formato HH:mm
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatoHora.format(calendar.time)
    }

    private fun mostrarMensajeError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
    private fun guardarPartidoEnFirestore(partido: Partido) {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val partidosCollection = db.collection("usuarios").document(currentUserEmail).collection("partidos")

            // Convertir la lista de jugadores a un formato que Firestore pueda manejar
            val jugadoresData = partido.jugadores?.map { jugador ->
                mapOf(
                    "nombre" to jugador.nombre,
                    "valoracion" to jugador.valoracion,
                    "posicion" to jugador.posicion,
                    "goles" to jugador.goles,
                    "asistencias" to jugador.asistencias
                    // Otros campos según tu modelo de datos para JugadorBase
                )
            }

            val partidoData = hashMapOf(
                "fecha" to partido.fecha,
                "horaInicio" to partido.horaInicio,
                "horaFin" to partido.horaFin,
                "jugadores" to jugadoresData
            )

            // Agregar el partido a la colección en Firestore
            partidosCollection.add(partidoData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Partido guardado exitosamente en Firestore", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar el partido en Firestore", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun leerPartidosDelUsuario() {
        val currentUserEmail = firebaseAuth.currentUser?.email

        if (currentUserEmail != null) {
            val partidosCollection = db.collection("usuarios").document(currentUserEmail).collection("partidos")

            partidosCollection.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            try {
                                // Obtener los datos del partido del documento
                                val fecha = document.getString("fecha")
                                val horaInicio = document.getString("horaInicio")
                                val horaFin = document.getString("horaFin")

                                // Obtener la lista de jugadores
                                val jugadoresData = document.get("jugadores") as List<HashMap<String, Any>>?
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

                                // Crear el objeto Partido y agregarlo a la lista de partidos
                                val partido = Partido(fecha!!, horaInicio!!, horaFin!!, jugadores)
                                partidosList.add(partido)

                            } catch (e: Exception) {
                                Log.e("Firebase", "Error al convertir documento a Partido", e)
                            }
                        }

                        // Notificar al adaptador sobre el cambio en los datos
                        partidosAdapter?.notifyDataSetChanged()
                    } else {
                        Log.e("Firebase", "Error al obtener los partidos", task.exception)
                        Toast.makeText(this, "Error al obtener los partidos", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Log.e("Firebase", "El email del usuario es nulo")
            // Puedes mostrar un mensaje o realizar alguna acción adecuada si el email es nulo
        }
    }
    private fun partidoExistenteEnFirestore(fecha: String, onComplete: (Boolean) -> Unit) {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val partidosCollection = db.collection("usuarios").document(currentUserEmail).collection("partidos")

            partidosCollection.whereEqualTo("fecha", fecha).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val partidos = task.result?.toObjects(Partido::class.java)
                        onComplete(partidos?.isNotEmpty() == true)
                    } else {
                        onComplete(false)
                        Log.e("Firebase", "Error al comprobar partido existente en Firestore", task.exception)
                    }
                }
        } else {
            onComplete(false)
            Log.e("Firebase", "El email del usuario es nulo")
        }
    }

}




