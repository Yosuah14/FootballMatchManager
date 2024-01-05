package com.example.footballmatchmanager

import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import com.example.footballmatchmanager.databinding.ActivityAcercaDeBinding
import com.example.footballmatchmanager.databinding.ActivityPreferenciasBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class Preferencias : AppCompatActivity() {
    lateinit var binding: ActivityPreferenciasBinding
    private val auth: FirebaseAuth = com.google.firebase.Firebase.auth
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferenciasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("idioma", MODE_PRIVATE)

        setSupportActionBar(binding.toolbarCrearJugadores)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarCrearJugadores.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.btnAplicar.setOnClickListener {
            val radioGroupIdioma = binding.idioma
            val radioButtonSeleccionado = findViewById<RadioButton>(radioGroupIdioma.checkedRadioButtonId)
            if (radioButtonSeleccionado != null) {
                val idiomaSeleccionado = when (radioButtonSeleccionado.id) {
                    R.id.ingles -> "en"
                    R.id.espanol -> "es"
                    else -> "es" // Idioma por defecto si no se encuentra ninguna selección
                }
                cambiarIdioma(idiomaSeleccionado)
                // Guardar el idioma seleccionado en las SharedPreferences
                sharedPreferences.edit().putString("idioma", idiomaSeleccionado).apply()
                // Ir a la actividad MenuOpciones
                val intent = Intent(this, MenuOpciones::class.java)
                startActivity(intent)
                finish() // Finalizar esta actividad para evitar que el usuario regrese al menú de preferencias
            }
        }

        binding.cerrarsesionmenuopciones.setOnLongClickListener{
            mostrarVentanaConfirmacionCerrarSesion()
            true
        }
        binding.btnBorrarUsuario.setOnClickListener {
            mostrarVentanaConfirmacionBorrar()
        }
    }


    private fun cambiarIdioma(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        // Reiniciar la actividad para aplicar el cambio de idioma
        recreate()
    }
    private fun borrarJugadoresEnFirestore() {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val jugadoresCollection = db.collection("usuarios").document(currentUserEmail).collection("jugadores")

            // Obtener todos los documentos de la colección "jugadores" del usuario actual
            jugadoresCollection.get().addOnSuccessListener { querySnapshot ->
                // Borrar cada documento de la colección
                for (document in querySnapshot.documents) {
                    jugadoresCollection.document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("BORRAR_JUGADOR", "Jugador borrado exitosamente de Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.e("BORRAR_JUGADOR", "Error al borrar jugador de Firestore", e)
                        }
                }
            }.addOnFailureListener { e ->
                Log.e("BORRAR_JUGADOR", "Error al obtener jugadores de Firestore", e)
            }
        }
    }
    private fun borrarPartidosEnFirestore() {
        val currentUserEmail = firebaseAuth.currentUser?.email

        if (currentUserEmail != null) {
            val partidosCollection = db.collection("usuarios").document(currentUserEmail).collection("partidos")

            // Obtener todos los documentos de la colección "partidos" del usuario actual
            partidosCollection.get().addOnSuccessListener { querySnapshot ->
                // Borrar cada documento de la colección
                for (document in querySnapshot.documents) {
                    partidosCollection.document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("BORRAR_PARTIDO", "Partido borrado exitosamente de Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.e("BORRAR_PARTIDO", "Error al borrar partido de Firestore", e)
                        }
                }
            }.addOnFailureListener { e ->
                Log.e("BORRAR_PARTIDO", "Error al obtener partidos de Firestore", e)
            }
        } else {
            Log.e("BORRAR_PARTIDO", "El email del usuario es nulo")
            // Puedes mostrar un mensaje o realizar alguna acción adecuada si el email es nulo
        }
    }


    fun borrarUsuarioEnUsersCollection() {
        val currentUserEmail = firebaseAuth.currentUser?.email

        if (currentUserEmail != null) {
            val userDocument = db.collection("users").document(currentUserEmail)

            // Borrar el documento del usuario en Firestore
            userDocument.delete()
                .addOnSuccessListener {
                    Log.d("BORRAR_USUARIO", "Usuario borrado exitosamente de Firestore")
                    // Realizar acciones adicionales después de borrar el usuario si es necesario
                }
                .addOnFailureListener { e ->
                    Log.e("BORRAR_USUARIO", "Error al borrar usuario de Firestore", e)
                }
        } else {
            Log.e("BORRAR_USUARIO", "El email del usuario es nulo")
            // Puedes mostrar un mensaje o realizar alguna acción adecuada si el email es nulo
        }
    }
    fun borrarDatosJugadoresPartido() {
        val currentUserEmail = firebaseAuth.currentUser?.email

        if (currentUserEmail != null) {
            val datosJugadoresPartidoCollection = db.collection("usuarios")
                .document(currentUserEmail)
                .collection("datosjugadorespartido")

            // Borrar todos los documentos en la colección "datosjugadorespartido"
            datosJugadoresPartidoCollection.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        datosJugadoresPartidoCollection.document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("BORRAR_DATOS_JUGADORES", "Datos del jugador para el partido borrados exitosamente de Firestore")
                                // Realizar acciones adicionales después de borrar los datos si es necesario
                            }
                            .addOnFailureListener { e ->
                                Log.e("BORRAR_DATOS_JUGADORES", "Error al borrar datos del jugador para el partido de Firestore", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BORRAR_DATOS_JUGADORES", "Error al obtener datos de jugadores para el partido de Firestore", e)
                }
        } else {
            Log.e("BORRAR_DATOS_JUGADORES", "El email del usuario es nulo")
            // Puedes mostrar un mensaje o realizar alguna acción adecuada si el email es nulo
        }
    }
    private fun cerrarSesion() {
        // Cerrar sesión en Firebase y en el proveedor de autenticación de Google
        auth.signOut()
        val signInClient = Identity.getSignInClient(this)
        signInClient.signOut().addOnCompleteListener {
            Log.e(ContentValues.TAG, "Cerrada sesión completamente")
            navigateToLogin()
        }.addOnFailureListener {
            Log.e(ContentValues.TAG, "Hubo algún error al cerrar la sesión")
            navigateToLogin()
        }
    }
    private fun navigateToLogin() {
        // Navegar a la actividad de inicio de sesión (Login)
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }
    private fun mostrarVentanaConfirmacionCerrarSesion() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                cerrarSesion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun mostrarVentanaConfirmacionBorrar() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Borrar Usuario")
            .setMessage("¿Estás seguro de que deseas borrar el usuario?")
            .setPositiveButton("Sí") { _, _ ->
                // Realiza la acción de borrado cuando el usuario confirma
                borrarJugadoresEnFirestore()
                borrarDatosJugadoresPartido()
                borrarUsuarioEnUsersCollection()
                borrarPartidosEnFirestore()
                cerrarSesion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}