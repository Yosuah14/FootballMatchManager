package com.example.footballmatchmanager

import Adaptadores.JugadoresAdapter2
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

    private lateinit var binding: ActivityCrearPartidosBinding
    private lateinit var jugadoresList: MutableList<JugadorBase>
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPartidosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        jugadoresList = mutableListOf()

        binding.btnCrearPartido.setOnClickListener {
            mostrarDialogoCrearPartido()
        }
    }

    private fun mostrarDialogoCrearPartido() {
        val builder = AlertDialog.Builder(this)
        val dialogBinding = DialogCrearPartidosBinding.inflate(layoutInflater)
        builder.setView(dialogBinding.root)

        builder.setPositiveButton("Crear") { _, _ ->
            // Obtener datos del diálogo y crear una instancia de Partido
            val fecha = dialogBinding.editTextFecha.text.toString()
            val horaInicio = dialogBinding.editTextHora.text.toString()

            // Verificar que todos los campos obligatorios estén llenos
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
                        val horaFin = sumarUnaHora(horaInicioSeleccionada)
                        val horaFin2 = horaFin.toString()
                        if (horaFin != null) {
                            // Crear una nueva instancia de partido
                            val nuevoPartido = Partido(
                                fecha = fecha,
                                horaInicio = horaInicio,
                                horaFin = horaFin2,
                                jugadores = emptyList() // La lista se actualizará en el segundo diálogo
                                // Puedes agregar más campos del partido según tus necesidades
                            )

                            // Mostrar el diálogo "Agregar Jugadores" cuando se crea el partido
                            mostrarDialogoAgregarJugadores(nuevoPartido)
                        }
                    } else {
                        mostrarMensajeError("La fecha debe ser mayor o igual a la fecha actual")
                    }
                } catch (e: Exception) {
                    mostrarMensajeError("Error al procesar la información del partido")
                }
            } else {
                mostrarMensajeError("Todos los campos obligatorios deben estar rellenos")
            }
        }

        builder.setNegativeButton("Cancelar", null)

        val dialog = builder.create()

        // Al hacer clic en "Cancelar" o cerrar el diálogo, no restablecer los datos
        dialog.setOnDismissListener {
            // Puedes realizar alguna acción si es necesario
        }

        dialog.show()

        // Configurar la lógica de visibilidad del botón "Seleccionar" en el adaptador
        jugadoresList.clear()
        val jugadoresAdapter = configurarDialogoAgregarJugadores()

        // Cargar los datos de Firebase en el RecyclerView del diálogo
        cargarDatosFirebaseEnDialog(jugadoresAdapter)
    }
    private fun mostrarDialogoAgregarJugadores(partido: Partido) {
        // Configurar el diálogo para agregar jugadores
        val inflater = LayoutInflater.from(this)
        val dialogBinding = DialogagregarjugadoresBinding.inflate(inflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        // Configurar el RecyclerView en el diálogo con el JugadoresAdapter2
        val jugadoresAdapter = JugadoresAdapter2(jugadoresList)
        dialogBinding.recyclerViewJugadoresDialog.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = jugadoresAdapter
        }

        // Configurar el botón "Seleccionar"
        dialogBinding.btnAgregarJugadores.setOnClickListener {
            // Obtener la lista de jugadores seleccionados
            val jugadoresSeleccionados = jugadoresAdapter.getSelectedJugadores().toMutableList()

            // Mostrar temporalmente "Jugador Agregado" y actualizar el adaptador
            jugadoresSeleccionados.forEach { jugador ->
                jugador.isAddedTemporarily = true
            }
            jugadoresAdapter.notifyDataSetChanged()

            // Actualizar la lista de jugadores en el partido
            partido.jugadores = jugadoresSeleccionados

            // Realizar acciones adicionales con el partido si es necesario
            // Por ejemplo, almacenar en Firebase, etc.

            // Cerrar el diálogo después de realizar las acciones necesarias
            dialog.dismiss()
        }

        // Mostrar el diálogo
        dialog.show()
    }

    private fun configurarDialogoAgregarJugadores(): JugadoresAdapter2 {
        // Configurar el diálogo para agregar jugadores
        val inflater = LayoutInflater.from(this)
        val dialogBinding = DialogagregarjugadoresBinding.inflate(inflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        // Configurar el RecyclerView en el diálogo con el JugadoresAdapter2
        val jugadoresAdapter = JugadoresAdapter2(jugadoresList)
        dialogBinding.recyclerViewJugadoresDialog.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = jugadoresAdapter
        }

        // Configurar el botón "Seleccionar"
        dialogBinding.btnAgregarJugadores.setOnClickListener {
            // Obtener la lista de jugadores seleccionados
            val jugadoresSeleccionados = jugadoresAdapter.getSelectedJugadores().toMutableList()

            // Mostrar temporalmente "Jugador Agregado" y actualizar el adaptador
            jugadoresSeleccionados.forEach { jugador ->
                jugador.isAddedTemporarily = true
            }
            jugadoresAdapter.notifyDataSetChanged()

            // Cerrar el diálogo después de realizar las acciones necesarias
            dialog.dismiss()
        }

        // Mostrar el diálogo
        dialog.show()

        return jugadoresAdapter
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
                                val goles = document.getLong("goles")?.toInt()
                                val asistencias = document.getLong("asistencias")?.toInt()
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
    private fun sumarUnaHora(fecha: Date?): Date? {
        if (fecha == null) {
            return null
        }
        val calendar = Calendar.getInstance()
        calendar.time = fecha
        calendar.add(Calendar.HOUR_OF_DAY, 1)

        return calendar.time
    }

    private fun mostrarMensajeError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}




