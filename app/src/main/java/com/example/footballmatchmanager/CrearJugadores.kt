package com.example.footballmatchmanager

import Adaptadores.JugadoresAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footballmatchmanager.databinding.ActivityCrearJugadoresBinding
import com.example.footballmatchmanager.databinding.DialogCrearJugadorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CrearJugadores : AppCompatActivity() {
    private val binding by lazy {
        ActivityCrearJugadoresBinding.inflate(layoutInflater)
    }

    private val jugadoresList: MutableList<JugadorBase> = mutableListOf()
    private lateinit var jugadoresAdapter: JugadoresAdapter
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // Leer los jugadores existentes del usuario actual
        leerJugadoresDelUsuario()

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
            // Obtener datos del diálogo y crear una instancia de Jugador
            val nombre = dialogBinding.editTextNombre.text.toString()
            val valoracion = dialogBinding.editTextValoracion.text.toString()
            val goles = dialogBinding.editTextGoles.text.toString()
            val asistencias = dialogBinding.editTextAsistencias.text.toString()

            // Verificar que todos los campos obligatorios estén llenos
            if (nombre.isNotEmpty() && valoracion.isNotEmpty() && goles.isNotEmpty() && asistencias.isNotEmpty()) {
                try {
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

                        val nuevoJugador = when (posicion) {
                            "Portero" -> Portero( valoracion.toDouble(), nombre,"Portero",goles.toLong(), asistencias.toLong())
                            "Jugador Normal" -> Jugadores(valoracion.toDouble(), nombre,"Jugador Normal",goles.toLong(), asistencias.toLong() )
                            else -> null
                        }

                        // Si se creó un nuevo jugador, agregarlo a la lista y actualizar el RecyclerView
                        nuevoJugador?.let {
                            jugadoresList.add(it)
                            jugadoresAdapter.notifyDataSetChanged()
                            // Guardar el jugador en Firestore
                            guardarJugadorEnFirestore(it)
                        }
                    } else {
                        mostrarMensajeError("Selecciona un tipo de jugador")
                        // No cierres el diálogo en caso de error
                        return@setPositiveButton
                    }
                } catch (e: Exception) {
                    mostrarMensajeError("Error al procesar la información del jugador")
                    // No cierres el diálogo en caso de error
                    return@setPositiveButton
                }
            } else {
                mostrarMensajeError("Todos los campos obligatorios deben estar rellenos")
                // No cierres el diálogo en caso de error
                return@setPositiveButton
            }
        }

        builder.setNegativeButton("Cancelar", null)

        val dialog = builder.create()

        // Al hacer clic en "Cancelar" o cerrar el diálogo, no restablecer los datos
        dialog.setOnDismissListener {
            // Puedes realizar alguna acción si es necesario
        }

        dialog.show()
    }

    private fun guardarJugadorEnFirestore(jugador: JugadorBase) {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val jugadoresCollection = db.collection("usuarios").document(currentUserEmail).collection("jugadores")

            val jugadorData = hashMapOf(
                "nombre" to jugador.nombre,
                "valoracion" to jugador.valoracion,
                "posicion" to jugador.posicion,
                "goles" to jugador.goles ,
                "asistencias" to jugador.asistencias
                // Otros campos según tu modelo de datos
            )
            // Agregar el jugador a la colección en Firestore
            jugadoresCollection.add(jugadorData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Jugador guardado exitosamente en Firestore", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar el jugador en Firestore", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun leerJugadoresDelUsuario() {
        val currentUserEmail = firebaseAuth.currentUser?.email
        Log.d("Firebase", "Email del usuario: $currentUserEmail")

        if (currentUserEmail != null) {
            val jugadoresCollection = db.collection("usuarios").document(currentUserEmail).collection("jugadores")

            jugadoresCollection.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase", "Obtención de jugadores exitosa")
                        for (document in task.result!!) {
                            try {
                                // Obtener los datos del jugador del documento
                                val nombre = document.getString("nombre")
                                val valoracion = document.getDouble("valoracion")
                                val posicion = document.getString("posicion")
                                val goles = document.getLong("goles")
                                val asistencias = document.getLong("asistencias")

                                // Determinar el tipo de jugador y crear la instancia adecuada
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
                        jugadoresAdapter.notifyDataSetChanged()
                    } else {
                        Log.e("Firebase", "Error al obtener los jugadores", task.exception)
                        Toast.makeText(this, "Error al obtener los jugadores", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Log.e("Firebase", "El email del usuario es nulo")
            // Puedes mostrar un mensaje o realizar alguna acción adecuada si el email es nulo
        }
    }


    private fun mostrarMensajeError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

}


