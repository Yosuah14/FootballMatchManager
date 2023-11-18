package com.example.footballmatchmanager

import Adaptadores.JugadoresAdapter
import android.os.Bundle
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
            val valoracion = dialogBinding.editTextValoracion.text.toString().toDoubleOrNull() ?: 0.0
            val goles = dialogBinding.editTextGoles.text.toString().toIntOrNull() ?: 0
            val asistencias = dialogBinding.editTextAsistencias.text.toString().toIntOrNull() ?: 0

            // Verificar qué tipo de jugador se seleccionó y crear la instancia correspondiente
            val posicion = when {
                dialogBinding.checkBoxPortero.isChecked -> "Portero"
                dialogBinding.checkBoxJugador.isChecked -> "Jugador Normal"
                else -> ""
            }

            val nuevoJugador = when (posicion) {
                "Portero" -> Portero(valoracion, nombre)
                "Jugador Normal" -> Jugadores(valoracion, nombre, goles, asistencias)
                else -> null
            }

            // Asignar la posición al nuevo jugador
            nuevoJugador?.posicion = posicion

            // Si se creó un nuevo jugador, agregarlo a la lista y actualizar el RecyclerView
            nuevoJugador?.let {
                jugadoresList.add(it)
                jugadoresAdapter.notifyDataSetChanged()
            }
        }

        builder.setNegativeButton("Cancelar", null)

        builder.create().show()
    }
}
