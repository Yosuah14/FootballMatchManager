package com.example.footballmatchmanager

import Adaptadores.JugadoresAdapter
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footballmatchmanager.databinding.ActivityCrearJugadoresBinding
import com.example.footballmatchmanager.databinding.DialogCrearJugadorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.io.IOException

class CrearJugadores : AppCompatActivity() {
    private val binding by lazy {
        ActivityCrearJugadoresBinding.inflate(layoutInflater)
    }
    private var selectedImageUri: Uri? = null
    private lateinit var dialogImageView: ImageView
    private val REQUEST_IMAGE_PICK = 2
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
        dialogImageView = dialogBinding.imagenJugador
        builder.setView(dialogBinding.root)
        dialogBinding.BtnSeleccionarImgen.setOnClickListener {
            abrirGaleriaDesdeDialogo()
        }
        builder.setPositiveButton("Crear") { _, _ ->
            // Obtener datos del diálogo y crear una instancia de Jugador
            val nombre = dialogBinding.editTextNombre.text.toString()
            val valoracion = dialogBinding.editTextValoracion.text.toString()
            val goles = dialogBinding.editTextGoles.text.toString()
            val asistencias = dialogBinding.editTextAsistencias.text.toString()

            // Verificar que todos los campos obligatorios estén llenos y que se haya seleccionado una imagen
            if (nombre.isNotEmpty() && valoracion.isNotEmpty() && goles.isNotEmpty() && asistencias.isNotEmpty() && selectedImageUri != null) {
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
                        // Verificar si ya existe un jugador con el mismo nombre en Firestore
                        jugadorExistenteEnFirestore(nombre) { jugadorExistente ->
                            if (!jugadorExistente) {
                                // Crear una nueva instancia de Jugador solo si no existe
                                val nuevoJugador = when (posicion) {
                                    "Portero" -> Portero(
                                        valoracion.toLong(),
                                        nombre,
                                        "Portero",
                                        goles.toLong(),
                                        asistencias.toLong(),
                                        selectedImageUri.toString()
                                    )

                                    "Jugador Normal" -> Jugadores(
                                        valoracion.toLong(),
                                        nombre,
                                        "Jugador Normal",
                                        goles.toLong(),
                                        asistencias.toLong(),
                                        selectedImageUri.toString()
                                    )
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
                                mostrarMensajeError("Ya existe un jugador con el mismo nombre")
                            }
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
                mostrarMensajeError("Todos los campos obligatorios deben estar rellenos y debes seleccionar una imagen")
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
    private fun abrirGaleriaDesdeDialogo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Almacenar la URI de la imagen seleccionada
                selectedImageUri = uri
                // Mostrar la imagen en el ImageView del diálogo sin usar Glide
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    dialogImageView.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
    private fun guardarJugadorEnFirestore(jugador: JugadorBase) {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val jugadoresCollection = db.collection("usuarios").document(currentUserEmail).collection("jugadores")
            val jugadorData = hashMapOf(
                "nombre" to jugador.nombre,
                "valoracion" to jugador.valoracion,
                "posicion" to jugador.posicion,
                "goles" to jugador.goles,
                "asistencias" to jugador.asistencias,
                "imageUrl" to jugador.imagenUrl // Nuevo campo para la URL de la imagen
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
        if (currentUserEmail != null) {
            val jugadoresCollection = db.collection("usuarios").document(currentUserEmail).collection("jugadores")
            jugadoresCollection.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            try {
                                // Obtener los datos del jugador del documento
                                val nombre = document.getString("nombre")
                                val valoracion = document.getLong("valoracion")
                                val posicion = document.getString("posicion")
                                val goles = document.getLong("goles")
                                val asistencias = document.getLong("asistencias")
                                val imageUrl = document.getString("imageUrl") // Obtener la URL de la imagen
                                // Determinar el tipo de jugador y crear la instancia adecuada
                                val jugador: JugadorBase? = when (posicion) {
                                    "Portero" -> Portero(
                                        valoracion!!,
                                        nombre!!,
                                        posicion!!,
                                        goles!!,
                                        asistencias!!,
                                        imageUrl ?: ""
                                    )
                                    "Jugador Normal" -> Jugadores(
                                        valoracion!!,
                                        nombre!!,
                                        posicion!!,
                                        goles!!,
                                        asistencias!!,
                                        imageUrl ?: ""
                                    )
                                    else -> null
                                }
                                jugador?.let {
                                    jugadoresList.add(it)
                                }
                            } catch (e: Exception) {

                            }
                        }
                        jugadoresAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this, "Error al obtener los jugadores", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
        }
    }

    private fun mostrarMensajeError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
    private fun jugadorExistenteEnFirestore(nombre: String, onComplete: (Boolean) -> Unit) {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val jugadoresCollection = db.collection("usuarios").document(currentUserEmail).collection("jugadores")
            jugadoresCollection.whereEqualTo("nombre", nombre).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val jugadores = task.result?.toObjects(JugadorBase::class.java)
                        onComplete(jugadores?.isNotEmpty() == true)
                    } else {
                        onComplete(false)
                    }
                }
        } else {
            onComplete(false)

        }
    }

}



