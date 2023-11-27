package com.example.footballmatchmanager

import Adaptadores.JugadoresAdapter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footballmatchmanager.databinding.ActivityCrearJugadoresBinding
import com.example.footballmatchmanager.databinding.DialogCrearJugadorBinding

class CrearJugadores : AppCompatActivity() {
    private val binding by lazy {
        ActivityCrearJugadoresBinding.inflate(layoutInflater)
    }

    private val jugadoresList: MutableList<JugadorBase> = mutableListOf()
    private lateinit var jugadoresAdapter: JugadoresAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Configurar el RecyclerView
        jugadoresAdapter = JugadoresAdapter(jugadoresList)
        binding.recyclerViewJugadores.apply {
            layoutManager = LinearLayoutManager(this@CrearJugadores)
            adapter = jugadoresAdapter
        }

        // Configurar el botón para crear jugadores
        binding.btnCrearJugador.setOnClickListener {
            mostrarDialogoCrearJugador()
        }

        // Configurar la Toolbar con la flecha de retroceso
        setSupportActionBar(binding.toolbarCrearJugadores)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarCrearJugadores.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun mostrarDialogoCrearJugador() {
        val builder = AlertDialog.Builder(this)
        val dialogBinding = DialogCrearJugadorBinding.inflate(layoutInflater)
        builder.setView(dialogBinding.root)

        builder.setPositiveButton("Crear") { _, _ ->
            // Obtener datos del diálogo y crear una instancia de JugadorBase (Portero o Jugador)
            val nombre = dialogBinding.editTextNombre.text.toString()
            val valoracion = dialogBinding.editTextValoracion.text.toString()
            val goles = dialogBinding.editTextGoles.text.toString()
            val asistencias = dialogBinding.editTextAsistencias.text.toString()

            // Verificar que todos los campos estén llenos
            if (nombre.isNotEmpty() && valoracion.isNotEmpty() && goles.isNotEmpty() && asistencias.isNotEmpty()) {
                // Obtener la posición seleccionada del RadioGroup
                val radioButtonPosicionId = dialogBinding.radioGroupPosicion.checkedRadioButtonId

                // Verificar que la posición no sea nula
                if (radioButtonPosicionId != View.NO_ID) {
                    // Obtener el texto del RadioButton seleccionado
                    val posicion = when (radioButtonPosicionId) {
                        dialogBinding.radioButtonPortero.id -> "Portero"
                        dialogBinding.radioButtonJugador.id -> "Jugador Normal"
                        else -> null
                    }

                    // Verificar que los campos numéricos contengan valores válidos
                    val valoracionDouble = valoracion.toDoubleOrNull()
                    val golesInt = goles.toIntOrNull()
                    val asistenciasInt = asistencias.toIntOrNull()

                    if (valoracionDouble != null && golesInt != null && asistenciasInt != null) {
                        // Crear la instancia correspondiente al tipo de jugador seleccionado
                        val nuevoJugador = when (posicion) {
                            "Portero" -> Portero(valoracionDouble, nombre, "Portero")
                            "Jugador Normal" -> Jugadores(
                                valoracionDouble,
                                nombre,
                                golesInt,
                                asistenciasInt,
                                "Jugador Normal"
                            )
                            else -> null
                        }

                        // Si se creó un nuevo jugador, agregarlo a la lista y actualizar el RecyclerView
                        nuevoJugador?.let {
                            jugadoresList.add(it)
                            jugadoresAdapter.notifyDataSetChanged()
                        }
                    } else {
                        mostrarMensajeError("Ingresa valores numéricos válidos en los campos correspondientes")
                    }
                } else {
                    mostrarMensajeError("Selecciona un tipo de jugador")
                }
            } else {
                mostrarMensajeError("Todos los campos deben estar rellenos")
            }
        }

        builder.setNegativeButton("Cancelar", null)

        builder.create().show()
    }

    private fun mostrarMensajeError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}


