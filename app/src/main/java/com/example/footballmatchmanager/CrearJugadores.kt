package com.example.footballmatchmanager

import Adaptadores.JugadoresAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footballmatchmanager.databinding.ActivityCrearJugadoresBinding
import com.example.footballmatchmanager.JugadorBase
import com.example.footballmatchmanager.Portero
import com.example.footballmatchmanager.Jugadores
import com.example.footballmatchmanager.databinding.DialogCrearJugadorBinding

class CrearJugadores : AppCompatActivity() {
    private lateinit var binding: ActivityCrearJugadoresBinding
    private val jugadoresList: MutableList<JugadorBase> = mutableListOf()
    private lateinit var jugadoresAdapter: JugadoresAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearJugadoresBinding.inflate(layoutInflater)
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
                // Verificar que solo se haya seleccionado un checkbox
                val checkBoxPorteroChecked = dialogBinding.checkBoxPortero.isChecked
                val checkBoxJugadorChecked = dialogBinding.checkBoxJugador.isChecked

                if (checkBoxPorteroChecked xor checkBoxJugadorChecked) { // Solo uno de los dos puede estar seleccionado
                    // Verificar que los campos numéricos contengan valores válidos
                    val valoracionDouble = valoracion.toDoubleOrNull()
                    val golesInt = goles.toIntOrNull()
                    val asistenciasInt = asistencias.toIntOrNull()

                    if (valoracionDouble != null && golesInt != null && asistenciasInt != null) {
                        // Verificar qué tipo de jugador se seleccionó y crear la instancia correspondiente
                        val posicion = if (checkBoxPorteroChecked) "Portero" else "Jugador Normal"

                        val nuevoJugador = when (posicion) {
                            "Portero" -> Portero(valoracionDouble, nombre, "Portero")
                            "Jugador Normal" -> Jugadores(valoracionDouble, nombre, golesInt, asistenciasInt, "Jugador Normal")
                            else -> null
                        }

                        // Si se creó un nuevo jugador, agregarlo a la lista y actualizar el RecyclerView
                        nuevoJugador?.let {
                            jugadoresList.add(it)
                            jugadoresAdapter.notifyDataSetChanged()
                        }
                    } else {
                        Toast.makeText(this, "Ingresa valores numéricos válidos en los campos correspondientes", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Selecciona solo un tipo de jugador", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Todos los campos deben estar rellenos", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar", null)

        builder.create().show()
    }

}
